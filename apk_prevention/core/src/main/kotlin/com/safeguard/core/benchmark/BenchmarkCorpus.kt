package com.safeguard.core.benchmark

import com.safeguard.core.domain.model.Verdict
import java.io.BufferedReader
import java.io.File
import java.io.Reader
import java.io.StringReader

/**
 * One labelled APK in the Phase 3.3 evaluation corpus.
 *
 * @property sha256 lowercase hex of the APK; serves as the primary key. Reusing a sample
 *   across runs (e.g. swapping the file path between dev machines) is supported because
 *   the corpus is *content*-addressed, not path-addressed.
 * @property expected the ground-truth verdict the corpus author asserts. Only [Verdict.SAFE]
 *   and [Verdict.MALICIOUS] are valid here — the metric calculations don't currently
 *   model "labelled SUSPICIOUS-by-curator", and silently coercing them would make the
 *   resulting precision/recall meaningless.
 * @property apkPath optional file path on disk. When present and the file exists, the
 *   harness will run the full scan pipeline; when absent, the harness falls back to a
 *   pre-recorded oracle (useful for CI runs that ship hash-only manifests).
 * @property metadata free-form key/value pairs from the manifest. Currently informational
 *   only — emitted to the per-row report so a curator can debug "why did this firmware
 *   sample get misclassified".
 */
data class BenchmarkSample(
    val sha256: String,
    val expected: Verdict,
    val apkPath: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(sha256.length == 64) { "sha256 must be 64 hex chars; got '${sha256.take(8)}…' (${sha256.length})" }
        require(expected == Verdict.SAFE || expected == Verdict.MALICIOUS) {
            "Corpus labels must be SAFE or MALICIOUS; got $expected for $sha256"
        }
    }
}

/**
 * Parses the Phase 3.3 corpus manifest CSV. Format (header row required):
 *
 * ```
 * sha256,label,apk_path,note
 * 5b1c4e…,malicious,/data/corpus/anatsa/v3/sample.apk,banker dropper
 * d29df4…,clean,,bundled play-store sample
 * ```
 *
 * Required columns: `sha256`, `label`. Optional columns: `apk_path` and any `*` column
 * is folded into [BenchmarkSample.metadata] verbatim. We intentionally accept either
 * `clean` or `safe` for the label, and `malicious` / `malware`, because real-world
 * curator workflows tend to mix terminology and we want to avoid a load-time diff war.
 *
 * Lines starting with `#` are treated as comments. Blank lines are ignored.
 */
object BenchmarkCorpusLoader {

    private val LABEL_TO_VERDICT: Map<String, Verdict> = mapOf(
        "clean" to Verdict.SAFE,
        "safe" to Verdict.SAFE,
        "benign" to Verdict.SAFE,
        "malicious" to Verdict.MALICIOUS,
        "malware" to Verdict.MALICIOUS,
        "bad" to Verdict.MALICIOUS
    )

    /**
     * Convenience for `loadFrom(reader = file.bufferedReader())`. Throws
     * [IllegalStateException] if the file doesn't exist — corpus runs that intend to be
     * skipped should check for the env var first instead of relying on a missing file.
     */
    fun loadFromFile(file: File): List<BenchmarkSample> {
        check(file.exists() && file.isFile) {
            "Corpus manifest not found at ${file.absolutePath}"
        }
        return file.bufferedReader().use(::loadFrom)
    }

    fun loadFromString(csv: String): List<BenchmarkSample> =
        StringReader(csv).buffered().use(::loadFrom)

    fun loadFrom(reader: Reader): List<BenchmarkSample> {
        val br: BufferedReader = if (reader is BufferedReader) reader else BufferedReader(reader)
        val rawLines = br.readLines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") }
        if (rawLines.isEmpty()) return emptyList()

        val header = parseCsvLine(rawLines.first()).map { it.trim().lowercase() }
        val shaIdx = header.indexOf("sha256").also {
            require(it >= 0) { "Corpus manifest header must contain 'sha256'" }
        }
        val labelIdx = header.indexOf("label").also {
            require(it >= 0) { "Corpus manifest header must contain 'label'" }
        }
        val pathIdx = header.indexOf("apk_path")

        val samples = mutableListOf<BenchmarkSample>()
        for ((rowIdx, line) in rawLines.drop(1).withIndex()) {
            val cols = parseCsvLine(line)
            // Defensive: don't fail the whole run for a malformed row. Skipping a single
            // line with a warning is preferable when the user is iterating on a corpus.
            if (cols.size <= maxOf(shaIdx, labelIdx)) {
                System.err.println(
                    "[benchmark] skipping malformed row ${rowIdx + 2}: not enough columns ($line)"
                )
                continue
            }
            val sha = cols[shaIdx].trim().lowercase()
            val labelRaw = cols[labelIdx].trim().lowercase()
            val verdict = LABEL_TO_VERDICT[labelRaw] ?: run {
                System.err.println(
                    "[benchmark] skipping row ${rowIdx + 2}: unknown label '$labelRaw' (expected one of ${LABEL_TO_VERDICT.keys})"
                )
                return@run null
            } ?: continue

            val apkPath = pathIdx.takeIf { it >= 0 && it < cols.size }
                ?.let { cols[it].trim().takeIf(String::isNotEmpty) }
            val metadata = header.indices
                .filter { it != shaIdx && it != labelIdx && it != pathIdx && it < cols.size }
                .associate { idx -> header[idx] to cols[idx].trim() }
                .filterValues { it.isNotEmpty() }

            samples += BenchmarkSample(sha256 = sha, expected = verdict, apkPath = apkPath, metadata = metadata)
        }
        return samples
    }

    /**
     * Hand-rolled CSV splitter — we avoid pulling in Apache Commons CSV / OpenCSV for a
     * single internal harness. Supports double-quoted fields and embedded `""` escapes,
     * which is enough for curator notes containing commas. Doesn't support newlines
     * inside quoted fields (corpora that need that should pre-process the manifest).
     */
    private fun parseCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var i = 0
        var inQuotes = false
        while (i < line.length) {
            val c = line[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i += 2
                        continue
                    }
                    inQuotes = false
                } else {
                    sb.append(c)
                }
            } else {
                when (c) {
                    ',' -> { out += sb.toString(); sb.setLength(0) }
                    '"' -> inQuotes = true
                    else -> sb.append(c)
                }
            }
            i++
        }
        out += sb.toString()
        return out
    }
}
