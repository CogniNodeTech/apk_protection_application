package com.safeguard.core.orchestration

import com.safeguard.core.domain.model.Action
import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.Verdict

/**
 * Zero-Trust decision engine: "Trust Nothing, Verify Everything".
 * Applies consensus rules to layer results to produce final verdict and action.
 */
class ZeroTrustDecisionEngine {

    fun calculateFinalVerdict(layerResults: List<LayerResult>): FinalVerdict {
        if (layerResults.isEmpty()) {
            return FinalVerdict(
                verdict = Verdict.SUSPICIOUS,
                confidence = 0f,
                recommendation = Action.WARN,
                reason = "No layer results available"
            )
        }

        // RULE 1: Any layer says MALICIOUS with >85% confidence → BLOCK (zero-trust spec)
        val highConfidenceMalicious = layerResults.any {
            it.verdict == Verdict.MALICIOUS && it.confidence > 0.85f
        }
        if (highConfidenceMalicious) {
            return FinalVerdict(
                verdict = Verdict.MALICIOUS,
                confidence = 1.0f,
                recommendation = Action.BLOCK,
                reason = "High-confidence malware detection"
            )
        }

        // RULE 2: 2+ layers say MALICIOUS (any confidence) → BLOCK (defense in depth)
        val maliciousCount = layerResults.count { it.verdict == Verdict.MALICIOUS }
        if (maliciousCount >= 2) {
            return FinalVerdict(
                verdict = Verdict.MALICIOUS,
                confidence = 0.95f,
                recommendation = Action.BLOCK,
                reason = "Multiple layers detected malware"
            )
        }

        // RULE 2b: Weighted malicious confidence across layers.
        val weightedThreatSignal = layerResults.sumOf { layer ->
            when (layer.verdict) {
                Verdict.MALICIOUS -> (0.6 + (layer.confidence * 0.4)).toDouble()
                Verdict.SUSPICIOUS -> (0.25 + (layer.confidence * 0.2)).toDouble()
                Verdict.UNKNOWN -> 0.15
                Verdict.SAFE -> 0.0
            }
        }
        if (weightedThreatSignal >= 1.8) {
            return FinalVerdict(
                verdict = Verdict.MALICIOUS,
                confidence = 0.85f,
                recommendation = Action.BLOCK,
                reason = "Weighted threat signal exceeded malicious threshold"
            )
        }

        // RULE 3: 3+ layers say SUSPICIOUS → Treat as MALICIOUS (zero-trust)
        val suspiciousCount = layerResults.count { it.verdict == Verdict.SUSPICIOUS }
        if (suspiciousCount >= 3) {
            return FinalVerdict(
                verdict = Verdict.MALICIOUS,
                confidence = 0.75f,
                recommendation = Action.BLOCK,
                reason = "Multiple suspicious indicators (zero-trust principle)"
            )
        }

        // RULE 4: ANY layer says MALICIOUS → SUSPICIOUS
        val anyMalicious = layerResults.any { it.verdict == Verdict.MALICIOUS }
        if (anyMalicious) {
            return FinalVerdict(
                verdict = Verdict.SUSPICIOUS,
                confidence = 0.65f,
                recommendation = Action.WARN,
                reason = "At least one layer detected threat"
            )
        }

        // RULE 5: ANY layer says UNKNOWN → SUSPICIOUS (zero-trust)
        val anyUnknown = layerResults.any { it.verdict == Verdict.UNKNOWN }
        if (anyUnknown) {
            return FinalVerdict(
                verdict = Verdict.SUSPICIOUS,
                confidence = 0.55f,
                recommendation = Action.WARN,
                reason = "Cannot fully verify - proceed with caution"
            )
        }

        // RULE 6: ALL layers say SAFE with high confidence → SAFE
        val allSafe = layerResults.all {
            it.verdict == Verdict.SAFE && it.confidence > 0.7f
        }
        if (allSafe) {
            val avgConfidence = layerResults.map { it.confidence }.average()
            return FinalVerdict(
                verdict = Verdict.SAFE,
                confidence = avgConfidence.toFloat(),
                recommendation = Action.ALLOW,
                reason = "Passed all security checks"
            )
        }

        // DEFAULT: Cannot determine → SUSPICIOUS (zero-trust)
        return FinalVerdict(
            verdict = Verdict.SUSPICIOUS,
            confidence = 0.40f,
            recommendation = Action.WARN,
            reason = "Inconclusive results - exercise caution"
        )
    }
}

data class FinalVerdict(
    val verdict: Verdict,
    val confidence: Float,
    val recommendation: Action,
    val reason: String
)
