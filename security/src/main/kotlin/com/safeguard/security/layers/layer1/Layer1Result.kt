package com.safeguard.security.layers.layer1

import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.Verdict

data class Layer1Result(
    override val layerId: Int = 1,
    override val layerName: String = "File System Monitor",
    override val verdict: Verdict,
    override val confidence: Float,
    override val riskScore: Int,
    val sourceLocation: String,
    val fileNameRisk: Int,
    val fileSizeRisk: Int,
    override val evidence: List<String>,
    override val executionTimeMs: Long = 0
) : LayerResult
