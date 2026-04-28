package com.safeguard.core.orchestration

import com.safeguard.core.domain.model.Action
import com.safeguard.core.domain.model.MalwareCategory
import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Test

class ForensicReasoningEngineTest {

    private val engine = ForensicReasoningEngine()

    @Test
    fun maliciousVerdict_emptyHeuristics_returnsUnspecifiedCategory() {
        val result = minimalScan(
            finalVerdict = Verdict.MALICIOUS,
            layerResults = emptyList(),
            installerSource = "Google Play Store"
        )
        val analysis = engine.analyze(result)
        assertEquals(MalwareCategory.UNSPECIFIED, analysis.category)
    }

    @Test
    fun safeVerdict_returnsCleanCategory() {
        val result = minimalScan(
            finalVerdict = Verdict.SAFE,
            layerResults = emptyList(),
            installerSource = "Google Play Store"
        )
        val analysis = engine.analyze(result)
        assertEquals(MalwareCategory.CLEAN, analysis.category)
    }

    private fun minimalScan(
        finalVerdict: Verdict,
        layerResults: List<com.safeguard.core.domain.model.LayerResult>,
        installerSource: String?
    ): ScanResult = ScanResult(
        id = "test-id",
        apkPath = "/tmp/test.apk",
        apkName = "test.apk",
        apkSizeBytes = 1L,
        scanTimestamp = 0L,
        finalVerdict = finalVerdict,
        overallConfidence = 0.5f,
        overallRiskScore = 50,
        layerResults = layerResults,
        aggregatedEvidence = emptyList(),
        recommendedAction = Action.WARN,
        installerSource = installerSource
    )
}
