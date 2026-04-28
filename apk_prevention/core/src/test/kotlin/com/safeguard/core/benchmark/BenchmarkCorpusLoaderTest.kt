package com.safeguard.core.benchmark

import com.safeguard.core.domain.model.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 3.3 corpus loader contract tests. Manifest format changes are an external API
 * (curators write it by hand), so we lock the parser's tolerance / strictness here.
 */
class BenchmarkCorpusLoaderTest {

    private val sha1 = "a".repeat(64)
    private val sha2 = "b".repeat(64)
    private val sha3 = "c".repeat(64)

    @Test
    fun loadsHeaderedCsvWithMixedLabels() {
        val csv = """
            sha256,label,apk_path,family
            $sha1,malicious,/tmp/a.apk,anatsa
            $sha2,clean,,playstore-bundle
            $sha3,SAFE,/tmp/c.apk,
        """.trimIndent()

        val samples = BenchmarkCorpusLoader.loadFromString(csv)
        assertEquals(3, samples.size)
        assertEquals(Verdict.MALICIOUS, samples[0].expected)
        assertEquals("/tmp/a.apk", samples[0].apkPath)
        assertEquals("anatsa", samples[0].metadata["family"])

        assertEquals(Verdict.SAFE, samples[1].expected)
        assertNull(samples[1].apkPath)
        assertEquals("playstore-bundle", samples[1].metadata["family"])

        assertEquals(Verdict.SAFE, samples[2].expected)
        // Empty metadata entries are stripped — they would otherwise pollute the
        // per-row report with noisy `family=` fields.
        assertTrue(samples[2].metadata.isEmpty())
    }

    @Test
    fun stripsCommentsAndBlankLines() {
        val csv = """
            # SafeGuard corpus v3 — run id smoke-001
            sha256,label

            # next sample is a known banker
            $sha1,malicious

        """.trimIndent()
        val samples = BenchmarkCorpusLoader.loadFromString(csv)
        assertEquals(1, samples.size)
        assertEquals(sha1, samples[0].sha256)
    }

    @Test
    fun supportsQuotedFieldsWithCommasAndEscapedQuotes() {
        val csv = """
            sha256,label,note
            $sha1,malicious,"family=""anatsa"", note=dropped from goo, gle drive"
        """.trimIndent()
        val samples = BenchmarkCorpusLoader.loadFromString(csv)
        assertEquals(1, samples.size)
        assertEquals(
            "family=\"anatsa\", note=dropped from goo, gle drive",
            samples[0].metadata["note"]
        )
    }

    @Test
    fun unknownLabelsAreSkippedNotFatal() {
        val csv = """
            sha256,label
            $sha1,malicious
            $sha2,definitely-bad-but-mistyped
            $sha3,clean
        """.trimIndent()
        val samples = BenchmarkCorpusLoader.loadFromString(csv)
        // Row 2 should be skipped, leaving the well-formed entries intact.
        assertEquals(2, samples.size)
        assertEquals(sha1, samples[0].sha256)
        assertEquals(sha3, samples[1].sha256)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsHeaderMissingShaColumn() {
        val csv = """
            label,note
            malicious,foo
        """.trimIndent()
        BenchmarkCorpusLoader.loadFromString(csv)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsHeaderMissingLabelColumn() {
        val csv = """
            sha256,note
            $sha1,foo
        """.trimIndent()
        BenchmarkCorpusLoader.loadFromString(csv)
    }
}
