package com.safeguard.core.util

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Random
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Unit tests for [ApkSignatureDetector]. The detector is the foundation of the recursive
 * file observer and the deep collector — false negatives mean missed malware, false positives
 * mean wasted scan budget. Tests pin both directions.
 */
class ApkSignatureDetectorTest {

    private val tempFiles = mutableListOf<File>()

    @After
    fun tearDown() {
        tempFiles.forEach { runCatching { it.delete() } }
        tempFiles.clear()
    }

    private fun tempFile(prefix: String, suffix: String, padToBytes: Long = 0): File {
        val f = File.createTempFile(prefix, suffix)
        if (padToBytes > 0 && f.length() < padToBytes) {
            f.outputStream().use { it.write(ByteArray(padToBytes.toInt())) }
        }
        tempFiles.add(f)
        return f
    }

    /**
     * Writes a ZIP whose entry contains the **given** bytes, defaulting to incompressible random
     * content so the resulting archive easily clears the detector's plausibility size floor
     * (the production code rejects ZIPs below 10 KB to skip pathological pseudo-APKs).
     */
    private fun writeZipWithEntry(
        file: File,
        entryName: String,
        contents: ByteArray = randomBytes(20_000)
    ) {
        ZipOutputStream(file.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry(entryName))
            zos.write(contents)
            zos.closeEntry()
        }
    }

    private fun randomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        // Fixed seed for deterministic test runs.
        Random(0xC0FFEE).nextBytes(bytes)
        return bytes
    }

    @Test
    fun hasApkLikeExtension_recognizesAllVariants() {
        listOf("app.apk", "Bundle.XAPK", "package.apks", "modded.ApkM").forEach { name ->
            val f = File(name)
            assertTrue("$name should match", ApkSignatureDetector.hasApkLikeExtension(f))
        }
    }

    @Test
    fun hasApkLikeExtension_rejectsObviousNonMatches() {
        listOf("photo.jpg", "doc.pdf", "archive.zip", "noext", "trailing.").forEach { name ->
            val f = File(name)
            assertFalse("$name should not match", ApkSignatureDetector.hasApkLikeExtension(f))
        }
    }

    @Test
    fun hasZipMagicBytes_detectsZipPrefix() {
        val zip = tempFile("with_magic", ".bin")
        writeZipWithEntry(zip, "AndroidManifest.xml")
        assertTrue(ApkSignatureDetector.hasZipMagicBytes(zip))
    }

    @Test
    fun hasZipMagicBytes_returnsFalseForNonZip() {
        val plain = tempFile("plain", ".bin", padToBytes = 12_000)
        plain.writeBytes(ByteArray(12_000))
        assertFalse(ApkSignatureDetector.hasZipMagicBytes(plain))
    }

    @Test
    fun isAndroidPackage_truePositive_extensionlessButValidApkStructure() {
        val disguised = tempFile("looks_like_image", ".dat")
        writeZipWithEntry(disguised, "AndroidManifest.xml")
        assertTrue(
            "ZIP with AndroidManifest.xml entry must be detected as APK regardless of name",
            ApkSignatureDetector.isAndroidPackage(disguised)
        )
    }

    @Test
    fun isAndroidPackage_falsePositive_zipWithoutAndroidManifestRejected() {
        val zipNotApk = tempFile("docs_only", ".zip")
        writeZipWithEntry(zipNotApk, "readme.txt")
        assertFalse(
            "Plain ZIP without AndroidManifest.xml must not be misclassified",
            ApkSignatureDetector.isAndroidPackage(zipNotApk)
        )
    }

    @Test
    fun isAndroidPackage_rejectsTooSmallFiles() {
        val tiny = tempFile("tiny_zip", ".zip")
        writeZipWithEntry(tiny, "AndroidManifest.xml", ByteArray(0))
        assertFalse(
            "Files below the plausibility size floor must be rejected to skip pathological cases",
            ApkSignatureDetector.isAndroidPackage(tiny)
        )
    }

    @Test
    fun looksLikeApk_extensionShortCircuitsBeforeIo() {
        // Empty file with .apk extension still passes the cheap path, even though it could not be
        // a real package — we leave that to the scan pipeline. The point is that no IO happens.
        val f = tempFile("zero_byte", ".apk")
        assertTrue(ApkSignatureDetector.looksLikeApk(f))
    }

    @Test
    fun looksLikeApk_recognizesDisguisedApkWithoutExtension() {
        val disguised = tempFile("update_payload", ".bin")
        writeZipWithEntry(disguised, "AndroidManifest.xml")
        assertTrue(ApkSignatureDetector.looksLikeApk(disguised))
    }
}
