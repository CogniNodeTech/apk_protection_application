package com.safeguard.security.layers.layer7

import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.Verdict
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * End-to-end tests for [YaraScanner]. We build synthetic APK-shaped ZIPs (manifest +
 * one or more `classes*.dex` entries) so the test exercises the same byte-extraction +
 * matcher pipeline production scans go through, without depending on a real APK fixture.
 */
class YaraScannerTest {

    // ── happy path ─────────────────────────────────────────────────────────────────────

    @Test
    fun firesMaliciousVerdict_onHighSeverityRuleHit_inDex() = runTest {
        val rule = """
            rule TestBanker {
              meta:
                family = "TestBanker"
                severity = 95
                description = "synthetic test rule"
              strings:
                ${'$'}a = "OverlayService"
                ${'$'}b = "/api/getInjects"
              condition: 2 of them
            }
        """.trimIndent()
        val apk = buildApk(
            classesDex = "void OverlayService(); GET /api/getInjects HTTP/1.1".toByteArray(),
            manifest = "<manifest/>".toByteArray()
        )
        val scanner = YaraScanner(YaraRuleSet.fromSources(listOf(rule)))

        val result = scanner.verify(APKContext(apk), emptyList())

        assertEquals(Verdict.MALICIOUS, result.verdict)
        assertTrue("expected high confidence for severity≥90, got ${result.confidence}", result.confidence >= 0.9f)
        assertEquals(95, result.highestSeverity)
        assertEquals(listOf("TestBanker"), result.firedRules)
        assertNotNull(result.threatInfo)
        assertEquals("TestBanker", result.threatInfo?.threatFamily)
    }

    @Test
    fun firesSuspiciousVerdict_onMediumSeverityRuleHit() = runTest {
        val rule = """
            rule TestEvasion {
              meta:
                family = "TestEvasion"
                severity = 75
              strings:
                ${'$'}a = "isProbablySandbox"
                ${'$'}b = "isAvsInstalled"
              condition: any of them
            }
        """.trimIndent()
        val apk = buildApk(
            classesDex = "boolean isProbablySandbox() { return false; }".toByteArray(),
            manifest = ByteArray(0)
        )
        val scanner = YaraScanner(YaraRuleSet.fromSources(listOf(rule)))

        val result = scanner.verify(APKContext(apk), emptyList())

        assertEquals(Verdict.SUSPICIOUS, result.verdict)
        assertEquals(75, result.highestSeverity)
        assertEquals(75, result.riskScore)
    }

    @Test
    fun returnsSafe_whenNoRulesFire() = runTest {
        val rule = """
            rule TestSpecific {
              meta:
                severity = 95
              strings:
                ${'$'}a = "OverlayService"
                ${'$'}b = "/api/getInjects"
              condition: all of them
            }
        """.trimIndent()
        val apk = buildApk(
            classesDex = "totally benign code with no banker strings".toByteArray(),
            manifest = "<manifest package=\"com.benign.example\"/>".toByteArray()
        )
        val scanner = YaraScanner(YaraRuleSet.fromSources(listOf(rule)))

        val result = scanner.verify(APKContext(apk), emptyList())

        assertEquals(Verdict.SAFE, result.verdict)
        assertEquals(0, result.firedRules.size)
        assertTrue(result.evidence.any { it.contains("No pattern rules fired") })
    }

    // ── cross-artifact ─────────────────────────────────────────────────────────────────

    @Test
    fun matchesAcrossDexAndManifest() = runTest {
        // The point of running the matcher across multiple buffers in one scan: a rule
        // with stringA in dex + stringB in manifest must fire.
        val rule = """
            rule CrossArtifact {
              meta: severity = 90
              strings:
                ${'$'}dex_str = "loadDexPayload"
                ${'$'}manifest_str = "BIND_ACCESSIBILITY_SERVICE"
              condition: all of them
            }
        """.trimIndent()
        val apk = buildApk(
            classesDex = "private void loadDexPayload() {}".toByteArray(),
            manifest = "android.permission.BIND_ACCESSIBILITY_SERVICE".toByteArray()
        )
        val scanner = YaraScanner(YaraRuleSet.fromSources(listOf(rule)))

        val result = scanner.verify(APKContext(apk), emptyList())

        assertEquals(Verdict.MALICIOUS, result.verdict)
        assertEquals(listOf("CrossArtifact"), result.firedRules)
    }

