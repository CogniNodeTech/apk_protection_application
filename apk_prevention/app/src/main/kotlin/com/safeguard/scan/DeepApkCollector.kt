package com.safeguard.scan

import com.safeguard.core.util.ApkSignatureDetector
import java.io.File

/**
 * Recursively walks accessible storage [root] to find every readable Android package file,
 * including inside dot-prefixed / deeply nested folders **and APKs disguised with non-`.apk`
 * filenames** (e.g. `update.zip`, `photo.dat`, no extension at all). Uses iterative DFS,
 * canonical path de-duplication (symlink loops), and caps to avoid runaway work.
 *
 * Detection tiers (cheapest first) — see [ApkSignatureDetector]:
 *   1. Extension match: `*.apk`, `*.xapk`, `*.apks`, `*.apkm` (zero IO).
 *   2. ZIP magic bytes (`PK\x03\x04`) for size-plausible files; confirmed APK by checking
 *      for an `AndroidManifest.xml` entry inside the archive.
 *
 * A `disguisedApkCount` counter is reported so the UI can surface "we caught N hidden APKs".
 */
object DeepApkCollector {

    private const val DEFAULT_MAX_DEPTH = 50
    private const val DEFAULT_MAX_DIR_VISITS = 120_000

    /**
     * Cap on the number of size-plausible non-`.apk`-extension files we will fully ZIP-verify
     * per scan. Magic-byte filtering already prunes >99% of media; this cap protects against
     * pathological storages with thousands of large ZIP archives.
     */
    private const val MAX_DEEP_VERIFY_CANDIDATES = 5_000

    data class Result(
        val apkFiles: List<File>,
        val directoriesVisited: Int,
        val truncated: Boolean,
        val disguisedApkCount: Int = 0
    )

    fun collectApks(
        root: File,
        maxDepth: Int = DEFAULT_MAX_DEPTH,
        maxDirVisits: Int = DEFAULT_MAX_DIR_VISITS
    ): Result {
        val apks = mutableListOf<File>()
        val visitedCanon = HashSet<String>()
        var dirVisits = 0
        var truncated = false
        var disguisedApkCount = 0
        var deepVerifyBudget = MAX_DEEP_VERIFY_CANDIDATES

        data class Frame(val dir: File, val depth: Int)

        if (!root.exists() || !root.isDirectory) {
            return Result(apks, 0, false)
        }

        val stack = ArrayDeque<Frame>()
        stack.addLast(Frame(root, 0))

        while (stack.isNotEmpty()) {
            val (dir, depth) = stack.removeLast()
            if (depth > maxDepth) continue
            if (dirVisits >= maxDirVisits) {
                truncated = true
                break
            }

            val canon = try {
                dir.canonicalPath
            } catch (_: Exception) {
                continue
            }
            if (!visitedCanon.add(canon)) continue
            dirVisits++

            val children = try {
                dir.listFiles()
            } catch (_: SecurityException) {
                null
            } catch (_: Exception) {
                null
            } ?: continue

            for (child in children) {
                try {
                    when {
                        child.isDirectory -> stack.addLast(Frame(child, depth + 1))
                        child.isFile && child.canRead() -> {
                            if (ApkSignatureDetector.hasApkLikeExtension(child)) {
                                apks.add(child)
                            } else if (deepVerifyBudget > 0 && ApkSignatureDetector.hasZipMagicBytes(child)) {
                                deepVerifyBudget--
                                if (ApkSignatureDetector.isAndroidPackage(child)) {
                                    apks.add(child)
                                    disguisedApkCount++
                                }
                            }
                        }
                    }
                } catch (_: SecurityException) {
                    // Skip unreadable entries
                }
            }
        }

        return Result(
            apkFiles = apks.distinctBy { apk ->
                try {
                    apk.canonicalPath
                } catch (_: Exception) {
                    apk.absolutePath
                }
            },
            directoriesVisited = dirVisits,
            truncated = truncated,
            disguisedApkCount = disguisedApkCount
        )
    }
}
