package com.safeguard.core.domain.model

/**
 * Result from a single protection layer (base for all 6 layers).
 */
interface LayerResult {
    val layerId: Int
    val layerName: String
    val verdict: Verdict
    val confidence: Float
    val riskScore: Int
    val evidence: List<String>
    val executionTimeMs: Long
    val threatInfo: ThreatInfo? get() = null
}
