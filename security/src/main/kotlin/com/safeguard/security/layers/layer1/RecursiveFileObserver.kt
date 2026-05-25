package com.safeguard.security.layers.layer1

import android.os.FileObserver
import android.util.Log
import java.io.File

/**
 * A recursive [FileObserver] that watches an entire directory subtree by registering one
 * `FileObserver` per directory it discovers. New subdirectories created at runtime are
 * automatically picked up; deleted/moved-out subdirectories release their watch descriptors.
 *
 * Why this class exists: Android's stock [FileObserver] is **not** recursive — it only signals
 * events for direct children. Without this, `FileSystemMonitor` would silently miss APKs
 * dropped into nested folders (e.g. `Android/media/org.telegram.messenger/Telegram/Telegram Documents/`),
 * which is exactly where messenger-delivered APKs land in 2025.
 *
 * Resource limits:
 *  - `inotify` has a per-user watch limit (typically a few thousand). We cap [maxWatches] to
 *    keep us well under that even on devices with deep storage trees.
 *  - Discovery is iterative DFS with canonical-path de-duplication (symlink loops).
 *
 * @param root           Subtree root.
 * @param eventMask      Same flags accepted by `FileObserver`. Defaults to apk-relevant ones.
 * @param maxWatches     Hard cap on simultaneous directory watches.
 * @param maxDepth       Hard cap on subtree depth.
 * @param onEvent        Invoked with `(eventMask, fullChildFile)`. `fullChildFile` is absolute.
 */
class RecursiveFileObserver(
    private val root: File,
    private val eventMask: Int = DEFAULT_EVENT_MASK,
    private val maxWatches: Int = DEFAULT_MAX_WATCHES,
    private val maxDepth: Int = DEFAULT_MAX_DEPTH,
    private val onEvent: (event: Int, file: File) -> Unit
) {
    private val watches = HashMap<String, FileObserver>()
    private val canonicalRegistered = HashSet<String>()

    fun startWatching() {
        if (!root.exists() || !root.isDirectory) return
        registerSubtree(root, depth = 0)
    }

    fun stopWatching() {
        watches.values.forEach { observer ->
            try {
                observer.stopWatching()
            } catch (_: Exception) {
                // ignore
            }
        }
        watches.clear()
        canonicalRegistered.clear()
    }

    /** Iterative DFS registration so we never recurse the JVM stack. */
    private fun registerSubtree(start: File, depth: Int) {
        data class Frame(val dir: File, val depth: Int)
        val stack = ArrayDeque<Frame>()
        stack.addLast(Frame(start, depth))
        while (stack.isNotEmpty() && watches.size < maxWatches) {
            val (dir, currentDepth) = stack.removeLast()
            if (currentDepth > maxDepth) continue
            if (!dir.isDirectory) continue

            val canon = try {
                dir.canonicalPath
            } catch (_: Exception) {
                continue
            }
            if (!canonicalRegistered.add(canon)) continue
            registerSingle(dir)

            val children = try {
                dir.listFiles()
            } catch (_: SecurityException) {
                null
            } catch (_: Exception) {
                null
            } ?: continue
            for (child in children) {
                if (child.isDirectory) stack.addLast(Frame(child, currentDepth + 1))
            }
        }
        if (watches.size >= maxWatches) {
            Log.w(TAG, "Reached recursive watch cap ($maxWatches); deeper paths under ${root.absolutePath} will not be watched.")
        }
    }

    private fun registerSingle(dir: File) {
        if (watches.containsKey(dir.absolutePath)) return
        val observer = object : FileObserver(dir, eventMask) {
            override fun onEvent(event: Int, relativePath: String?) {
                relativePath ?: return
                val target = File(dir, relativePath)
                // If a new directory is created or moved into here, attach a watcher to it
                // so APKs dropped further down still trigger events.
                val createdOrMovedIn = (event and (FileObserver.CREATE or FileObserver.MOVED_TO)) != 0
                if (createdOrMovedIn && target.isDirectory) {
                    registerSubtree(target, depth = 0)
                }
                onEvent(event, target)

                // If the directory we are watching went away, release its watch entry.
                val removed = (event and (FileObserver.DELETE_SELF or FileObserver.MOVED_FROM)) != 0
                if (removed && relativePath.isEmpty()) {
                    watches.remove(dir.absolutePath)?.stopWatching()
                }
            }
        }
        try {
            observer.startWatching()
            watches[dir.absolutePath] = observer
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start watching ${dir.absolutePath}: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "RecursiveFileObserver"

        /** Default event mask — APK-relevant lifecycle events for files and dirs. */
        const val DEFAULT_EVENT_MASK: Int =
            FileObserver.CREATE or
                FileObserver.MOVED_TO or
                FileObserver.CLOSE_WRITE or
                FileObserver.DELETE_SELF or
                FileObserver.MOVED_FROM

        /**
         * Conservative cap. Real-world `inotify` user limits are typically 8K–512K depending on
         * OEM; staying at 2K leaves headroom for other apps and preserves device performance.
         */
        const val DEFAULT_MAX_WATCHES = 2_000

        /** Mirrors [com.safeguard.scan.DeepApkCollector] cap to keep behavior consistent. */
        const val DEFAULT_MAX_DEPTH = 50
    }
}
