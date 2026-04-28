package com.safeguard.service

import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import java.util.concurrent.atomic.AtomicReference

/**
 * Observes MediaStore (Downloads) for new APK files on Android 10+.
 * When the content provider changes, debounces and queries for recent APKs, then invokes
 * [onApkFound] with the content URI (or path if available). Scoped storage limits what we can
 * access; this improves coverage for standard download locations.
 */
class MediaStoreApkObserver(
    private val contentResolver: ContentResolver,
    private val onApkFound: (String) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private val debounceRunnable = AtomicReference<Runnable?>(null)
    private var contentObserver: ContentObserver? = null
    private val seenIds = mutableSetOf<Long>()
    private var initialQueryDone = false
    private val debounceMs = 800L

    fun start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        contentObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                scheduleQuery()
            }
        }
        contentResolver.registerContentObserver(uri, true, contentObserver!!)
        // Populate seenIds with current APKs so we only report newly added ones (sync so we don't miss races)
        queryRecentApks(notifyNewOnly = false)
    }

    fun stop() {
        debounceRunnable.getAndSet(null)?.let { handler.removeCallbacks(it) }
        contentObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
        contentObserver = null
        seenIds.clear()
    }

    private fun scheduleQuery() {
        debounceRunnable.getAndSet(null)?.let { handler.removeCallbacks(it) }
        val runnable = Runnable {
            debounceRunnable.set(null)
            queryRecentApks(notifyNewOnly = true)
        }
        debounceRunnable.set(runnable)
        handler.postDelayed(runnable, debounceMs)
    }

    /**
     * @param notifyNewOnly if false, only populate seenIds (initial run). if true, call onApkFound for new ids.
     */
    private fun queryRecentApks(notifyNewOnly: Boolean = true) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.DATE_ADDED
        )
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} LIKE ? OR ${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%.apk", "%.APK")
        val sortOrder = "${MediaStore.Downloads.DATE_ADDED} DESC"
        val cursor: Cursor? = try {
            contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
        } catch (e: SecurityException) {
            null
        }
        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            while (c.moveToNext()) {
                val id = c.getLong(idColumn)
                val isNew = seenIds.add(id)
                if (notifyNewOnly && isNew) {
                    val contentUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                    onApkFound(contentUri.toString())
                }
            }
            if (!initialQueryDone) initialQueryDone = true
            if (seenIds.size > 500) {
                val toRemove = seenIds.sorted().drop(250)
                seenIds.removeAll(toRemove.toSet())
            }
        }
    }
}
