package com.safeguard.core.orchestration

import com.safeguard.core.domain.layer.ProtectionLayer
import com.safeguard.core.domain.model.Action
import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.model.ThreatInfo
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.security.DeviceIntegrityProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

private class FakeIntegrityProvider : DeviceIntegrityProvider {
    override fun checkSecurityStatus(): DeviceIntegrityProvider.SecurityStatus {
        return DeviceIntegrityProvider.SecurityStatus(
            threatLevel = DeviceIntegrityProvider.ThreatLevel.NONE,
            detectedThreats = emptyList(),
            isRooted = false,
            isHooked = false,
            isDebuggerAttached = false,
            isTampered = false
        )
    }
}

private class FakeLayer(private val layerId: Int, private val verdict: Verdict, private val confidence: Float) :
    ProtectionLayer {
    override suspend fun verify(context: APKContext, previousLayerResults: List<LayerResult>): LayerResult {
        return object : LayerResult {
            override val layerId: Int = this@FakeLayer.layerId
            override val layerName: String = "Fake Layer ${this@FakeLayer.layerId}"
            override val verdict: Verdict = this@FakeLayer.verdict
            override val confidence: Float = this@FakeLayer.confidence
            override val riskScore: Int = when (this@FakeLayer.verdict) {
                Verdict.MALICIOUS -> 80
                Verdict.SUSPICIOUS -> 50
                Verdict.UNKNOWN -> 30
                Verdict.SAFE -> 0
            }
            override val evidence: List<String> = emptyList()
            override val executionTimeMs: Long = 0L
            override val threatInfo: ThreatInfo? = null
        }
    }
}

class ScanOrchestratorIntegrationTest {

    private fun tempApkFile(): File =
        File.createTempFile("safeguard-test", ".apk").apply { writeBytes(ByteArray(128)) }

    @Test
    fun scan_orchestrator_layer6Malicious_triggersFinalMalicious_quarantine() {
        val layers = listOf(
            FakeLayer(1, Verdict.SAFE, 0.9f),
            FakeLayer(2, Verdict.SAFE, 0.9f),
            FakeLayer(3, Verdict.SAFE, 0.9f),
            FakeLayer(4, Verdict.SAFE, 0.9f),
            FakeLayer(5, Verdict.SAFE, 0.9f),
            FakeLayer(6, Verdict.MALICIOUS, 0.9f), // simulates cloud verify hit
        )
        val orchestrator = ScanOrchestrator(
            layers = layers,
            decisionEngine = ZeroTrustDecisionEngine(),
            riskEngine = RiskAssessmentEngine(),
            integrityProvider = FakeIntegrityProvider(),
            forensicEngine = ForensicReasoningEngine()
        )

        val result = runBlocking { orchestrator.scan(tempApkFile()) }
        assertEquals(Verdict.MALICIOUS, result.finalVerdict)
        assertEquals(Action.QUARANTINE, result.recommendedAction)
        assertTrue(result.overallConfidence > 0.7f)
    }

    @Test
    fun scan_orchestrator_layer6Unknown_degradesToSuspicious_warn() {
        val layers = listOf(
            FakeLayer(1, Verdict.SAFE, 0.9f),
            FakeLayer(2, Verdict.SAFE, 0.9f),
            FakeLayer(3, Verdict.SAFE, 0.9f),
            FakeLayer(4, Verdict.SAFE, 0.9f),
            FakeLayer(5, Verdict.SAFE, 0.9f),
            FakeLayer(6, Verdict.UNKNOWN, 0.4f), // simulates cloud miss/error path
        )
        val orchestrator = ScanOrchestrator(
            layers = layers,
            decisionEngine = ZeroTrustDecisionEngine(),
            riskEngine = RiskAssessmentEngine(),
            integrityProvider = FakeIntegrityProvider(),
            forensicEngine = ForensicReasoningEngine()
        )

        val result = runBlocking { orchestrator.scan(tempApkFile()) }
        assertEquals(Verdict.SUSPICIOUS, result.finalVerdict)
        assertEquals(Action.WARN, result.recommendedAction)
    }
}

