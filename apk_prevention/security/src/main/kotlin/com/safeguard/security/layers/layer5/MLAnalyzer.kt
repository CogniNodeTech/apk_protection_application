package com.safeguard.security.layers.layer5

import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.layer.ProtectionLayer
import com.safeguard.core.domain.model.LayerResult
import com.safeguard.mlmodel.FeatureExtractor
import com.safeguard.mlmodel.TFLiteRunner
import java.io.File
import java.util.Locale

/** Expected feature vector size for the bundled TFLite model; must match FeatureExtractor and model input. */
private const val EXPECTED_FEATURE_SIZE = 50

class MLAnalyzer(
    private val featureExtractor: FeatureExtractor,
    private val tfliteRunner: TFLiteRunner
) : ProtectionLayer {

    override suspend fun verify(context: APKContext, previousLayerResults: List<LayerResult>): Layer5Result {
        val start = System.currentTimeMillis()
        val apkFile = context.apkFile
        val features = featureExtractor.extract(apkFile)
        val topFeatures = mutableListOf<FeatureContribution>()
        val output = if (features.size == EXPECTED_FEATURE_SIZE) tfliteRunner.run(features) else null
        val malwareProb = if (output != null && output.size >= 2) {
            output[1].also { p ->
                topFeatures.add(FeatureContribution("ml_output", p, 1f, "Model probability"))
            }
        } else {
            heuristicMalwareScore(features).also { p ->
                topFeatures.add(FeatureContribution("heuristic", p, 0.8f, "No TFLite model - using heuristic"))
            }
        }
        val verdict = when {
            malwareProb >= 0.75f -> Verdict.MALICIOUS
            malwareProb >= 0.40f -> Verdict.SUSPICIOUS
            else -> Verdict.SAFE
        }
        val riskScore = (malwareProb * 100).toInt().coerceIn(0, 100)
        val confidence = when (verdict) {
            Verdict.SAFE -> (1f - malwareProb).coerceIn(0f, 1f)
            else -> malwareProb
        }
        val evidence = mutableListOf<String>()
        val analysisMode = if (output != null && output.size >= 2) "TFLite on-device model (${features.size} features)" else "Heuristic fallback (model missing or wrong input size)"
        evidence.add(analysisMode)
        evidence.add(String.format(Locale.US, "Malware probability: %.1f%% (0–100%% scale)", malwareProb * 100))
        if (malwareProb >= 0.75f) evidence.add("Threshold: ≥75% → classified as malicious (high confidence).")
        else if (malwareProb >= 0.40f) evidence.add("Threshold: 40–75% → suspicious; review permissions and source.")
        if (malwareProb >= 0.4f) evidence.add("Elevated on-device behavioral risk score from static/dynamic feature vector.")
        val time = System.currentTimeMillis() - start
        return Layer5Result(
            verdict = verdict,
            confidence = confidence,
            riskScore = riskScore,
            malwareProbability = malwareProb,
            topFeatures = topFeatures,
            evidence = evidence,
            executionTimeMs = time
        )
    }

    private fun heuristicMalwareScore(features: FloatArray): Float {
        var score = 0.5f
        if (features.size > 8) {
            if (features.getOrNull(4) ?: 0f > 0.5f) score += 0.15f
            if (features.getOrNull(8) ?: 0f > 0.8f) score += 0.1f
            if (features.getOrNull(16) ?: 0f > 0.5f) score += 0.2f
            if (features.getOrNull(17) ?: 0f > 0f) score += 0.25f
        }
        return score.coerceIn(0f, 1f)
    }
}
