package com.safeguard.security.layers.layer7

import com.safeguard.core.domain.layer.ProtectionLayer
import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.ThreatInfo
import com.safeguard.core.domain.model.Verdict
import com.safeguard.security.layers.layer7.yara.RuleHit
import com.safeguard.security.layers.layer7.yara.YaraMatcher
import com.safeguard.security.layers.layer7.yara.YaraRule
import java.io.File
import java.util.zip.ZipFile

/**
 * Layer 7 — YARA-subset content scanner.
 *
 * Why this layer exists:
 *  - Layers 2 (hashes), 3 (permissions), 4 (signatures), 5 (ML), 6 (cloud) all key off
 *    *metadata* about the APK. None of them inspect actual code or string content. This
 *    means a freshly-recompiled banker with a clean SHA-256, normal permissions, a fresh
 *    cert, weak ML signal, and no cloud match would slip through completely.
 *  - YARA rules give us a cheap, declarative way to spot known string / hex fingerprints
 *    inside DEX bytecode strings, the compiled manifest, and any small native libs.
 *
 * Inputs scanned (each capped at [MAX_BYTES_PER_BUFFER] to bound work):
 *  - All `classes*.dex` entries (multi-dex aware)
 *  - `AndroidManifest.xml` (compiled binary form is fine — string atoms still hit)
 *  - Top-level `lib/`*\/`*.so` files (only the first [MAX_NATIVE_LIBS_SCANNED] of them)
 *
 * Hard ceilings:
 *  - [MAX_TOTAL_BYTES] across all artifacts in a single scan to bound CPU on absurd APKs
 *  - [MAX_BYTES_PER_BUFFER] per individual entry
 *  - [MAX_NATIVE_LIBS_SCANNED] cap on number of native libs visited
 *  - Defensive try/catch around every entry read so a corrupt zip never escalates to a
 *    crashed scan; we surface a SAFE-with-low-confidence result instead.
 *
 * Threat-info / verdict mapping:
 *  - Hits on rules with `meta.severity >= 90` → MALICIOUS @ 0.92 confidence. This is
 *    deliberately **just above** the ZeroTrustDecisionEngine's 0.85 high-conf threshold,
 *    so a single high-severity Layer 7 fire is enough to BLOCK on its own. Recompiled
 *    bankers tend to be exactly this case (string fingerprint survives recompile).
 *  - Hits with severity 60..89 → SUSPICIOUS proportional to severity.
 *  - No hits → SAFE @ 0.7 (we only scanned a subset of the file; not enough signal to
 *    *vouch* for it).
 *  - Empty ruleset (rare; means the asset bundle was stripped) → SAFE @ 0.5 with an
 *    explanatory evidence line.
 */
