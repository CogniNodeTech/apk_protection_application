package com.safeguard.core.benchmark

import com.safeguard.core.domain.model.Verdict
import kotlinx.coroutines.runBlocking

/**
 * Phase 3.3 oracle abstraction. The benchmark runner doesn't care whether the verdict
 * comes from a live `ScanOrchestrator` against a real APK, a recorded JSON of past scan
 * outputs, or even a fuzzy-hash-only lookup against a pre-built catalogue. All it needs
 * to know is "given a sample, what verdict and confidence does the engine emit".
 *
 * Implementations in `app:` will typically wrap the production `ScanOrchestrator`. Tests
 * use the [InMemoryScanOracle] below.
 */
interface ScanOracle {
    /**
     * Returns the engine's prediction for [sample]. Throws if the sample cannot be
     * evaluated (missing on-disk APK + no recorded result); the runner will catch and
     * count as an evaluation failure rather than aborting the whole batch.
     */
    suspend fun evaluate(sample: BenchmarkSample): OracleVerdict
}

data class OracleVerdict(
    val verdict: Verdict,
    val confidence: Float,
    val notes: String? = null
)

/**
 * Test/CI-friendly oracle backed by a static map. Useful for two flows:
 *  - In `core` unit tests, where we want to exercise the metric pipeline with fully
 *    deterministic inputs.
 *  - In CI corpus runs that ship a pre-computed `(sha256 -> verdict)` index alongside
 *    the manifest, removing the requirement for the CI runner to hold the actual APKs.
 */
class InMemoryScanOracle(
    private val verdicts: Map<String, OracleVerdict>,
    private val fallback: OracleVerdict = OracleVerdict(Verdict.UNKNOWN, 0f, "no recorded verdict")
) : ScanOracle {
    override suspend fun evaluate(sample: BenchmarkSample): OracleVerdict =
        verdicts[sample.sha256.lowercase()] ?: fallback
}

/**
 * Drives a [ScanOracle] across a corpus and produces a [BenchmarkPrediction] per row.
 * Single-threaded by design — corpus runs are I/O bound on the APK reader and the
 * decision engine is cheap, so the simplicity of a deterministic loop wins over
 * coroutine-fanout machinery for the size of corpora we expect (a few thousand samples
 * at most before someone moves the harness to a real ML pipeline).
 */
class BenchmarkRunner(private val oracle: ScanOracle) {

    fun run(corpus: List<BenchmarkSample>): List<BenchmarkPrediction> = runBlocking {
        val out = ArrayList<BenchmarkPrediction>(corpus.size)
        for (sample in corpus) {
            val start = System.nanoTime()
            val prediction = try {
                val v = oracle.evaluate(sample)
                BenchmarkPrediction(
                    sample = sample,
                    predicted = v.verdict,
                    confidence = v.confidence,
                    durationMs = (System.nanoTime() - start) / 1_000_000,
                    notes = v.notes
                )
            } catch (t: Throwable) {
                // Errored evaluations are treated as UNKNOWN — they show up in the
                // abstain bucket of the metric report, which is the correct behaviour:
                // they're neither a TP nor a FP, but they *do* count as a recall miss
                // when the underlying sample was malicious.
                BenchmarkPrediction(
                    sample = sample,
                    predicted = Verdict.UNKNOWN,
                    confidence = 0f,
                    durationMs = (System.nanoTime() - start) / 1_000_000,
                    notes = "oracle_error: ${t.javaClass.simpleName}: ${t.message?.take(120)}"
                )
            }
            out += prediction
        }
        out
    }
}
