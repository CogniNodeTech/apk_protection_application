package com.safeguard.core.util

import java.io.File
import java.io.RandomAccessFile
import java.util.zip.ZipFile

/**
 * Identifies Android package files independently of their filename extension.
 *
 * Why: malware commonly drops APKs with disguised names (e.g. `update.zip`, `photo.dat`,
 * no extension at all, or split-APK bundles like `.xapk` / `.apks`). A scanner that only
 * matches `*.apk` will miss those. This detector provides a tiered check:
 *
 *  1. [hasApkLikeExtension]              — zero-IO, fast list-filter pass.
 *  2. [hasZipMagicBytes]                 — 4-byte read of `PK\x03\x04` (ZIP local header).
 *  3. [isAndroidPackage]                 — full confirmation by opening the ZIP and looking
 *                                          for an `AndroidManifest.xml` entry.
 *
 * Use the cheaper checks during enumeration / event-time triage, and confirm with
 * [isAndroidPackage] before enqueueing a scan.
 */
object ApkSignatureDetector {

    /**
     * APK-like extensions we consider as candidates without further I/O. `.xapk` and `.apks`
     * are split-APK bundles produced by Play Asset Delivery / common installers and routinely
     * abused to ship side-loaded payloads.
     */
    val APK_LIKE_EXTENSIONS: Set<String> = setOf("apk", "xapk", "apks", "apkm")

    /** ZIP local file header: "PK\x03\x04". APKs are ZIPs, so they always start with this. */
    private val ZIP_MAGIC: ByteArray = byteArrayOf(0x50, 0x4B, 0x03, 0x04)

    /**
     * Plausibility window for a real Android package. APKs below ~10 KB cannot hold a real
     * `classes.dex` + manifest + resources; files above ~2 GiB cannot be installed. This is a
     * pre-filter to avoid touching obviously-impossible candidates.
     */
    private const val MIN_PLAUSIBLE_APK_SIZE = 10_000L
    private const val MAX_PLAUSIBLE_APK_SIZE = 2L * 1024 * 1024 * 1024

    /** True when [file]'s lowercased extension is one of [APK_LIKE_EXTENSIONS]. No IO. */
    fun hasApkLikeExtension(file: File): Boolean {
        val name = file.name
        val dot = name.lastIndexOf('.')
        if (dot < 0 || dot == name.length - 1) return false
        return name.substring(dot + 1).lowercase() in APK_LIKE_EXTENSIONS
    }

    /**
     * True when [file] starts with the ZIP local-header magic bytes. Reads exactly 4 bytes.
     * Returns false for unreadable files, directories, or files smaller than the magic.
     */
    fun hasZipMagicBytes(file: File): Boolean {
        if (!file.isFile || !file.canRead()) return false
        if (file.length() < ZIP_MAGIC.size) return false
        return try {
            RandomAccessFile(file, "r").use { raf ->
                val buf = ByteArray(ZIP_MAGIC.size)
                if (raf.read(buf) != ZIP_MAGIC.size) return false
                buf.contentEquals(ZIP_MAGIC)
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Authoritative check: opens the file as a ZIP and verifies an `AndroidManifest.xml`
     * entry exists at the archive root. This is the only way to be certain a `*.zip` /
     * extension-less file is actually an Android package. Falls back to false on any I/O
     * or format error so callers can safely chain it.
     */
    fun isAndroidPackage(file: File): Boolean {
        if (!file.isFile || !file.canRead()) return false
        val len = file.length()
        if (len !in MIN_PLAUSIBLE_APK_SIZE..MAX_PLAUSIBLE_APK_SIZE) return false
        if (!hasZipMagicBytes(file)) return false
        return try {
            ZipFile(file).use { zip -> zip.getEntry("AndroidManifest.xml") != null }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Convenience: cheap-first decision used by the deep scanner and the file observer.
     * Files matching [hasApkLikeExtension] are accepted immediately; otherwise we only
     * pay the ZIP-open cost when [hasZipMagicBytes] passes.
     */
    fun looksLikeApk(file: File): Boolean {
        if (hasApkLikeExtension(file)) return true
        return isAndroidPackage(file)
    }
}
