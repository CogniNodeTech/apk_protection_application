package com.safeguard.security.layers.layer2

import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.repository.FuzzyMatch
import com.safeguard.core.domain.repository.MalwareSignature
import com.safeguard.core.domain.repository.ThreatDatabaseRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.util.Random

class HashValidatorTest {

    private lateinit var threatDb: ThreatDatabaseRepository
    private lateinit var validator: HashValidator
    private val tempFiles = mutableListOf<File>()

    @Before
    fun setUp() {
        threatDb = mock()
        validator = HashValidator(threatDb)
    }

    @After
    fun tearDown() {
        tempFiles.forEach { runCatching { it.delete() } }
        tempFiles.clear()
    }

    /**
     * Creates a synthetic APK-sized buffer with enough entropy that [com.safeguard.core.util.FuzzyHasher]
     * always produces a stable TLSH hash — the previous fixture (1 KB of sequential bytes) was below
     * TLSH's plausibility floor and made the suspicious-fuzzy path unreachable.
     */
    private fun apkLikeFile(seed: Long = 0xC0FFEE): File {
        val bytes = ByteArray(8_192)
        Random(seed).nextBytes(bytes)
        return File.createTempFile("apk", ".apk").apply {
            writeBytes(bytes)
            tempFiles.add(this)
        }
    }

    @Test
    fun knownMalwareHash_returnsMalicious() = runTest {
        val file = apkLikeFile()
        val sha256 = "known_malware_sha256"
        whenever(threatDb.findMalwareBySha256(any())).thenReturn(
            MalwareSignature(sha256, null, null, "TestMalware", "Family", 80, null, "test")
        )
        whenever(threatDb.findTrustedBySha256(any())).thenReturn(null)
        whenever(threatDb.findSimilarByFuzzyHash(any(), any())).thenReturn(emptyList())

        val result = validator.verify(APKContext(file), emptyList())

        assertEquals(Verdict.MALICIOUS, result.verdict)
        assertEquals(1.0f, result.confidence, 0.01f)
        assertEquals(100, result.riskScore)
        // SHA-512 must always be populated, even on legacy rows with no expected SHA-512.
        // Downstream layers (forensic reasoning, telemetry) rely on this invariant.
        assertEquals("SHA-512 must always be computed", 128, result.sha512?.length ?: 0)
        assertTrue("Collision flag must be false on confirmed malware", !result.isCollision)
    }

    @Test
    fun knownMalwareHash_withMatchingSha512_emitsConfirmedEvidence() = runTest {
        // The actual SHA-512 of the synthetic APK is deterministic from the seed; we read
        // it from the validator's own helper to keep this fixture in sync with whatever the
        // in-memory file ends up containing, so any future change to the fixture size /
        // entropy doesn't silently break this test.
        val file = apkLikeFile()
        val (sha256, sha512) = validator.calculateHashes(file)
        whenever(threatDb.findMalwareBySha256(any())).thenReturn(
            MalwareSignature(sha256, sha512, null, "ConfirmedMalware", "Anatsa", 95, null, "test")
        )
        whenever(threatDb.findTrustedBySha256(any())).thenReturn(null)
        whenever(threatDb.findSimilarByFuzzyHash(any(), any())).thenReturn(emptyList())

        val result = validator.verify(APKContext(file), emptyList())

        assertEquals(Verdict.MALICIOUS, result.verdict)
        assertEquals(1.0f, result.confidence, 0.01f)
        assertEquals(100, result.riskScore)
        assertTrue("Collision flag must be false when SHA-512 confirms", !result.isCollision)
        assertTrue(
            "Confirmed evidence must call out collision-resistant match for legal/IR context",
            result.evidence.any { it.contains("SHA-512 confirmed", ignoreCase = true) }
        )
    }

