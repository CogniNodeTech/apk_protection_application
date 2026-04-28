package com.safeguard.security.layers.layer7.yara

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Behavioural tests for [YaraMatcher]. Every test feeds raw bytes directly so we can
 * pin the matcher's contract independent of zip / dex extraction in [YaraScanner].
 */
class YaraMatcherTest {

    @Test
    fun fires_whenAllStringsPresent_andConditionIsAllOfThem() {
        val rules = YaraRuleParser.parseAll(
            """
            rule R {
              strings:
                ${'$'}a = "alpha"
                ${'$'}b = "bravo"
              condition: all of them
            }
            """.trimIndent()
        )
        val m = YaraMatcher(rules)
        m.scan("--alpha--bravo--".toByteArray(Charsets.ISO_8859_1))
        val hits = m.results()
        assertEquals(1, hits.size)
        assertEquals(listOf("\$a", "\$b"), hits.single().matchedIdentifiers)
    }

    @Test
    fun doesNotFire_whenOnlyOneOfTwoStringsHits_underAllOfThem() {
        val rules = YaraRuleParser.parseAll(
            """
            rule R {
              strings:
                ${'$'}a = "alpha"
                ${'$'}b = "bravo"
              condition: all of them
            }
            """.trimIndent()
        )
        val m = YaraMatcher(rules)
        m.scan("--alpha-only--".toByteArray(Charsets.ISO_8859_1))
        assertTrue(m.results().isEmpty())
    }

    @Test
    fun fires_onAnyOfThem_evenWithSingleHit() {
        val rules = YaraRuleParser.parseAll(
            """
            rule R {
              strings:
                ${'$'}a = "alpha"
                ${'$'}b = "bravo"
                ${'$'}c = "charlie"
              condition: any of them
            }
            """.trimIndent()
        )
        val m = YaraMatcher(rules)
        m.scan("zzz bravo qqq".toByteArray(Charsets.ISO_8859_1))
        assertEquals(1, m.results().size)
    }

    @Test
    fun nOfThem_demandsAtLeastN() {
        val rules = YaraRuleParser.parseAll(
            """
            rule R {
              strings:
                ${'$'}a = "x1"
                ${'$'}b = "x2"
                ${'$'}c = "x3"
                ${'$'}d = "x4"
              condition: 3 of them
            }
            """.trimIndent()
        )
        val m1 = YaraMatcher(rules); m1.scan("x1 x2".toByteArray()); assertTrue(m1.results().isEmpty())
        val m2 = YaraMatcher(rules); m2.scan("x1 x2 x3".toByteArray()); assertEquals(1, m2.results().size)
        val m3 = YaraMatcher(rules); m3.scan("x1 x2 x3 x4".toByteArray()); assertEquals(1, m3.results().size)
    }

    @Test
    fun nocase_matchesMixedCase() {
        val rules = YaraRuleParser.parseAll(
            """
            rule NoCase {
              strings:
                ${'$'}a = "anatsa" nocase
              condition: ${'$'}a
            }
            """.trimIndent()
        )
        val m = YaraMatcher(rules)
        m.scan("we found ANATSA inside!".toByteArray(Charsets.ISO_8859_1))
        assertEquals(1, m.results().size)
    }

    @Test
    fun caseSensitive_doesNotMatchAcrossCase() {
        val rules = YaraRuleParser.parseAll(
            """
            rule Case {
              strings:
                ${'$'}a = "Anatsa"
              condition: ${'$'}a
            }
            """.trimIndent()
        )
        val m = YaraMatcher(rules)
        m.scan("we found ANATSA inside!".toByteArray(Charsets.ISO_8859_1))
        assertTrue(m.results().isEmpty())
    }

    @Test
    fun hexPattern_matchesWithWildcards() {
        val rules = YaraRuleParser.parseAll(
            """
            rule Hex {
              strings:
                ${'$'}h = { DE AD ?? BE EF }
              condition: ${'$'}h
            }
            """.trimIndent()
        )
        val m = YaraMatcher(rules)
        // Two candidate substrings, one matches via wildcard byte 0x42.
        val payload = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0x42, 0xBE.toByte(), 0xEF.toByte())
        m.scan(payload)
        assertEquals(1, m.results().size)
    }

    @Test
    fun hexPattern_doesNotMatchWhenFixedByteWrong() {
        val rules = YaraRuleParser.parseAll(
            """
            rule Hex {
              strings:
                ${'$'}h = { DE AD BE EF }
              condition: ${'$'}h
            }
            """.trimIndent()
        )
        val m = YaraMatcher(rules)
        val payload = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xCC.toByte(), 0xEF.toByte())
        m.scan(payload)
        assertTrue(m.results().isEmpty())
    }

    @Test
    fun multipleRules_matchedIndependently() {
        val rules = YaraRuleParser.parseAll(
            """
            rule A {
              strings: ${'$'}a = "alpha"
              condition: ${'$'}a
            }
            rule B {
              strings: ${'$'}b = "beta"
              condition: ${'$'}b
            }
            """.trimIndent()
        )
        val m = YaraMatcher(rules)
        m.scan("zzz alpha zzz".toByteArray())
        val hits = m.results()
        assertEquals(1, hits.size)
        assertEquals("A", hits.single().rule.name)
    }

    @Test
    fun stringMatchesAcrossMultipleScanCalls() {
        // This is the cross-artifact use case: dex contributes ${'$'}a and the manifest
        // contributes ${'$'}b. The matcher must accumulate hits across calls.
        val rules = YaraRuleParser.parseAll(
            """
            rule Cross {
              strings:
                ${'$'}a = "stage1"
                ${'$'}b = "stage2"
              condition: all of them
            }
            """.trimIndent()
        )
        val m = YaraMatcher(rules)
        m.scan("xxx stage1 yyy".toByteArray())
        assertTrue(m.results().isEmpty())
        m.scan("zzz stage2 qqq".toByteArray())
        assertEquals(1, m.results().size)
    }

    @Test
    fun emptyBufferIsSafe() {
        val rules = YaraRuleParser.parseAll(
            """
            rule R {
              strings: ${'$'}a = "x"
              condition: ${'$'}a
            }
            """.trimIndent()
        )
        val m = YaraMatcher(rules)
        m.scan(ByteArray(0))
        assertTrue(m.results().isEmpty())
    }

    @Test
    fun lengthBoundIsRespected() {
        // The matcher's `length` arg lets a caller scan a *prefix* of the buffer; the
        // pattern that lives past the prefix must not match.
        val rules = YaraRuleParser.parseAll(
            """
            rule R {
              strings: ${'$'}a = "tail"
              condition: ${'$'}a
            }
            """.trimIndent()
        )
        val m = YaraMatcher(rules)
        val buf = "head padding tail".toByteArray()
        m.scan(buf, length = "head padding".length)
        assertTrue(m.results().isEmpty())
        m.scan(buf, length = buf.size)
        assertEquals(1, m.results().size)
    }

    @Test
    fun andOrNot_evaluateOnMatchSet() {
        val rules = YaraRuleParser.parseAll(
            """
            rule Logic {
              strings:
                ${'$'}a = "alpha"
                ${'$'}b = "bravo"
                ${'$'}c = "charlie"
              condition: ${'$'}a and not ${'$'}b
            }
            """.trimIndent()
        )
        val mFire = YaraMatcher(rules); mFire.scan("alpha charlie".toByteArray())
        assertEquals(1, mFire.results().size)
        val mNo = YaraMatcher(rules); mNo.scan("alpha bravo".toByteArray())
        assertFalse(mNo.results().any())
    }
}
