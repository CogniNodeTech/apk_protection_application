package com.safeguard.core.orchestration

import com.safeguard.core.domain.model.Action
import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ZeroTrustDecisionEngineTest {

    private lateinit var engine: ZeroTrustDecisionEngine

    @Before
    fun setUp() {
        engine = ZeroTrustDecisionEngine()
    }

    private fun layer(id: Int, name: String, verdict: Verdict, confidence: Float = 0.9f, riskScore: Int = 50): LayerResult =
        object : LayerResult {
            override val layerId = id
            override val layerName = name
            override val verdict = verdict
            override val confidence = confidence
            override val riskScore = riskScore
            override val evidence = emptyList<String>()
            override val executionTimeMs = 0L
        }

    @Test
    fun emptyLayerResults_returnsSuspiciousWarn() {
        val result = engine.calculateFinalVerdict(emptyList())
        assertEquals(Verdict.SUSPICIOUS, result.verdict)
        assertEquals(Action.WARN, result.recommendation)
    }

    @Test
    fun rule1_highConfidenceMalicious_returnsBlock() {
        val layers = listOf(
            layer(1, "L1", Verdict.SAFE),
            layer(2, "L2", Verdict.MALICIOUS, confidence = 0.90f)
        )
        val result = engine.calculateFinalVerdict(layers)
        assertEquals(Verdict.MALICIOUS, result.verdict)
        assertEquals(Action.BLOCK, result.recommendation)
    }

    @Test
    fun rule1_maliciousBelowThreshold_doesNotTriggerRule1() {
        val layers = listOf(
            layer(1, "L1", Verdict.MALICIOUS, confidence = 0.80f)
        )
        val result = engine.calculateFinalVerdict(layers)
        assertEquals(Verdict.SUSPICIOUS, result.verdict)
        assertEquals(Action.WARN, result.recommendation)
    }

    @Test
    fun rule2_twoMaliciousHighConfidence_returnsBlock() {
        val layers = listOf(
            layer(1, "L1", Verdict.MALICIOUS, confidence = 0.90f),
            layer(2, "L2", Verdict.MALICIOUS, confidence = 0.88f)
        )
        val result = engine.calculateFinalVerdict(layers)
        assertEquals(Verdict.MALICIOUS, result.verdict)
        assertEquals(Action.BLOCK, result.recommendation)
    }

    @Test
    fun rule2_twoMaliciousLowConfidence_returnsBlock() {
        val layers = listOf(
            layer(1, "L1", Verdict.MALICIOUS, confidence = 0.80f),
            layer(2, "L2", Verdict.MALICIOUS, confidence = 0.75f)
        )
        val result = engine.calculateFinalVerdict(layers)
        assertEquals(Verdict.MALICIOUS, result.verdict)
        assertEquals(Action.BLOCK, result.recommendation)
    }

    @Test
    fun rule3_threeSuspicious_returnsBlock() {
        val layers = listOf(
            layer(1, "L1", Verdict.SUSPICIOUS),
            layer(2, "L2", Verdict.SUSPICIOUS),
            layer(3, "L3", Verdict.SUSPICIOUS)
        )
        val result = engine.calculateFinalVerdict(layers)
        assertEquals(Verdict.MALICIOUS, result.verdict)
        assertEquals(Action.BLOCK, result.recommendation)
    }

    @Test
    fun rule4_anyMalicious_returnsSuspiciousWarn() {
        val layers = listOf(
            layer(1, "L1", Verdict.SAFE),
            layer(2, "L2", Verdict.MALICIOUS, confidence = 0.80f)
        )
        val result = engine.calculateFinalVerdict(layers)
        assertEquals(Verdict.SUSPICIOUS, result.verdict)
        assertEquals(Action.WARN, result.recommendation)
    }

    @Test
    fun rule5_anyUnknown_returnsSuspiciousWarn() {
        val layers = listOf(
            layer(1, "L1", Verdict.SAFE),
            layer(2, "L2", Verdict.UNKNOWN)
        )
        val result = engine.calculateFinalVerdict(layers)
        assertEquals(Verdict.SUSPICIOUS, result.verdict)
        assertEquals(Action.WARN, result.recommendation)
    }

    @Test
    fun rule6_allSafeHighConfidence_returnsAllow() {
        val layers = listOf(
            layer(1, "L1", Verdict.SAFE, confidence = 0.85f),
            layer(2, "L2", Verdict.SAFE, confidence = 0.90f)
        )
        val result = engine.calculateFinalVerdict(layers)
        assertEquals(Verdict.SAFE, result.verdict)
        assertEquals(Action.ALLOW, result.recommendation)
    }

    @Test
    fun rule6_allSafeLowConfidence_returnsDefaultSuspicious() {
        val layers = listOf(
            layer(1, "L1", Verdict.SAFE, confidence = 0.65f),
            layer(2, "L2", Verdict.SAFE, confidence = 0.60f)
        )
        val result = engine.calculateFinalVerdict(layers)
        assertEquals(Verdict.SUSPICIOUS, result.verdict)
        assertEquals(Action.WARN, result.recommendation)
    }

    @Test
    fun default_inconclusive_returnsSuspiciousWarn() {
        val layers = listOf(
            layer(1, "L1", Verdict.SAFE, confidence = 0.50f),
            layer(2, "L2", Verdict.SAFE, confidence = 0.55f)
        )
        val result = engine.calculateFinalVerdict(layers)
        assertEquals(Verdict.SUSPICIOUS, result.verdict)
        assertEquals(Action.WARN, result.recommendation)
    }
}
