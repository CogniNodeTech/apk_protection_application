package com.safeguard.core.benchmark

import com.safeguard.core.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BenchmarkRunnerTest {

    private fun corpus(): List<BenchmarkSample> = listOf(
        BenchmarkSample("a".repeat(64), Verdict.MALICIOUS),
        BenchmarkSample("b".repeat(64), Verdict.MALICIOUS),
        BenchmarkSample("c".repeat(64), Verdict.SAFE),
    )

    @Test
    fun runner_appliesOracleVerdictsAndPreservesOrder() {
        val oracle = InMemoryScanOracle(
            verdicts = mapOf(
                "a".repeat(64) to OracleVerdict(Verdict.MALICIOUS, 0.95f, "yara_hit"),
                "b".repeat(64) to OracleVerdict(Verdict.SUSPICIOUS, 0.6f, "high_risk_perm_only"),
                "c".repeat(64) to OracleVerdict(Verdict.SAFE, 0.9f, null),
            )
        )
        val preds = BenchmarkRunner(oracle).run(corpus())
        assertEquals(3, preds.size)
        assertEquals(Verdict.MALICIOUS, preds[0].predicted)
        assertEquals(0.95f, preds[0].confidence)
        assertEquals("yara_hit", preds[0].notes)
        assertEquals(Verdict.SUSPICIOUS, preds[1].predicted)
        assertEquals(Verdict.SAFE, preds[2].predicted)
    }

    @Test
    fun runner_treatsOracleErrorAsUnknownAndContinues() {
        val explosiveOracle = object : ScanOracle {
            override suspend fun evaluate(sample: BenchmarkSample): OracleVerdict {
                if (sample.sha256.startsWith("b")) error("oracle blew up")
                return OracleVerdict(Verdict.MALICIOUS, 0.9f)
            }
        }
        val preds = BenchmarkRunner(explosiveOracle).run(corpus())
        assertEquals(3, preds.size)
        assertEquals(Verdict.MALICIOUS, preds[0].predicted)
        // Errored sample is degraded to UNKNOWN, with the underlying error captured in notes.
        assertEquals(Verdict.UNKNOWN, preds[1].predicted)
        assertNotNull(preds[1].notes)
        assertTrue("notes should mention oracle_error", preds[1].notes!!.contains("oracle_error"))
        // Run continues past the failure.
        assertEquals(Verdict.MALICIOUS, preds[2].predicted)
    }

    @Test
    fun runner_fallsBackToConfiguredDefaultWhenOracleHasNoEntry() {
        val oracle = InMemoryScanOracle(
            verdicts = mapOf("c".repeat(64) to OracleVerdict(Verdict.SAFE, 0.9f)),
            fallback = OracleVerdict(Verdict.UNKNOWN, 0f, "no recorded verdict")
        )
        val preds = BenchmarkRunner(oracle).run(corpus())
        assertEquals(Verdict.UNKNOWN, preds[0].predicted)
        assertEquals(Verdict.UNKNOWN, preds[1].predicted)
        assertEquals(Verdict.SAFE, preds[2].predicted)
    }
}
