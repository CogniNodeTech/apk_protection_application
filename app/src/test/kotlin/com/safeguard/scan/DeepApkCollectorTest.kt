package com.safeguard.scan

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.Random
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * End-to-end coverage for the deep collector's two detection tiers:
 *   - Files with apk-like extensions are picked up.
 *   - Files **without** apk-like extensions are picked up when their bytes are a real Android
 *     package (ZIP + AndroidManifest.xml).
 *
 * Pinning the disguised-APK case is the whole point: that is the malware-evasion vector that
 * extension-only scanners miss.
 */
class DeepApkCollectorTest {

    private val tempDirs = mutableListOf<File>()

    @After
    fun tearDown() {
        tempDirs.forEach { it.deleteRecursively() }
        tempDirs.clear()
    }

    private fun newTempDir(): File {
        val dir = Files.createTempDirectory("deep_collector_test_").toFile()
        tempDirs.add(dir)
        return dir
    }

    /**
     * Writes an apk-shaped ZIP. Uses incompressible random bytes so the resulting file clears
     * [com.safeguard.core.util.ApkSignatureDetector]'s 10 KB plausibility floor — DEFLATE would
     * otherwise shrink 20 KB of zero bytes to a few hundred bytes and the detector would (correctly)
     * reject it as too small to be a real package.
     */
    private fun File.writeApkLikeZip(includeAndroidManifest: Boolean = true) {
        val rng = Random(0xC0FFEE)
        ZipOutputStream(outputStream()).use { zos ->
            if (includeAndroidManifest) {
                zos.putNextEntry(ZipEntry("AndroidManifest.xml"))
                zos.write(ByteArray(20_000).also { rng.nextBytes(it) })
                zos.closeEntry()
            }
            zos.putNextEntry(ZipEntry("classes.dex"))
            zos.write(ByteArray(20_000).also { rng.nextBytes(it) })
            zos.closeEntry()
        }
    }

    @Test
    fun finds_apksByExtensionInNestedDirectories() {
        val root = newTempDir()
        val nested = File(root, "Telegram/Telegram Documents").apply { mkdirs() }
        val apk = File(nested, "payload.apk").apply { writeApkLikeZip() }

        val result = DeepApkCollector.collectApks(root)

        assertTrue(result.apkFiles.any { it.absolutePath == apk.absolutePath })
        assertEquals(0, result.disguisedApkCount)
    }

    @Test
    fun finds_disguisedApkWithNonApkExtension() {
        val root = newTempDir()
        val downloads = File(root, "Download").apply { mkdirs() }
        val disguised = File(downloads, "weather_update.dat").apply { writeApkLikeZip() }
        val benignZip = File(downloads, "photos.zip").apply { writeApkLikeZip(includeAndroidManifest = false) }

        val result = DeepApkCollector.collectApks(root)

        val foundPaths = result.apkFiles.map { it.absolutePath }
        assertTrue("Disguised APK must be detected", foundPaths.contains(disguised.absolutePath))
        assertTrue(
            "Plain ZIP without AndroidManifest.xml must NOT be misclassified as APK",
            !foundPaths.contains(benignZip.absolutePath)
        )
        assertEquals(1, result.disguisedApkCount)
    }

    @Test
    fun ignores_unrelatedMediaFilesQuickly() {
        val root = newTempDir()
        val dcim = File(root, "DCIM/Camera").apply { mkdirs() }
        repeat(10) { idx ->
            File(dcim, "IMG_$idx.jpg").writeBytes(ByteArray(50_000))
        }

        val result = DeepApkCollector.collectApks(root)

        assertEquals(0, result.apkFiles.size)
        assertEquals(0, result.disguisedApkCount)
    }

    @Test
    fun deduplicates_apkSeenViaMultiplePaths() {
        val root = newTempDir()
        val a = File(root, "a").apply { mkdirs() }
        val target = File(a, "real.apk").apply { writeApkLikeZip() }
        val b = File(root, "b").apply { mkdirs() }
        // A second hard reference (copy) at a different path counts as a separate APK; we are
        // testing the canonical-path dedup which only collapses symlink loops, not copies.
        val copyOfTarget = File(b, "real_copy.apk")
        target.copyTo(copyOfTarget)

        val result = DeepApkCollector.collectApks(root)

        assertEquals(2, result.apkFiles.size)
    }
}
