package com.safeguard.core.util

import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.util.zip.ZipFile

/**
 * Utility for safely interacting with APK files to prevent resource leaks and memory issues.
 */
object SafeApkReader {

    /**
     * Executes a block of code with an ApkFile and ensures it is closed properly.
     */
    fun <T> useApkFile(file: File, block: (ApkFile) -> T): T? {
        return try {
            ApkFile(file).use { apk ->
                block(apk)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Executes a block of code with a ZipFile and ensures it is closed properly.
     */
    fun <T> useZipFile(file: File, block: (ZipFile) -> T): T? {
        return try {
            ZipFile(file).use { zip ->
                block(zip)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts strings from an APK entry without loading the entire entry into memory if possible.
     */
    fun containsString(file: File, entryName: String, pattern: String): Boolean {
        return try {
            ZipFile(file).use { zip ->
                val entry = zip.getEntry(entryName) ?: return false
                zip.getInputStream(entry).use { input ->
                    input.bufferedReader().lineSequence().any { it.contains(pattern, ignoreCase = true) }
                }
            }
        } catch (e: Exception) {
            false
        }
    }
}
