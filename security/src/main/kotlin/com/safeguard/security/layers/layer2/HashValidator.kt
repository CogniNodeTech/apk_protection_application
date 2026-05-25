package com.safeguard.security.layers.layer2

import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.ThreatInfo
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.repository.ThreatDatabaseRepository
import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.layer.ProtectionLayer
import com.safeguard.core.util.FuzzyHasher
import java.io.File
import java.security.MessageDigest

class HashValidator(
    private val threatDb: ThreatDatabaseRepository
) : ProtectionLayer {

    companion object {
        /** Default minimum TLSH similarity (%) for a fuzzy match to be considered suspicious. */
        const val MIN_FUZZY_SIMILARITY = 70
    }

    override suspend fun verify(context: APKContext, previousLayerResults: List<LayerResult>): Layer2Result {
        val start = System.currentTimeMillis()
        val apkFile = context.apkFile

        // Compute SHA-256 and SHA-512 in a single pass so the SHA-512 collision check costs
        // exactly one extra `MessageDigest.update` per buffer instead of a second full file
        // read. Both values land in the shared APK context so downstream layers (e.g. the
        // forensic reasoning engine) get the strongest digest for free.
        val cachedSha256 = context.getCached<String>(APKContext.KEY_SHA256)
        val cachedSha512 = context.getCached<String>(APKContext.KEY_SHA512)
        val (sha256, sha512) = if (cachedSha256 != null && cachedSha512 != null) {
            cachedSha256 to cachedSha512
        } else {
            val computed = calculateHashes(apkFile)
            context.putCached(APKContext.KEY_SHA256, computed.first)
            context.putCached(APKContext.KEY_SHA512, computed.second)
            computed
        }

        // TLSH fuzzy hash. May be null for very small files or low-entropy buffers; in that
        // case we simply skip the variant-similarity lookup below — exact-hash and trusted-app
        // checks are unaffected.
        val fuzzyHash = FuzzyHasher.hash(apkFile)

        val malware = threatDb.findMalwareBySha256(sha256)
        if (malware != null) {
            val expectedSha512 = malware.sha512?.lowercase()?.takeIf { it.isNotBlank() }
            // Collision branch: the threat row carries a SHA-512 *and* it disagrees with what
            // we just computed locally. Either the row is corrupt (feed-pipeline bug, MITM
            // tampering of the threat DB) or this is the first SHA-256 collision in the
            // wild — in 2024 that's still a research-grade event, but we treat both
            // possibilities the same way: refuse to label the file with the matched row's
            // threat name and downgrade to SUSPICIOUS so the user gets a "needs review"
            // outcome instead of a confidently-wrong "MALICIOUS: <name>".
            val isCollision = expectedSha512 != null && expectedSha512 != sha512
            val time = System.currentTimeMillis() - start
            return if (isCollision) {
                Layer2Result(
                    verdict = Verdict.SUSPICIOUS,
                    confidence = COLLISION_CONFIDENCE,
                    riskScore = COLLISION_RISK_SCORE,
                    sha256 = sha256,
                    sha512 = sha512,
                    threatName = null,
                    similarity = 100,
                    evidence = listOf(
                        "Hash collision detected: SHA-256 matches '${malware.threatName}' but SHA-512 differs.",
                        "Expected SHA-512: $expectedSha512",
                        "Actual   SHA-512: $sha512",
                        "Treating as suspicious: either the threat database row is corrupt or this is a SHA-256 collision."
                    ),
                    executionTimeMs = time,
                    isCollision = true,
                    threatInfo = null
                )
            } else {
                val sha512Confirmed = expectedSha512 != null && expectedSha512 == sha512
                val evidence = buildList {
                    add("KNOWN MALWARE: ${malware.threatName}")
                    add("Family: ${malware.threatFamily ?: "Unknown"}")
                    add("Severity: ${malware.severity}/100")
                    if (sha512Confirmed) {
                        // Surfacing the SHA-512 confirmation in evidence lets the forensic
                        // engine cite "collision-resistant match" in user-facing reports —
                        // an important distinction for legal/IR contexts where the customer
                        // needs to know we matched on more than just SHA-256.
                        add("SHA-512 confirmed (collision-resistant match).")
                    }
                }
                Layer2Result(
                    verdict = Verdict.MALICIOUS,
                    confidence = 1.0f,
                    riskScore = 100,
                    sha256 = sha256,
                    sha512 = sha512,
                    threatName = malware.threatName,
                    similarity = 100,
                    evidence = evidence,
                    executionTimeMs = time,
                    threatInfo = ThreatInfo(malware.threatName, malware.threatFamily, malware.severity, null, null, null)
                )
            }
        }

        val trusted = threatDb.findTrustedBySha256(sha256)
        if (trusted != null && !threatDb.isTrustedExpired(trusted)) {
            val time = System.currentTimeMillis() - start
            return Layer2Result(
                verdict = Verdict.SAFE,
                confidence = 0.8f,
                riskScore = 10,
                sha256 = sha256,
                sha512 = sha512,
                threatName = null,
                similarity = null,
                evidence = listOf("Matches trusted app: ${trusted.packageName}"),
                executionTimeMs = time
            )
        }

        if (fuzzyHash != null) {
            val similar = threatDb.findSimilarByFuzzyHash(fuzzyHash, MIN_FUZZY_SIMILARITY)
            if (similar.isNotEmpty()) {
                val best = similar.maxByOrNull { it.similarity }!!
                val time = System.currentTimeMillis() - start
                return Layer2Result(
                    verdict = Verdict.SUSPICIOUS,
                    confidence = best.similarity / 100f,
                    riskScore = best.similarity,
                    sha256 = sha256,
                    sha512 = sha512,
                    threatName = best.threatName,
                    similarity = best.similarity,
                    evidence = listOf(
                        "Similar to known threat: ${best.threatName}",
                        "TLSH similarity: ${best.similarity}%"
                    ),
                    executionTimeMs = time
                )
            }
        }

        val time = System.currentTimeMillis() - start
        return Layer2Result(
            verdict = Verdict.UNKNOWN,
            confidence = 0.5f,
            riskScore = 50,
            sha256 = sha256,
            sha512 = sha512,
            threatName = null,
            similarity = null,
            evidence = listOf("Hash not in database - requires deeper analysis"),
            executionTimeMs = time
        )
    }

    /**
     * Single-pass SHA-256 + SHA-512. Both digests are fed from the same buffered read so the
     * file is touched once even when we need both hashes — important for the on-install
     * scan path where users notice a 100 ms vs 200 ms difference on multi-MB APKs.
     *
     * Returns lowercase hex (no separators, no `0x` prefix). Lower-case is the convention
     * the threat-feed DTO and Room schema both normalise to in [com.safeguard.data.repository.ThreatFeedRepositoryImpl],
     * so equality checks against `MalwareSignature.sha512` don't need an extra `.lowercase()`
     * call here. We still defensively lowercase the row's value at the comparison site —
     * defence in depth against a future migration that forgets to normalise.
     */
    internal fun calculateHashes(file: File): Pair<String, String> {
        val sha256Digest = MessageDigest.getInstance("SHA-256")
        val sha512Digest = MessageDigest.getInstance("SHA-512")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } > 0) {
                sha256Digest.update(buffer, 0, read)
                sha512Digest.update(buffer, 0, read)
            }
        }
        return sha256Digest.digest().toHex() to sha512Digest.digest().toHex()
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }
}

/**
 * Confidence of a SHA-256 hit whose SHA-512 doesn't match. Picked at 0.85 because:
 *  - it's high enough that the orchestrator/UI treats the file as a real concern
 *    (the cloud-verification layer's own "low risk" threshold is 0.5);
 *  - it's strictly below the 1.0 we use for fully-confirmed malware, so any future
 *    decision-engine rule that branches on `>= 0.95` won't accidentally treat collisions
 *    as confirmed malware;
 *  - it's well above the 0.7 fuzzy-match floor so collisions outrank "looks similar to
 *    something bad" evidence in the aggregated risk score.
 */
private const val COLLISION_CONFIDENCE: Float = 0.85f

/** Risk-score parallel of [COLLISION_CONFIDENCE]: 85 / 100 → SUSPICIOUS but not MALICIOUS. */
private const val COLLISION_RISK_SCORE: Int = 85
