package com.safeguard.core.orchestration

import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RiskAssessmentEngineTest {

    private lateinit var engine: RiskAssessmentEngine

    @Before
    fun setUp() {
        engine = RiskAssessmentEngine()
    }

    private fun layer(id: Int, riskScore: Int, evidence: List<String> = emptyList()): LayerResult =
        object : LayerResult {
            override val layerId = id
            override val layerName = "Layer$id"
            override val verdict = Verdict.SAFE
            override val confidence = 0.9f
            override val riskScore = riskScore
            override val evidence = evidence
            override val executionTimeMs = 0L
        }

    @Test
    fun emptyLayerResults_returnsZero() {
        assertEquals(0, engine.calculateOverallRiskScore(emptyList()))
    }

    @Test
    fun singleLayer_returnsThatLayerRisk() {
        val layers = listOf(layer(1, 75))
        assertEquals(75, engine.calculateOverallRiskScore(layers))
    }

    @Test
    fun multipleLayers_returnsConsensusWeightedRisk() {
        val layers = listOf(
            layer(1, 20),
            layer(2, 90),
            layer(3, 45)
        )
        assertEquals(68, engine.calculateOverallRiskScore(layers))
    }

    @Test
    fun riskAbove100_coercedTo100() {
        val layers = listOf(layer(1, 150))
        assertEquals(100, engine.calculateOverallRiskScore(layers))
    }

    @Test
    fun riskBelowZero_coercedTo0() {
        val layers = listOf(layer(1, -10))
        assertEquals(0, engine.calculateOverallRiskScore(layers))
    }

    @Test
    fun aggregateEvidence_combinesAndDedupes() {
        val layers = listOf(
            layer(1, 50, evidence = listOf("a", "b")),
            layer(2, 50, evidence = listOf("b", "c"))
        )
        val evidence = engine.aggregateEvidence(layers)
        assertEquals(listOf("a", "b", "c"), evidence)
    }

    @Test
    fun aggregateEvidence_emptyLayers_returnsEmpty() {
        assertEquals(emptyList<String>(), engine.aggregateEvidence(emptyList()))
    }
}
