package com.safeguard.core.orchestration

import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.Verdict

/**
 * Aggregates risk score (0-100) and evidence from all layer results.
 */
class RiskAssessmentEngine {

    fun calculateOverallRiskScore(layerResults: List<LayerResult>): Int {
        if (layerResults.isEmpty()) return 0
        val weightedAverage = layerResults.map { layer ->
            val weight = when (layer.layerId) {
                2 -> 1.25 // hash intel
                4 -> 1.25 // signature trust
                5 -> 1.15 // ML behavioral
                6 -> 1.1  // cloud intelligence
                else -> 1.0
            }
            layer.riskScore * weight
        }.sum() / layerResults.size.toDouble()

        val maxRisk = layerResults.maxOf { it.riskScore }
        val suspiciousBonus = layerResults.count { it.verdict == Verdict.SUSPICIOUS } * 3
        val maliciousBonus = layerResults.count { it.verdict == Verdict.MALICIOUS } * 7
        val consensusRisk = ((weightedAverage * 0.7) + (maxRisk * 0.3)).toInt() + suspiciousBonus + maliciousBonus
        return consensusRisk.coerceIn(0, 100)
    }

    fun aggregateEvidence(layerResults: List<LayerResult>): List<String> {
        return layerResults
            .flatMap { it.evidence }
            .distinct()
    }
}
