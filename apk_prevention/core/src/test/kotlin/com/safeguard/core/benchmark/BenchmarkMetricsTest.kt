package com.safeguard.core.benchmark

import com.safeguard.core.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the Phase 3.3 metric calculator. We pin the precision/recall/F1 math
 * with hand-computed values so a future refactor of [BenchmarkMetrics] can't silently
 * change the published score formula.
 */
class BenchmarkMetricsTest {

    private fun sample(sha: String, expected: Verdict): BenchmarkSample =
        BenchmarkSample(sha256 = sha.padEnd(64, '0'), expected = expected)

    private fun pred(sha: String, expected: Verdict, predicted: Verdict, confidence: Float = 0.9f) =
        BenchmarkPrediction(
            sample = sample(sha, expected),
            predicted = predicted,
            confidence = confidence,
            durationMs = 0L
        )

    @Test
    fun perfectClassifier_yieldsPrecisionAndRecallOfOne() {
        val preds = listOf(
            pred("a", Verdict.MALICIOUS, Verdict.MALICIOUS),
            pred("b", Verdict.MALICIOUS, Verdict.MALICIOUS),
            pred("c", Verdict.SAFE, Verdict.SAFE),
            pred("d", Verdict.SAFE, Verdict.SAFE),
        )
        val m = BenchmarkMetrics.confusionMatrix(preds)
        assertEquals(2, m.truePositives)
        assertEquals(0, m.falsePositives)
        assertEquals(2, m.trueNegatives)
        assertEquals(0, m.falseNegatives)
        assertEquals(1.0, m.precision, 1e-9)
        assertEquals(1.0, m.recall, 1e-9)
        assertEquals(1.0, m.f1, 1e-9)
        assertEquals(0.0, m.falsePositiveRate, 1e-9)
    }

    @Test
    fun mixedPredictions_yieldExpectedConfusionMatrix() {
        val preds = listOf(
            pred("1", Verdict.MALICIOUS, Verdict.MALICIOUS),    // TP
            pred("2", Verdict.MALICIOUS, Verdict.MALICIOUS),    // TP
            pred("3", Verdict.MALICIOUS, Verdict.SAFE),         // FN
            pred("4", Verdict.MALICIOUS, Verdict.SUSPICIOUS),   // abstain on bad — recall miss
            pred("5", Verdict.SAFE, Verdict.MALICIOUS),         // FP
            pred("6", Verdict.SAFE, Verdict.SAFE),              // TN
            pred("7", Verdict.SAFE, Verdict.SAFE),              // TN
            pred("8", Verdict.SAFE, Verdict.UNKNOWN),           // abstain on safe — counts neither way
        )
        val m = BenchmarkMetrics.confusionMatrix(preds)

        assertEquals(2, m.truePositives)
        assertEquals(1, m.falsePositives)
        assertEquals(2, m.trueNegatives)
        assertEquals(1, m.falseNegatives)
        assertEquals(1, m.abstainPositive)
        assertEquals(1, m.abstainNegative)
        assertEquals(8, m.total)
        assertEquals(6, m.committedTotal)

        // precision = 2 / (2+1) = 0.6667
        assertEquals(2.0 / 3.0, m.precision, 1e-9)
        // recall = 2 / (2 + 1 + 1 abstain) = 0.5  (abstain on malicious counts as miss)
        assertEquals(0.5, m.recall, 1e-9)
        // f1 = 2 * 0.6667 * 0.5 / (0.6667 + 0.5)
        val expectedF1 = 2.0 * (2.0 / 3.0) * 0.5 / ((2.0 / 3.0) + 0.5)
        assertEquals(expectedF1, m.f1, 1e-9)
        // FPR = 1 / (2 + 1 + 1 abstain) = 0.25
        assertEquals(0.25, m.falsePositiveRate, 1e-9)
    }

    @Test
    fun emptyCorpus_doesNotDivideByZero() {
        val m = BenchmarkMetrics.confusionMatrix(emptyList())
        assertEquals(0.0, m.precision, 1e-9)
        assertEquals(0.0, m.recall, 1e-9)
        assertEquals(0.0, m.f1, 1e-9)
        assertEquals(0.0, m.falsePositiveRate, 1e-9)
        assertEquals(0.0, m.accuracyCommitted, 1e-9)
    }

    @Test
    fun renderReport_includesAllPinnedFields() {
        val preds = listOf(
            pred("1", Verdict.MALICIOUS, Verdict.MALICIOUS),
            pred("2", Verdict.SAFE, Verdict.SAFE),
        )
        val m = BenchmarkMetrics.confusionMatrix(preds)
        val report = BenchmarkMetrics.renderReport(m, runId = "smoke-2026-04-27")
        // We freeze the line keys so downstream dashboards / CI parsers don't break.
        for (key in listOf(
            "samples_total", "samples_committed",
            "true_positives", "false_positives", "true_negatives", "false_negatives",
            "abstain_on_malicious", "abstain_on_safe",
            "precision", "recall", "f1", "false_positive_rate", "accuracy_on_committed"
        )) {
            assertTrue("report must include '$key'\nreport was:\n$report", report.contains(key))
        }
        assertTrue(report.contains("smoke-2026-04-27"))
    }
}
