package com.safeguard.security.layers.layer7.yara

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Parser-only tests for the YARA-subset DSL. We assert *strictness* (anything outside
 * the documented subset must error loudly at load time) at least as much as we assert
 * happy-path parsing — silent shadowing of unsupported features is the worst possible
 * failure mode for a rules engine.
 */
class YaraRuleParserTest {

    @Test
    fun parsesMinimalRule() {
        val rule = YaraRuleParser.parseSingle(
            """
            rule Tiny {
              strings:
                ${'$'}a = "hello"
              condition:
                ${'$'}a
            }
            """.trimIndent()
        )
        assertEquals("Tiny", rule.name)
        assertEquals(1, rule.strings.size)
        assertTrue(rule.condition is YaraCondition.StringRef)
    }

    @Test
    fun parsesMetaWithSeverityAndDescription() {
        val rule = YaraRuleParser.parseSingle(
            """
            rule WithMeta {
              meta:
                author = "SafeGuard"
                severity = 95
                description = "demo"
                family = "Anatsa"
              strings:
                ${'$'}x = "abc"
              condition:
                ${'$'}x
            }
            """.trimIndent()
        )
        assertEquals(95, rule.meta.severity)
        assertEquals("SafeGuard", rule.meta.author)
        assertEquals("Anatsa", rule.meta.family)
        assertEquals("demo", rule.meta.description)
    }

    @Test
    fun parsesAnyAllNOfThemConditions() {
        val any = YaraRuleParser.parseSingle(
            """
            rule R1 {
              strings:
                ${'$'}a = "a"
                ${'$'}b = "b"
              condition: any of them
            }
            """.trimIndent()
        ).condition
        val all = YaraRuleParser.parseSingle(
            """
            rule R2 {
              strings:
                ${'$'}a = "a"
                ${'$'}b = "b"
              condition: all of them
            }
            """.trimIndent()
        ).condition
        val n = YaraRuleParser.parseSingle(
            """
            rule R3 {
              strings:
                ${'$'}a = "a"
                ${'$'}b = "b"
                ${'$'}c = "c"
              condition: 2 of them
            }
            """.trimIndent()
        ).condition
        assertEquals(YaraCondition.AnyOfThem, any)
        assertEquals(YaraCondition.AllOfThem, all)
        assertEquals(YaraCondition.NOfThem(2), n)
    }

    @Test
    fun parsesAndOrNotPrecedence() {
        // Per YARA: `not` > `and` > `or`. So `not $a and $b or $c` should parse as
        // `((not $a) and $b) or $c`.
        val cond = YaraRuleParser.parseSingle(
            """
            rule Logic {
              strings:
                ${'$'}a = "a"
                ${'$'}b = "b"
                ${'$'}c = "c"
              condition: not ${'$'}a and ${'$'}b or ${'$'}c
            }
            """.trimIndent()
        ).condition
        // Top-level should be Or; left should be And; And.left should be Not.
        assertTrue("expected Or at top-level, was $cond", cond is YaraCondition.Or)
        cond as YaraCondition.Or
        assertTrue(cond.left is YaraCondition.And)
        val and = cond.left as YaraCondition.And
        assertTrue(and.left is YaraCondition.Not)
    }

    @Test
    fun parsesHexBlockWithWildcards() {
        val rule = YaraRuleParser.parseSingle(
            """
            rule Hex {
              strings:
                ${'$'}h = { 4D 5A ?? 50 45 }
              condition: ${'$'}h
            }
            """.trimIndent()
        )
        val form = (rule.strings.single().matchableForms.single() as LiteralOrHex.Hex).pattern
        assertEquals(5, form.size)
        assertEquals(true, form.mask[0])
        assertEquals(false, form.mask[2])
        assertEquals(0x4D.toByte(), form.bytes[0])
        assertEquals(0x50.toByte(), form.bytes[3])
    }

    @Test
    fun parsesNocaseModifier_taggingId() {
        val rule = YaraRuleParser.parseSingle(
            """
            rule NoCase {
              strings:
                ${'$'}a = "Hydra" nocase
              condition: ${'$'}a
            }
            """.trimIndent()
        )
        // The parser tags the identifier with a sentinel suffix so the matcher can do
        // case-insensitive compares without reparsing modifiers.
        assertTrue(
            "expected NOCASE-tagged identifier, got '${rule.strings.single().identifier}'",
            rule.strings.single().identifier.endsWith("\u0001NOCASE")
        )
    }

    @Test
    fun rejectsRegexStrings() {
        try {
            YaraRuleParser.parseSingle(
                """
                rule Bad {
                  strings:
                    ${'$'}a = /pattern/
                  condition: ${'$'}a
                }
                """.trimIndent()
            )
            fail("Expected regex rejection")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("Regex") == true)
        }
    }

    @Test
    fun rejectsHalfByteWildcards() {
        try {
            YaraRuleParser.parseSingle(
                """
                rule Bad {
                  strings:
                    ${'$'}h = { 4D ?A }
                  condition: ${'$'}h
                }
                """.trimIndent()
            )
            fail("Expected half-byte wildcard rejection")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Half-byte"))
        }
    }

    @Test
    fun rejectsAllWildcardHexPattern() {
        try {
            YaraRuleParser.parseSingle(
                """
                rule Bad {
                  strings:
                    ${'$'}h = { ?? ?? ?? ?? }
                  condition: ${'$'}h
                }
                """.trimIndent()
            )
            fail("Expected all-wildcard rejection")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("all-wildcard"))
        }
    }

    @Test
    fun rejectsDuplicateRuleNamesInSingleSource() {
        try {
            YaraRuleParser.parseAll(
                """
                rule DupName {
                  strings: ${'$'}a = "a"
                  condition: ${'$'}a
                }
                rule DupName {
                  strings: ${'$'}b = "b"
                  condition: ${'$'}b
                }
                """.trimIndent()
            )
            fail("Expected duplicate-name rejection")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Duplicate"))
        }
    }

    @Test
    fun rejectsRuleWithoutCondition() {
        try {
            YaraRuleParser.parseSingle(
                """
                rule NoCondition {
                  strings: ${'$'}a = "a"
                }
                """.trimIndent()
            )
            fail("Expected missing-condition rejection")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("condition"))
        }
    }

    @Test
    fun parsesMultipleRulesWithComments() {
        val rules = YaraRuleParser.parseAll(
            """
            // top comment
            rule A {
              strings: ${'$'}a = "a"
              condition: ${'$'}a
            }
            /* block
               comment */
            rule B {
              strings: ${'$'}b = "b"
              condition: ${'$'}b
            }
            """.trimIndent()
        )
        assertEquals(listOf("A", "B"), rules.map { it.name })
    }
}
