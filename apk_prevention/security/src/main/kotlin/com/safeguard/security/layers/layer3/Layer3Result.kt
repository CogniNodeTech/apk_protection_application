package com.safeguard.security.layers.layer3

import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.Verdict

data class Layer3Result(
    override val layerId: Int = 3,
    override val layerName: String = "Permission Analyzer",
    override val verdict: Verdict,
    override val confidence: Float,
    override val riskScore: Int,
    override val evidence: List<String>,
    override val executionTimeMs: Long = 0,
    val permissionCount: Int = 0,
    val dangerousCombinations: List<String> = emptyList()
) : LayerResult
