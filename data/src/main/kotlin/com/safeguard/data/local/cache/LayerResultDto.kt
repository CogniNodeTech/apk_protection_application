package com.safeguard.data.local.cache

import com.squareup.moshi.Json

/**
 * DTO for serializing LayerResult to JSON in DB.
 */
data class LayerResultDto(
    @Json(name = "layerId") val layerId: Int,
    @Json(name = "layerName") val layerName: String,
    @Json(name = "verdict") val verdict: String,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "riskScore") val riskScore: Int,
    @Json(name = "evidence") val evidence: List<String>,
    @Json(name = "executionTimeMs") val executionTimeMs: Long,
    @Json(name = "threatName") val threatName: String? = null,
    @Json(name = "threatFamily") val threatFamily: String? = null,
    @Json(name = "threatRiskScore") val threatRiskScore: Int? = null
)