    @Test
    fun matchesInMultipleDexFiles() = runTest {
        // classes.dex hits stringA, classes2.dex hits stringB. The scanner must visit
        // both — multidex apps are normal in 2026.
        val rule = """
            rule MultiDex {
              meta: severity = 90
              strings:
                ${'$'}a = "primary_marker"
                ${'$'}b = "secondary_marker"
              condition: all of them
            }
        """.trimIndent()
        val apk = buildApk(
            classesDex = "void primary_marker() {}".toByteArray(),
            extraDex = mapOf("classes2.dex" to "void secondary_marker() {}".toByteArray()),
            manifest = ByteArray(0)
        )
        val scanner = YaraScanner(YaraRuleSet.fromSources(listOf(rule)))

        val result = scanner.verify(APKContext(apk), emptyList())

        assertEquals(Verdict.MALICIOUS, result.verdict)
    }

    // ── ruleset edge cases ─────────────────────────────────────────────────────────────

    @Test
    fun emptyRuleSet_returnsSafeWithExplanatoryEvidence() = runTest {
        val apk = buildApk("anything".toByteArray(), "manifest".toByteArray())
        val scanner = YaraScanner(YaraRuleSet.fromSources(emptyList()))

        val result = scanner.verify(APKContext(apk), emptyList())

        assertEquals(Verdict.SAFE, result.verdict)
        assertTrue(
            "empty ruleset should be flagged in evidence",
            result.evidence.any { it.contains("ruleset is empty") }
        )
    }

    @Test
    fun corruptZip_returnsSafeWithoutThrowing() = runTest {
        val notAZip = File.createTempFile("corrupt", ".apk").apply {
            writeBytes(ByteArray(2048) { 0xFF.toByte() })
            deleteOnExit()
        }
        val rule = """
            rule R { meta: severity = 95 strings: ${'$'}a = "anything" condition: ${'$'}a }
        """.trimIndent()
        val scanner = YaraScanner(YaraRuleSet.fromSources(listOf(rule)))

        val result = scanner.verify(APKContext(notAZip), emptyList())

        assertEquals(Verdict.SAFE, result.verdict)
        assertTrue(
            "corrupt apk should produce skip evidence",
            result.evidence.any { it.contains("could not be opened") }
        )
        assertFalse(notAZip.exists() && result.firedRules.isNotEmpty())
    }

    @Test
    fun multipleRulesFire_highestSeverityWins() = runTest {
        val sources = listOf(
            """
            rule LowSev {
              meta: severity = 70
              strings: ${'$'}a = "marker_low"
              condition: ${'$'}a
            }
            """.trimIndent(),
            """
            rule HighSev {
              meta: severity = 95 family = "Critical"
              strings: ${'$'}a = "marker_high"
              condition: ${'$'}a
            }
            """.trimIndent()
        )
        val apk = buildApk(
            classesDex = "marker_low and marker_high are both here".toByteArray(),
            manifest = ByteArray(0)
        )
        val scanner = YaraScanner(YaraRuleSet.fromSources(sources))

        val result = scanner.verify(APKContext(apk), emptyList())

        assertEquals(Verdict.MALICIOUS, result.verdict)
        assertEquals(95, result.highestSeverity)
        assertEquals(setOf("LowSev", "HighSev"), result.firedRules.toSet())
        // ThreatInfo should reflect the *higher* severity rule's family.
        assertEquals("Critical", result.threatInfo?.threatFamily)
    }

    // ── helpers ────────────────────────────────────────────────────────────────────────

    private fun buildApk(
        classesDex: ByteArray,
        manifest: ByteArray,
        extraDex: Map<String, ByteArray> = emptyMap()
    ): File {
        val apk = File.createTempFile("synth", ".apk").apply { deleteOnExit() }
        ZipOutputStream(apk.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry("AndroidManifest.xml"))
            zos.write(manifest); zos.closeEntry()
            zos.putNextEntry(ZipEntry("classes.dex"))
            zos.write(classesDex); zos.closeEntry()
            for ((name, body) in extraDex) {
                zos.putNextEntry(ZipEntry(name))
                zos.write(body); zos.closeEntry()
            }
        }
        return apk
    }
}
