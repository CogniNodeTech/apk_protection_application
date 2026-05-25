package com.safeguard.core.orchestration

import com.safeguard.core.domain.layer.ProtectionLayer
import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.Action
import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.model.ThreatInfo
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.security.DeviceIntegrityProvider
import com.safeguard.core.util.SafeApkReader
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.UUID

/**
 * Coordinates execution of all 6 protection layers and applies zero-trust decision.
 */
class ScanOrchestrator(
    private val layers: List<ProtectionLayer>,
    private val decisionEngine: ZeroTrustDecisionEngine,
    private val riskEngine: RiskAssessmentEngine,
    private val integrityProvider: DeviceIntegrityProvider,
    private val forensicEngine: ForensicReasoningEngine
) {

    suspend fun scan(apkFile: File, displayName: String? = null): ScanResult {
        // Use a timeout to prevent scanning a malformed APK forever
        return withTimeoutOrNull(30_000L) {
            internalScan(apkFile, displayName)
        } ?: createTimeoutResult(apkFile, displayName)
    }

    private suspend fun internalScan(apkFile: File, displayName: String? = null): ScanResult {
        val orchestratorStart = System.currentTimeMillis()
        if (!apkFile.exists() || !apkFile.isFile) {
            throw IllegalArgumentException("APK file does not exist or is not a file: ${apkFile.absolutePath}")
        }
        
        // 1. RASP Environment Check
        val integrityStatus = integrityProvider.checkSecurityStatus()
        
        val context = APKContext(apkFile).apply {
            putCached(APKContext.KEY_RASP_THREAT, integrityStatus.threatLevel)
            putCached("is_rooted", integrityStatus.isRooted)
            
            // Pre-calculate SHA-256 once for all layers
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val hash = apkFile.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } > 0) md.update(buffer, 0, read)
                md.digest().joinToString("") { "%02x".format(it) }
            }
            putCached(APKContext.KEY_SHA256, hash)
        }

        val apkName = displayName?.takeIf { it.isNotBlank() } ?: context.apkName
        val installerSource = context.getCached<String>(APKContext.KEY_INSTALLER_SOURCE) ?: "Unknown"
        val layerResults = mutableListOf<LayerResult>()
        val slowLayers = mutableListOf<String>()

        for (layer in layers) {
            val layerStart = System.currentTimeMillis()
            
            // Pass the unified context instead of the raw File
            val result = try {
                layer.verify(context, layerResults.toList())
            } catch (e: Exception) {
                 createErrorLayerResult(layer, e)
            }
            
            val layerTime = System.currentTimeMillis() - layerStart
            layerResults.add(withExecutionTime(result, layerTime))
            if (layerTime > 2_000) {
                slowLayers.add("${result.layerName}:${layerTime}ms")
            }
            // Early-stop on definitive malware verdicts to reduce exposure window and scan latency.
            val definitiveMalware = result.verdict == Verdict.MALICIOUS && result.confidence >= 0.95f
            if (definitiveMalware) break
        }

        val calculatedFinalVerdict = decisionEngine.calculateFinalVerdict(layerResults)
        val integrityOverride = integrityVerdictOverride(integrityStatus)
        val finalVerdict = integrityOverride?.finalVerdict ?: calculatedFinalVerdict

        var overallRisk = riskEngine.calculateOverallRiskScore(layerResults)
        if (integrityOverride != null) {
            overallRisk = maxOf(overallRisk, integrityOverride.riskFloor.coerceIn(0, 100))
        }

        val evidence = riskEngine.aggregateEvidence(layerResults).toMutableList().apply {
            context.getCached<String>(APKContext.KEY_SHA256)?.let { add("apk_sha256=$it") }
            add("apk_size_bytes=${apkFile.length()}")
            add("layers_executed=${layerResults.size}")
            add("total_scan_time_ms=${System.currentTimeMillis() - orchestratorStart}")
            if (slowLayers.isNotEmpty()) add("slow_layers=${slowLayers.joinToString(";")}")
            if (integrityStatus.threatLevel != DeviceIntegrityProvider.ThreatLevel.NONE) {
                add("rasp_threat_level=${integrityStatus.threatLevel}")
                if (integrityStatus.detectedThreats.isNotEmpty()) {
                    add("rasp_threats=${integrityStatus.detectedThreats.joinToString("|")}")
                }
            }
        }

        val recommendedAction = when (finalVerdict.recommendation) {
            Action.BLOCK -> Action.QUARANTINE
            else -> finalVerdict.recommendation
        }

        val rawThreatInfo = layerResults.lastOrNull { it.threatInfo != null }?.threatInfo

        val provisionalResult = ScanResult(
            id = UUID.randomUUID().toString(),
            apkPath = apkFile.absolutePath,
            apkName = apkName,
            apkSizeBytes = apkFile.length(),
            scanTimestamp = System.currentTimeMillis(),
            finalVerdict = finalVerdict.verdict,
            overallConfidence = finalVerdict.confidence,
            overallRiskScore = overallRisk,
            layerResults = layerResults,
            aggregatedEvidence = evidence,
            recommendedAction = recommendedAction,
            userDecision = null,
            threatInfo = rawThreatInfo,
            installerSource = installerSource
        )

        // Final Deep Forensics Sync
        val forensicReport = forensicEngine.analyze(provisionalResult)

        return provisionalResult.copy(
            overallConfidence = (provisionalResult.overallConfidence + forensicReport.confidenceAdjustment).coerceIn(0f, 1f),
            threatInfo = (rawThreatInfo ?: ThreatInfo(null, null, (overallRisk / 10).coerceIn(0, 10), null, null, null)).copy(
                category = forensicReport.category,
                technicalReasoning = forensicReport.reasoning
            )
        )
    }

    private fun integrityVerdictOverride(
        integrityStatus: DeviceIntegrityProvider.SecurityStatus
    ): IntegrityOverride? {
        return when (integrityStatus.threatLevel) {
            DeviceIntegrityProvider.ThreatLevel.CRITICAL -> IntegrityOverride(
                finalVerdict = FinalVerdict(
                    verdict = Verdict.MALICIOUS,
                    confidence = 1.0f,
                    recommendation = Action.BLOCK,
                    reason = "RASP device integrity: CRITICAL threats detected"
                ),
                riskFloor = 100
            )
            DeviceIntegrityProvider.ThreatLevel.HIGH -> IntegrityOverride(
                finalVerdict = FinalVerdict(
                    verdict = Verdict.MALICIOUS,
                    confidence = 0.9f,
                    recommendation = Action.BLOCK,
                    reason = "RASP device integrity: HIGH threats detected"
                ),
                riskFloor = 85
            )
            DeviceIntegrityProvider.ThreatLevel.MEDIUM -> IntegrityOverride(
                finalVerdict = FinalVerdict(
                    verdict = Verdict.SUSPICIOUS,
                    confidence = 0.75f,
                    recommendation = Action.WARN,
                    reason = "RASP device integrity: MEDIUM threats detected"
                ),
                riskFloor = 55
            )
            DeviceIntegrityProvider.ThreatLevel.LOW,
            DeviceIntegrityProvider.ThreatLevel.NONE -> null
        }
    }

    private data class IntegrityOverride(
        val finalVerdict: FinalVerdict,
        val riskFloor: Int
    )

    private fun createTimeoutResult(apkFile: File, displayName: String?): ScanResult {
        return ScanResult(
            id = UUID.randomUUID().toString(),
            apkPath = apkFile.absolutePath,
            apkName = displayName ?: apkFile.name,
            apkSizeBytes = apkFile.length(),
            scanTimestamp = System.currentTimeMillis(),
            finalVerdict = Verdict.SUSPICIOUS,
            overallConfidence = 0.3f,
            overallRiskScore = 40,
            layerResults = emptyList(),
            aggregatedEvidence = listOf("Scan timed out after 30s - file might be malformed or overly complex"),
            recommendedAction = Action.WARN,
            userDecision = null,
            threatInfo = null,
            installerSource = "Unknown"
        )
    }

    private fun createErrorLayerResult(layer: ProtectionLayer, e: Exception): LayerResult {
        return object : LayerResult {
            override val layerId = layer.toString().hashCode() // Fallback ID
            override val layerName = "Unknown Layer (Error)"
            override val verdict = Verdict.UNKNOWN
            override val confidence = 0f
            override val riskScore = 0
            override val evidence = listOf("Layer failed with exception: ${e.message}")
            override val executionTimeMs = 0L
            override val threatInfo: ThreatInfo? = null
        }
    }

    private fun withExecutionTime(result: LayerResult, timeMs: Long): LayerResult {
        return object : LayerResult {
            override val layerId = result.layerId
            override val layerName = result.layerName
            override val verdict = result.verdict
            override val confidence = result.confidence
            override val riskScore = result.riskScore
            override val evidence = result.evidence
            override val executionTimeMs = timeMs
            override val threatInfo: ThreatInfo? = result.threatInfo
        }
    }
}
