package com.safeguard.core.benchmark

import com.safeguard.core.domain.model.Verdict

/**
 * Phase 3.3 detection-rate metrics. We model the problem as binary classification with
 * malicious as the positive class:
 *
 * | predicted \ actual | malicious | safe |
 * | malicious          | TP        | FP   |
 * | safe               | FN        | TN   |
 *
 * SUSPICIOUS and UNKNOWN predictions are treated as "abstain" — counted but not folded
 * into TP/TN/FP/FN. We report them under [abstainPositive] / [abstainNegative] so
 * curators can decide whether their corpus is being underserved by the engine vs.
 * truly genuinely-uncertain.
 *
 * Why not collapse SUSPICIOUS into "predicted malicious"? Doing so would inflate recall
 * for free at the cost of precision and obscure the question "is the engine confidently
 * right when it commits". Keeping them separate forces honest accounting.
 */
data class ConfusionMatrix(
    val truePositives: Int,
    val falsePositives: Int,
    val trueNegatives: Int,
    val falseNegatives: Int,
    val abstainPositive: Int,
    val abstainNegative: Int
) {
    val total: Int get() = truePositives + falsePositives + trueNegatives + falseNegatives + abstainPositive + abstainNegative

    val committedTotal: Int get() = truePositives + falsePositives + trueNegatives + falseNegatives

    /** Fraction of correct decisive verdicts among all decisive verdicts. */
    val accuracyCommitted: Double
        get() = if (committedTotal == 0) 0.0 else (truePositives + trueNegatives).toDouble() / committedTotal

    /** Of everything we *flagged*, how much was actually bad. */
    val precision: Double
        get() = if (truePositives + falsePositives == 0) 0.0
        else truePositives.toDouble() / (truePositives + falsePositives)

    /**
     * Of everything that *was* bad, how much did we flag — abstaining counts as a miss
     * for recall, because from the user's perspective an "I'm not sure" verdict on a
     * banker is functionally identical to a false negative.
     */
    val recall: Double
        get() {
            val totalPositives = truePositives + falseNegatives + abstainPositive
            return if (totalPositives == 0) 0.0 else truePositives.toDouble() / totalPositives
        }

    val f1: Double
        get() = if (precision + recall == 0.0) 0.0 else 2.0 * precision * recall / (precision + recall)

    /** False-positive rate against the safe corpus. Abstains on safe samples are treated as benign. */
    val falsePositiveRate: Double
        get() {
            val totalNegatives = trueNegatives + falsePositives + abstainNegative
            return if (totalNegatives == 0) 0.0 else falsePositives.toDouble() / totalNegatives
        }
}

/**
 * Per-row tape produced by the harness. Useful both for the printed summary and for
 * external dashboards that want to slice by malware family / metadata column.
 */
data class BenchmarkPrediction(
    val sample: BenchmarkSample,
    val predicted: Verdict,
    val confidence: Float,
    val durationMs: Long,
    val notes: String? = null
)

/**
 * Pure-function metric calculator. Pulled out so the harness can be unit-tested without
 * spinning up a real scan oracle.
 */
object BenchmarkMetrics {

    fun confusionMatrix(predictions: List<BenchmarkPrediction>): ConfusionMatrix {
        var tp = 0; var fp = 0; var tn = 0; var fn = 0; var abPos = 0; var abNeg = 0
        for (pred in predictions) {
            val actual = pred.sample.expected
            when (pred.predicted) {
                Verdict.MALICIOUS -> if (actual == Verdict.MALICIOUS) tp++ else fp++
                Verdict.SAFE -> if (actual == Verdict.SAFE) tn++ else fn++
                Verdict.SUSPICIOUS, Verdict.UNKNOWN ->
                    if (actual == Verdict.MALICIOUS) abPos++ else abNeg++
            }
        }
        return ConfusionMatrix(
            truePositives = tp,
            falsePositives = fp,
            trueNegatives = tn,
            falseNegatives = fn,
            abstainPositive = abPos,
            abstainNegative = abNeg
        )
    }

    /**
     * Render a stable, terminal-friendly summary. We deliberately avoid any chart-rendering
     * dependency — in CI this is grep-able, and the published score in
     * `docs/BENCHMARKING.md` is the curator's canonical pasteable artifact.
     */
    fun renderReport(matrix: ConfusionMatrix, runId: String? = null): String = buildString {
        if (!runId.isNullOrBlank()) {
            appendLine("=== SafeGuard detection-rate run: $runId ===")
        } else {
            appendLine("=== SafeGuard detection-rate run ===")
        }
        appendLine("samples_total            ${matrix.total}")
        appendLine("samples_committed        ${matrix.committedTotal}")
        appendLine("true_positives           ${matrix.truePositives}")
        appendLine("false_positives          ${matrix.falsePositives}")
        appendLine("true_negatives           ${matrix.trueNegatives}")
        appendLine("false_negatives          ${matrix.falseNegatives}")
        appendLine("abstain_on_malicious     ${matrix.abstainPositive}")
        appendLine("abstain_on_safe          ${matrix.abstainNegative}")
        appendLine()
        appendLine("precision                ${"%.4f".format(matrix.precision)}")
        appendLine("recall                   ${"%.4f".format(matrix.recall)}")
        appendLine("f1                       ${"%.4f".format(matrix.f1)}")
        appendLine("false_positive_rate      ${"%.4f".format(matrix.falsePositiveRate)}")
        appendLine("accuracy_on_committed    ${"%.4f".format(matrix.accuracyCommitted)}")
    }
}
