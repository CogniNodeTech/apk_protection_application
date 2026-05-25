package com.safeguard.security.layers.layer5

import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.Verdict

data class Layer5Result(
    override val layerId: Int = 5,
    override val layerName: String = "ML Behavioral Analyzer",
    override val verdict: Verdict,
    override val confidence: Float,
    override val riskScore: Int,
    val malwareProbability: Float,
    val topFeatures: List<FeatureContribution>,
    override val evidence: List<String>,
    override val executionTimeMs: Long = 0
) : LayerResult

data class FeatureContribution(
    val featureName: String,
    val value: Float,
    val weight: Float,
    val explanation: String
)
