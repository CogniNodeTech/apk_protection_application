package com.safeguard.security.layers.layer1

import android.os.FileObserver
import com.safeguard.core.util.ApkSignatureDetector
import java.io.File

/**
 * Monitors directories for new APK files. Uses FileObserver (no accessibility service).
 *
 * Detection coverage:
 *  - Filenames with apk-like extensions (`.apk`, `.xapk`, `.apks`, `.apkm`) trigger on any of
 *    CREATE / MOVED_TO / CLOSE_WRITE events (mirrors how installers/messengers drop files).
 *  - Files without an apk-like extension are only deep-checked once the writer closes them
 *    (CLOSE_WRITE) by reading ZIP magic bytes and the `AndroidManifest.xml` entry. This catches
 *    APKs disguised as `update.zip`, `photo.dat`, no-extension drops, etc.
 */
class FileSystemMonitor(
    private val monitoredPaths: List<File>,
    private val recursive: Boolean = false,
    private val onAPKDetected: (File, Int) -> Unit
) {
    private val flatObservers = mutableListOf<FileObserver>()
    private val recursiveObservers = mutableListOf<RecursiveFileObserver>()

    fun startMonitoring() {
        monitoredPaths.forEach { path ->
            if (!path.exists()) return@forEach
            if (recursive) {
                val observer = RecursiveFileObserver(root = path) { event, file ->
                    handleEvent(event, file, monitoredRoot = path)
                }
                observer.startWatching()
                recursiveObservers.add(observer)
            } else {
                val observer = createFlatObserver(path)
                observer.startWatching()
                flatObservers.add(observer)
            }
        }
    }

    fun stopMonitoring() {
        flatObservers.forEach {
            try { it.stopWatching() } catch (_: Exception) { }
        }
        flatObservers.clear()
        recursiveObservers.forEach {
            try { it.stopWatching() } catch (_: Exception) { }
        }
        recursiveObservers.clear()
    }

    private fun createFlatObserver(path: File): FileObserver {
        return object : FileObserver(path, CREATE or MOVED_TO or CLOSE_WRITE) {
            override fun onEvent(event: Int, relativePath: String?) {
                relativePath ?: return
                val target = File(path, relativePath)
                handleEvent(event, target, monitoredRoot = path)
            }
        }
    }

    /** Shared classification used by both flat and recursive observers. */
    private fun handleEvent(event: Int, target: File, monitoredRoot: File) {
        if (!target.exists() || !target.isFile) return

        val isFinalized = (event and (FileObserver.CLOSE_WRITE or FileObserver.MOVED_TO)) != 0
        val matchedByExtension = ApkSignatureDetector.hasApkLikeExtension(target)

        val isApk = when {
            matchedByExtension -> true
            // Only run the costlier ZIP/manifest probe once the writer closes the file
            // or the file is moved into place (renames). Avoids reading torn writes on CREATE.
            isFinalized -> ApkSignatureDetector.isAndroidPackage(target)
            else -> false
        }
        if (!isApk) return

        val risk = calculateInitialRisk(target, monitoredRoot.absolutePath, matchedByExtension)
        onAPKDetected(target, risk)
    }

    fun calculateInitialRisk(apkFile: File, sourcePath: String, matchedByExtension: Boolean = true): Int {
        var risk = 0
        if (sourcePath.contains("WhatsApp", ignoreCase = true) ||
            sourcePath.contains("Telegram", ignoreCase = true)
        ) risk += 20
        val name = apkFile.name.lowercase()
        if (name.contains("update") || name.contains("patch") || name.contains("mod")) risk += 30
        val size = apkFile.length()
        if (size < 100_000) risk += 40
        else if (size > 200_000_000) risk += 20
        // Disguised APKs (no .apk extension) are inherently more suspicious — they imply intent
        // to bypass naive filename-based scanners. Bump the initial risk score accordingly.
        if (!matchedByExtension) risk += 25
        return risk.coerceIn(0, 100)
    }
}
