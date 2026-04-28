package com.safeguard.core.domain.model

/**
 * Complete scan result for one APK (output of scan orchestration).
 */
data class ScanResult(
    val id: String,
    val apkPath: String,
    val apkName: String,
    val apkSizeBytes: Long,
    val scanTimestamp: Long,
    val finalVerdict: Verdict,
    val overallConfidence: Float,
    val overallRiskScore: Int,
    val layerResults: List<LayerResult>,
    val aggregatedEvidence: List<String>,
    val recommendedAction: Action,
    val userDecision: UserDecision? = null,
    val threatInfo: ThreatInfo? = null,
    val installerSource: String? = null
)
