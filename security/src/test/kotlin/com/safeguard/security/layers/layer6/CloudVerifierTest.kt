package com.safeguard.security.layers.layer6

import com.safeguard.core.domain.integrity.PlayIntegrityChecker
import com.safeguard.core.domain.integrity.PlayIntegrityVerdict
import com.safeguard.core.domain.integrity.ScanIntegrityContext
import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.repository.CloudVerificationRepository
import com.safeguard.core.domain.repository.CloudVerificationResponse
import com.safeguard.core.domain.repository.DeviceCloudMetadata
import com.safeguard.core.domain.repository.LocalLayerScores
import com.safeguard.data.local.preferences.SecurePreferencesManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

private class FakeCloudRepo : CloudVerificationRepository {
    lateinit var response: CloudVerificationResponse
    val callCount = AtomicInteger(0)

    override suspend fun verify(
        sha256: String,
        sha512: String,
        packageName: String,
        versionCode: Int,
        permissions: List<String>,
        fileSize: Long,
        targetSdk: Int,
        signatureFingerprint: String?,
        localLayerScores: LocalLayerScores,
        deviceMetadata: DeviceCloudMetadata
    ): CloudVerificationResponse {
        callCount.incrementAndGet()
        return response
    }
}

class CloudVerifierTest {

    private lateinit var cloudRepo: FakeCloudRepo
    private lateinit var prefs: SecurePreferencesManager
    private lateinit var verifier: CloudVerifier

    @Before
    fun setUp() {
        cloudRepo = FakeCloudRepo()
        prefs = mock()
        whenever(prefs.privacyOnboardingAcknowledged).thenReturn(true)
        whenever(prefs.cloudVerificationEnabled).thenReturn(true)
        verifier = CloudVerifier(cloudRepo, 34, "en-US", prefs)
    }

    @Test
    fun cloudReturnsUnknown_verifierReturnsUnknown() = runTest {
        val file = File.createTempFile("apk", ".apk").apply { writeBytes(ByteArray(100)) }
        cloudRepo.response = CloudVerificationResponse(
            verdict = "UNKNOWN",
            confidence = 0f,
            threatName = null,
            threatFamily = null,
            avDetections = null,
            totalAvScanned = null,
            communityReports = null,
            virustotalLink = null,
            evidence = listOf("Cloud verification unavailable"),
            recommendation = "WARN_USER"
        )

        val result = verifier.verify(APKContext(file), emptyList())

        assertEquals(Verdict.UNKNOWN, result.verdict)
        assertEquals(0f, result.confidence, 0.01f)
        file.delete()
    }

    @Test
    fun cloudReturnsSafe_verifierReturnsSafe() = runTest {
        val file = File.createTempFile("apk", ".apk").apply { writeBytes(ByteArray(100)) }
        cloudRepo.response = CloudVerificationResponse(
            verdict = "SAFE",
            confidence = 0.95f,
            threatName = null,
            threatFamily = null,
            avDetections = null,
            totalAvScanned = null,
            communityReports = null,
            virustotalLink = null,
            evidence = emptyList(),
            recommendation = "ALLOW"
        )

        val result = verifier.verify(APKContext(file), emptyList())

        assertEquals(Verdict.SAFE, result.verdict)
        assertEquals(0.95f, result.confidence, 0.01f)
        file.delete()
    }

    @Test
    fun cloudDisabled_repoNotCalled() = runTest {
        whenever(prefs.cloudVerificationEnabled).thenReturn(false)
        val file = File.createTempFile("apk", ".apk").apply { writeBytes(ByteArray(100)) }
        val v = CloudVerifier(cloudRepo, 34, "en-US", prefs)

        val result = v.verify(APKContext(file), emptyList())

        assertEquals(Verdict.UNKNOWN, result.verdict)
        assertEquals(0, cloudRepo.callCount.get())
        file.delete()
    }

    @Test
    fun cloudOnboardingAcknowledgedFalse_repoNotCalled() = runTest {
        whenever(prefs.privacyOnboardingAcknowledged).thenReturn(false)
        whenever(prefs.cloudVerificationEnabled).thenReturn(true)

        val file = File.createTempFile("apk", ".apk").apply { writeBytes(ByteArray(100)) }
        val v = CloudVerifier(cloudRepo, 34, "en-US", prefs)

        val result = v.verify(APKContext(file), emptyList())

        assertEquals(Verdict.UNKNOWN, result.verdict)
        assertEquals(0, cloudRepo.callCount.get())
        assert(result.evidence.firstOrNull()?.contains("paused", ignoreCase = true) == true)
        file.delete()
    }

    // -- Phase 3.4 --------------------------------------------------------------------