    @Test
    fun knownMalwareHash_withMismatchingSha512_returnsCollisionSuspicious() = runTest {
        // SHA-256 collision scenario: server says this hash belongs to "FakeBanker" with
        // a specific SHA-512, but the local file's SHA-512 disagrees. Either the threat
        // DB is corrupt or we have a real cryptographic collision — either way, refuse to
        // pin the threat name and downgrade verdict.
        val file = apkLikeFile()
        val wrongSha512 = "ff".repeat(64) // 128 hex chars, deliberately not the real digest
        whenever(threatDb.findMalwareBySha256(any())).thenReturn(
            MalwareSignature("any_sha256", wrongSha512, null, "FakeBanker", "Anatsa", 95, null, "test")
        )
        whenever(threatDb.findTrustedBySha256(any())).thenReturn(null)
        whenever(threatDb.findSimilarByFuzzyHash(any(), any())).thenReturn(emptyList())

        val result = validator.verify(APKContext(file), emptyList())

        assertEquals(Verdict.SUSPICIOUS, result.verdict)
        assertTrue("Collision branch must set isCollision=true", result.isCollision)
        // Threat name must be cleared on collision — labelling the user's file with a name
        // we can't actually verify is the failure mode this whole branch exists to prevent.
        assertEquals(null, result.threatName)
        assertTrue(
            "Evidence must surface that SHA-256 hit but SHA-512 differed",
            result.evidence.any { it.contains("collision detected", ignoreCase = true) }
        )
        assertTrue(
            "Evidence must include the expected SHA-512 for the matched row (forensic trail)",
            result.evidence.any { it.contains(wrongSha512, ignoreCase = true) }
        )
        // Risk score sits between the fuzzy-match floor (70) and confirmed malware (100)
        // so collision detections outrank "kind of looks like X" but stay below confirmed.
        assertTrue("Collision risk score must outrank fuzzy matches", result.riskScore >= 80)
        assertTrue("Collision must not exceed confirmed-malware risk score", result.riskScore < 100)
    }

    @Test
    fun apkContext_isPopulated_withBothHashesAfterScan() = runTest {
        val file = apkLikeFile()
        whenever(threatDb.findMalwareBySha256(any())).thenReturn(null)
        whenever(threatDb.findTrustedBySha256(any())).thenReturn(null)
        whenever(threatDb.findSimilarByFuzzyHash(any(), any())).thenReturn(emptyList())

        val ctx = APKContext(file)
        validator.verify(ctx, emptyList())

        // After the validator runs, both keys must be present so downstream layers don't
        // re-read the file. Without this invariant the forensic engine ends up doing a
        // second 100-MB read for nothing.
        assertEquals(64, ctx.getCached<String>(APKContext.KEY_SHA256)?.length ?: 0)
        assertEquals(128, ctx.getCached<String>(APKContext.KEY_SHA512)?.length ?: 0)
    }

    @Test
    fun unknownHash_returnsUnknown() = runTest {
        val file = apkLikeFile()
        whenever(threatDb.findMalwareBySha256(any())).thenReturn(null)
        whenever(threatDb.findTrustedBySha256(any())).thenReturn(null)
        whenever(threatDb.findSimilarByFuzzyHash(any(), any())).thenReturn(emptyList())

        val result = validator.verify(APKContext(file), emptyList())

        assertEquals(Verdict.UNKNOWN, result.verdict)
        assertEquals(0.5f, result.confidence, 0.01f)
    }

    @Test
    fun similarFuzzyHash_returnsSuspicious() = runTest {
        val file = apkLikeFile()
        whenever(threatDb.findMalwareBySha256(any())).thenReturn(null)
        whenever(threatDb.findTrustedBySha256(any())).thenReturn(null)
        whenever(threatDb.findSimilarByFuzzyHash(any(), any())).thenReturn(
            listOf(FuzzyMatch("SimilarThreat", 75))
        )

        val result = validator.verify(APKContext(file), emptyList())

        assertEquals(Verdict.SUSPICIOUS, result.verdict)
        assertEquals("Risk score must mirror the matched similarity", 75, result.riskScore)
        assertTrue(
            "Evidence must surface the TLSH similarity to the user",
            result.evidence.any { it.contains("TLSH", ignoreCase = true) }
        )
    }

    @Test
    fun tooSmallFile_skipsFuzzyLookup() = runTest {
        // Files smaller than the TLSH plausibility floor must not consume the per-row similarity
        // budget on the fuzzy index; instead they fall through to UNKNOWN.
        val tiny = File.createTempFile("tinyapk", ".apk").apply {
            writeBytes(ByteArray(64)) // below FuzzyHasher.MIN_INPUT_BYTES
            tempFiles.add(this)
        }
        whenever(threatDb.findMalwareBySha256(any())).thenReturn(null)
        whenever(threatDb.findTrustedBySha256(any())).thenReturn(null)

        val result = validator.verify(APKContext(tiny), emptyList())

        assertEquals(Verdict.UNKNOWN, result.verdict)
    }
}
