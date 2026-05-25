package com.safeguard.core.benchmark

import com.safeguard.core.domain.model.Verdict
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File

/**
 * Phase 3.3 corpus smoke harness.
 *
 * This test is **skipped by default** so the CI pipeline doesn't require a malware
 * corpus to be present. Curators run it locally — or in a private CI shard with the
 * corpus mounted as a volume — by setting:
 *
 *   - `SAFEGUARD_BENCHMARK_MANIFEST` — required, path to a CSV manifest
 *   - `SAFEGUARD_BENCHMARK_ORACLE`   — optional, path to a JSON map of pre-recorded
 *                                       `sha256 -> verdict` (verdict ∈ SAFE/MALICIOUS/
 *                                       SUSPICIOUS/UNKNOWN). When absent the test falls
 *                                       back to the [InMemoryScanOracle.fallback] verdict
 *                                       (UNKNOWN), which is fine for verifying the
 *                                       harness pipeline; in production the app module
 *                                       will plug in a `ScanOrchestrator`-backed oracle.
 *   - `SAFEGUARD_BENCHMARK_RUN_ID`   — optional human-readable identifier for the run
 *
 * The point of running this in `core/src/test` (rather than as a `:app:` Android test)
 * is that we want metric calculations to be reproducible on plain-JVM CI without an
 * emulator. The `app:` module will add a separate harness that wires up the real
 * `ScanOrchestrator` once it's been packaged in a JVM-friendly form.
 */
class CorpusBenchmarkSmokeTest {

    @Test
    fun runCorpusBenchmark_whenManifestEnvVarIsSet() {
        val manifestPath = System.getenv("SAFEGUARD_BENCHMARK_MANIFEST")
            ?: System.getProperty("safeguard.benchmark.manifest")
        Assume.assumeTrue(
            "Skipping corpus smoke benchmark: set SAFEGUARD_BENCHMARK_MANIFEST to enable.",
            !manifestPath.isNullOrBlank()
        )

        val manifestFile = File(manifestPath!!)
        Assume.assumeTrue(
            "Skipping corpus smoke benchmark: manifest path '$manifestPath' is not readable.",
            manifestFile.exists() && manifestFile.canRead()
        )

        val corpus = BenchmarkCorpusLoader.loadFromFile(manifestFile)
        Assume.assumeTrue(
            "Skipping corpus smoke benchmark: manifest contained zero usable rows.",
            corpus.isNotEmpty()
        )

        val oracle = buildOracle()
        val runId = System.getenv("SAFEGUARD_BENCHMARK_RUN_ID")
            ?: "corpus-${System.currentTimeMillis()}"

        val predictions = BenchmarkRunner(oracle).run(corpus)
        val matrix = BenchmarkMetrics.confusionMatrix(predictions)
        val report = BenchmarkMetrics.renderReport(matrix, runId)

        // Print to stdout so curators can paste the score block into BENCHMARKING.md
        // verbatim. We deliberately do not fail the build on a low score — quality
        // gates belong in a dedicated CI step that the curator wires up explicitly.
        println(report)
        assertTrue(predictions.isNotEmpty())
    }

    /**
     * If `SAFEGUARD_BENCHMARK_ORACLE` is set, parse it as a tiny JSON map of `{sha256:
     * verdictName}` and use [InMemoryScanOracle]. Otherwise fall back to a single shared
     * UNKNOWN verdict — the harness still measures *something* (zero detection-rate)
     * which is informative when the corpus is misconfigured.
     */
    private fun buildOracle(): ScanOracle {
        val oraclePath = System.getenv("SAFEGUARD_BENCHMARK_ORACLE")
            ?: System.getProperty("safeguard.benchmark.oracle")
        if (oraclePath.isNullOrBlank()) {
            return InMemoryScanOracle(
                verdicts = emptyMap(),
                fallback = OracleVerdict(Verdict.UNKNOWN, 0f, "no oracle configured")
            )
        }
        val file = File(oraclePath)
        if (!file.exists()) {
            return InMemoryScanOracle(
                verdicts = emptyMap(),
                fallback = OracleVerdict(Verdict.UNKNOWN, 0f, "oracle file '$oraclePath' missing")
            )
        }
        val raw = file.readText()
        // Tiny hand-rolled JSON object parser that's safe enough for `{"sha":"verdict"}`
        // map literals. We avoid pulling Moshi/Gson into `core` just for the corpus
        // tooling; the curator-controlled JSON shape is intentionally trivial.
        val verdicts = parseFlatVerdictMap(raw)
        return InMemoryScanOracle(verdicts)
    }

    private fun parseFlatVerdictMap(json: String): Map<String, OracleVerdict> {
        val out = HashMap<String, OracleVerdict>()
        val s = json.trim().removePrefix("{").removeSuffix("}")
        if (s.isBlank()) return out
        for (rawEntry in splitJsonEntries(s)) {
            val (rawKey, rawVal) = rawEntry.split(":", limit = 2)
                .takeIf { it.size == 2 } ?: continue
            val key = rawKey.trim().trim('"').lowercase()
            val verdictName = rawVal.trim().trim('"').uppercase()
            val verdict = runCatching { Verdict.valueOf(verdictName) }.getOrNull() ?: continue
            out[key] = OracleVerdict(verdict, 0.9f, "recorded")
        }
        return out
    }

    /**
     * Split `"a":"b","c":"d"` on top-level commas — i.e. commas not inside a quoted
     * string. Naive but correct for our corpus oracle format which never embeds commas
     * inside a verdict literal.
     */
    private fun splitJsonEntries(body: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (c in body) {
            when {
                c == '"' -> { inQuotes = !inQuotes; sb.append(c) }
                c == ',' && !inQuotes -> { out += sb.toString(); sb.setLength(0) }
                else -> sb.append(c)
            }
        }
        if (sb.isNotEmpty()) out += sb.toString()
        return out.map(String::trim).filter(String::isNotEmpty)
    }
}
