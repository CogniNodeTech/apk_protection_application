package com.safeguard.core.domain.model

/**
 * Threat intelligence details (e.g. from cloud or hash DB).
 */
data class ThreatInfo(
    val threatName: String?,
    val threatFamily: String?,
    val severity: Int,
    val avDetections: Int?,
    val totalAvScanned: Int?,
    val communityReports: Int?,
    val category: MalwareCategory = MalwareCategory.UNSPECIFIED,
    val technicalReasoning: List<String> = emptyList()
)