    @Test
    fun playIntegrityVerdict_isAppendedToCloudEvidenceOnSuccess() = runTest {
        val recordingChecker = RecordingPlayIntegrityChecker(
            verdict = PlayIntegrityVerdict(
                device = PlayIntegrityVerdict.DeviceIntegrityLevel.STRONG,
                app = PlayIntegrityVerdict.AppIntegrityLevel.PLAY_RECOGNIZED,
                account = PlayIntegrityVerdict.AccountIntegrityLevel.LICENSED,
                source = PlayIntegrityVerdict.Source.PLAY_INTEGRITY_API
            )
        )
        val v = CloudVerifier(cloudRepo, 34, "en-US", prefs, recordingChecker)
        val file = File.createTempFile("apk", ".apk").apply { writeBytes(ByteArray(100)) }
        cloudRepo.response = CloudVerificationResponse(
            verdict = "SAFE",
            confidence = 0.95f,
            threatName = null,
            threatFamily = null,
            avDetections = null,
            totalAvScanned = null,
            communityReports = null,
            virustotalLink = null,
            evidence = listOf("Cloud: SAFE"),
            recommendation = "ALLOW"
        )

        val result = v.verify(APKContext(file), emptyList())

        // The checker was called exactly once with the SHA-256 of the scanned APK.
        assertEquals(1, recordingChecker.callCount.get())
        assertTrue(
            "request hash should be 64 hex chars (SHA-256), got '${recordingChecker.lastContext?.requestHash}'",
            recordingChecker.lastContext?.requestHash?.length == 64
        )
        // Cloud verdict is preserved.
        assertEquals(Verdict.SAFE, result.verdict)
        // PlayIntegrity evidence line is appended after the cloud's own evidence.
        val integrityLine = result.evidence.find { it.startsWith("PlayIntegrity:") }
        assertTrue("integrity line missing from $result", integrityLine != null)
        assertTrue(integrityLine!!.contains("device=STRONG"))
        assertTrue(integrityLine.contains("app=PLAY_RECOGNIZED"))
        assertTrue(integrityLine.contains("source=PLAY_INTEGRITY_API"))
        file.delete()
    }

    @Test
    fun playIntegrityCheckerThrows_isReportedAsErrorVerdict_doesNotCrashLayer() = runTest {
        val explosiveChecker = object : PlayIntegrityChecker {
            override suspend fun check(scanContext: ScanIntegrityContext): PlayIntegrityVerdict {
                throw IllegalStateException("checker had a bad day")
            }
        }
        val v = CloudVerifier(cloudRepo, 34, "en-US", prefs, explosiveChecker)
        val file = File.createTempFile("apk", ".apk").apply { writeBytes(ByteArray(100)) }
        cloudRepo.response = CloudVerificationResponse(
            verdict = "SAFE",
            confidence = 0.9f,
            threatName = null,
            threatFamily = null,
            avDetections = null,
            totalAvScanned = null,
            communityReports = null,
            virustotalLink = null,
            evidence = emptyList(),
            recommendation = "ALLOW"
        )

        val result = v.verify(APKContext(file), emptyList())

        // Layer still produces a verdict despite the checker exploding.
        assertEquals(Verdict.SAFE, result.verdict)
        // Evidence captures the error path.
        val integrityLine = result.evidence.find { it.startsWith("PlayIntegrity:") }
        assertTrue("integrity line missing from $result", integrityLine != null)
        assertTrue(integrityLine!!.contains("source=PLAY_INTEGRITY_API_ERROR"))
        file.delete()
    }

    @Test
    fun defaultConstructor_usesNoOpChecker_andEmitsDisabledEvidence() = runTest {
        val v = CloudVerifier(cloudRepo, 34, "en-US", prefs)
        val file = File.createTempFile("apk", ".apk").apply { writeBytes(ByteArray(100)) }
        cloudRepo.response = CloudVerificationResponse(
            verdict = "SAFE",
            confidence = 0.9f,
            threatName = null,
            threatFamily = null,
            avDetections = null,
            totalAvScanned = null,
            communityReports = null,
            virustotalLink = null,
            evidence = emptyList(),
            recommendation = "ALLOW"
        )

        val result = v.verify(APKContext(file), emptyList())

        val integrityLine = result.evidence.find { it.startsWith("PlayIntegrity:") }
        assertTrue("integrity line missing from $result", integrityLine != null)
        assertTrue(integrityLine!!.contains("source=DISABLED"))
        file.delete()
    }
}

/**
 * Capturing fake — lets the test assert on what the verifier passed to the integrity
 * checker (specifically the request hash, which must equal the APK SHA-256).
 */
private class RecordingPlayIntegrityChecker(
    private val verdict: PlayIntegrityVerdict
) : PlayIntegrityChecker {
    val callCount = AtomicInteger(0)
    @Volatile var lastContext: ScanIntegrityContext? = null

    override suspend fun check(scanContext: ScanIntegrityContext): PlayIntegrityVerdict {
        callCount.incrementAndGet()
        lastContext = scanContext
        return verdict
    }
}