class YaraScanner(
    private val ruleSet: YaraRuleSet
) : ProtectionLayer {

    override suspend fun verify(
        context: APKContext,
        previousLayerResults: List<LayerResult>
    ): Layer7Result {
        val start = System.currentTimeMillis()
        if (ruleSet.rules.isEmpty()) {
            return safeResult(
                start,
                "Layer 7 ruleset is empty (no .yar assets bundled); skipping content scan",
                confidence = 0.5f
            )
        }

        val matcher = YaraMatcher(ruleSet.rules)
        var totalBytesScanned = 0L
        var nativeLibsScanned = 0
        var hadReadFailure = false

        try {
            ZipFile(context.apkFile).use { zip ->
                val entries = zip.entries().toList()
                // Walk in a deterministic order: dex first (highest-signal), then manifest,
                // then native libs. This makes evidence ordering stable across runs.
                val dexEntries = entries.filter { it.name.matches(DEX_REGEX) }
                val manifest = entries.firstOrNull { it.name == MANIFEST_NAME }
                val nativeLibs = entries.filter { it.name.matches(NATIVE_LIB_REGEX) }
                    .sortedBy { it.name }

                fun feed(entryName: String, bytes: ByteArray): Boolean {
                    if (totalBytesScanned >= MAX_TOTAL_BYTES) return false
                    val len = minOf(bytes.size.toLong(), MAX_BYTES_PER_BUFFER.toLong()).toInt()
                    matcher.scan(bytes, len)
                    totalBytesScanned += len
                    return true
                }

                for (e in dexEntries) {
                    if (totalBytesScanned >= MAX_TOTAL_BYTES) break
                    val bytes = readEntryBytes(zip, e.name)
                    if (bytes == null) {
                        hadReadFailure = true
                    } else if (!feed(e.name, bytes)) {
                        break
                    }
                }
                if (manifest != null && totalBytesScanned < MAX_TOTAL_BYTES) {
                    val bytes = readEntryBytes(zip, manifest.name)
                    if (bytes != null) feed(manifest.name, bytes) else hadReadFailure = true
                }
                for (e in nativeLibs) {
                    if (nativeLibsScanned >= MAX_NATIVE_LIBS_SCANNED) break
                    if (totalBytesScanned >= MAX_TOTAL_BYTES) break
                    val bytes = readEntryBytes(zip, e.name)
                    if (bytes == null) {
                        hadReadFailure = true
                    } else {
                        if (!feed(e.name, bytes)) break
                        nativeLibsScanned++
                    }
                }
            }
        } catch (e: Exception) {
            // A corrupt or truncated zip should not bubble up — Layer 7 must always
            // produce a result. Log via evidence and return SAFE-with-low-confidence so
            // upstream zero-trust treats this as ambient noise, not a verdict.
            return safeResult(
                start,
                "Pattern scan skipped: APK could not be opened (${e.javaClass.simpleName})",
                confidence = 0.4f
            )
        }

        val hits = matcher.results()
        val time = System.currentTimeMillis() - start
        if (hits.isEmpty()) {
            val msg = buildList {
                add("No pattern rules fired across ${ruleSet.rules.size} rules")
                add("scanned_bytes=$totalBytesScanned")
                if (hadReadFailure) add("note: some entries unreadable")
            }
            return Layer7Result(
                verdict = Verdict.SAFE,
                confidence = if (hadReadFailure) 0.55f else 0.7f,
                riskScore = if (hadReadFailure) 25 else 10,
                firedRules = emptyList(),
                highestSeverity = 0,
                evidence = msg,
                executionTimeMs = time
            )
        }

        return verdictFromHits(hits, totalBytesScanned, time, hadReadFailure)
    }

    private fun verdictFromHits(
        hits: List<RuleHit>,
        totalBytesScanned: Long,
        timeMs: Long,
        hadReadFailure: Boolean
    ): Layer7Result {
        val highestSev = hits.maxOf { it.rule.meta.severity }
        val verdict = when {
            highestSev >= MALICIOUS_SEVERITY_THRESHOLD -> Verdict.MALICIOUS
            highestSev >= SUSPICIOUS_SEVERITY_THRESHOLD -> Verdict.SUSPICIOUS
            else -> Verdict.SUSPICIOUS // Even low-sev rule fires deserve attention.
        }
        val confidence = when (verdict) {
            Verdict.MALICIOUS -> 0.92f
            Verdict.SUSPICIOUS -> (highestSev / 100f).coerceIn(0.5f, 0.85f)
            else -> 0.7f
        }
        val risk = highestSev.coerceIn(0, 100)
        val evidence = mutableListOf<String>()
        evidence += "Pattern rules fired: ${hits.size}/${ruleSet.rules.size}"
        evidence += "scanned_bytes=$totalBytesScanned"
        if (hadReadFailure) evidence += "note: some entries unreadable, scan may be partial"
        for (h in hits) {
            val tag = h.rule.meta.family ?: h.rule.name
            evidence += "rule=${h.rule.name} family=$tag severity=${h.rule.meta.severity} matches=${h.matchedIdentifiers.joinToString(",")}"
        }
        // Pick the highest-severity rule for ThreatInfo so the dashboard shows the most
        // damaging finding first if multiple rules fired.
        val flagship = hits.maxBy { it.rule.meta.severity }.rule
        val threatInfo = ThreatInfo(
            threatName = flagship.meta.description ?: flagship.name,
            threatFamily = flagship.meta.family,
            severity = flagship.meta.severity,
            avDetections = null,
            totalAvScanned = null,
            communityReports = null
        )
        return Layer7Result(
            verdict = verdict,
            confidence = confidence,
            riskScore = risk,
            firedRules = hits.map { it.rule.name },
            highestSeverity = highestSev,
            evidence = evidence,
            executionTimeMs = timeMs,
            threatInfo = threatInfo
        )
    }

    private fun readEntryBytes(zip: ZipFile, name: String): ByteArray? {
        return try {
            val entry = zip.getEntry(name) ?: return null
            // Cap each entry up-front. If the declared size is enormous we read at most
            // MAX_BYTES_PER_BUFFER from the stream — keeping a runaway entry from
            // ballooning into memory.
            val cap = MAX_BYTES_PER_BUFFER
            zip.getInputStream(entry).use { ins ->
                val buf = ByteArray(cap.coerceAtMost(8192))
                val out = java.io.ByteArrayOutputStream(minOf(cap, 64 * 1024))
                var read = ins.read(buf)
                while (read > 0 && out.size() < cap) {
                    val toWrite = minOf(read, cap - out.size())
                    out.write(buf, 0, toWrite)
                    if (out.size() >= cap) break
                    read = ins.read(buf)
                }
                out.toByteArray()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun safeResult(start: Long, reason: String, confidence: Float): Layer7Result {
        return Layer7Result(
            verdict = Verdict.SAFE,
            confidence = confidence,
            riskScore = 10,
            firedRules = emptyList(),
            highestSeverity = 0,
            evidence = listOf(reason),
            executionTimeMs = System.currentTimeMillis() - start
        )
    }

    companion object {
        // Public-ish for tests in same module to tune.
        internal const val MAX_BYTES_PER_BUFFER = 4 * 1024 * 1024 // 4 MiB / artifact
        internal const val MAX_TOTAL_BYTES = 32 * 1024 * 1024L // 32 MiB / scan
        internal const val MAX_NATIVE_LIBS_SCANNED = 6
        internal const val MALICIOUS_SEVERITY_THRESHOLD = 90
        internal const val SUSPICIOUS_SEVERITY_THRESHOLD = 60

        private const val MANIFEST_NAME = "AndroidManifest.xml"
        private val DEX_REGEX = Regex("""classes\d*\.dex""")
        private val NATIVE_LIB_REGEX = Regex("""lib/[^/]+/[^/]+\.so""")
    }

    internal val rules: List<YaraRule> get() = ruleSet.rules
}
