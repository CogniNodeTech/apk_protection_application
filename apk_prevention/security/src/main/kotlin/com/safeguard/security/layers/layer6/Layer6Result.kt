package com.safeguard.security.layers.layer6

import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.ThreatInfo
import com.safeguard.core.domain.model.Verdict

data class Layer6Result(
    override val layerId: Int = 6,
    override val layerName: String = "Cloud Verification",
    override val verdict: Verdict,
    override val confidence: Float,
    override val riskScore: Int,
    val avDetections: Int?,
    val totalAvScanned: Int?,
    val communityReports: Int?,
    val threatName: String?,
    override val evidence: List<String>,
    override val executionTimeMs: Long = 0,
    override val threatInfo: ThreatInfo? = null
) : LayerResult
