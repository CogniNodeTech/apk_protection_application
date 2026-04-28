package com.safeguard.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.safeguard.MainActivity
import com.safeguard.security.layers.layer1.FileSystemMonitor
import com.safeguard.worker.ApkScanWorker
import java.io.File

/**
 * Roots monitored when [Environment.isExternalStorageManager] is granted. A single recursive
 * watcher on the storage root replaces the previous ~200 flat watchers and removes the silent
 * blind spot for nested folders such as
 * `Android/media/<pkg>/Telegram/Telegram Documents/<file>.apk`.
 */
private fun buildRecursiveRoots(hasAllFilesAccess: Boolean): List<File> {
    val storageRoot = Environment.getExternalStorageDirectory()
    if (hasAllFilesAccess && storageRoot != null && storageRoot.exists()) {
        // One recursive observer covers everything under shared storage. Inotify sub-watch
        // creation is bounded by RecursiveFileObserver.DEFAULT_MAX_WATCHES.
        return listOf(storageRoot)
    }
    // Fallback when only scoped READ_EXTERNAL_STORAGE is granted: cover the public dirs plus
    // Android/media (the only Android/-subtree apps can read without MES) recursively. Each
    // root is independent so a SecurityException in one tree does not silently kill the others.
    val publicDirs = listOf(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
    )
    val androidMedia = if (storageRoot != null) File(storageRoot, "Android/media") else null
    val legacyDownload = if (storageRoot != null) File(storageRoot, "Download") else null
    return (publicDirs + listOfNotNull(androidMedia, legacyDownload))
        .filterNotNull()
        .filter { it.exists() }
        .distinctBy { it.absolutePath }
}

/**
 * Foreground service that runs the file system monitor for real-time APK detection.
 * Monitors all accessible public directories and common app subfolders (Downloads, Documents,
 * Telegram, WhatsApp, etc.). When an APK is detected, enqueues a one-time scan via WorkManager.
 */
class FileObserverService : Service() {

    private var monitor: FileSystemMonitor? = null
    private var mediaStoreObserver: MediaStoreApkObserver? = null

    /** Debounce: avoid enqueueing the same path/URI within this window (FileObserver can fire CREATE + CLOSE_WRITE). */
    private val recentEnqueues = mutableMapOf<String, Long>()
    private val debounceMs = 3000L

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    private fun hasStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            return true
        }
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun enqueueScan(pathOrUri: String) {
        val now = System.currentTimeMillis()
        val prev = recentEnqueues[pathOrUri] ?: 0L
        if (now - prev < debounceMs) {
            Log.d(TAG, "APK debounced (already enqueued recently): $pathOrUri")
            return
        }
        recentEnqueues[pathOrUri] = now
        if (recentEnqueues.size > 200) {
            val cutoff = now - debounceMs * 2
            recentEnqueues.entries.removeAll { it.value < cutoff }
        }
        Log.i(TAG, "APK detected, enqueueing scan: $pathOrUri")
        val request = OneTimeWorkRequestBuilder<ApkScanWorker>()
            .setInputData(Data.Builder().putString(ApkScanWorker.KEY_APK_PATH, pathOrUri).build())
            .build()
        WorkManager.getInstance(applicationContext).enqueue(request)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        if (!hasStoragePermission()) {
            Log.w(TAG, "FileObserverService started but storage permission not granted; monitoring will not run.")
            return START_STICKY
        }
        val hasAllFilesAccess =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()
        val monitoredPaths = buildRecursiveRoots(hasAllFilesAccess)
        Log.i(
            TAG,
            "FileObserverService started; recursive roots=${monitoredPaths.size}, allFilesAccess=$hasAllFilesAccess"
        )
        if (monitoredPaths.isNotEmpty()) {
            monitor = FileSystemMonitor(monitoredPaths, recursive = true) { apkFile, _ ->
                enqueueScan(apkFile.absolutePath)
            }
            monitor?.startMonitoring()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaStoreObserver = MediaStoreApkObserver(contentResolver) { pathOrUri ->
                enqueueScan(pathOrUri)
            }
            mediaStoreObserver?.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        monitor?.stopMonitoring()
        monitor = null
        mediaStoreObserver?.stop()
        mediaStoreObserver = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "APK monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pending = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SafeGuard")
            .setContentText("Scanning for new APK files")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "SafeGuard"
        private const val CHANNEL_ID = "safeguard_monitor"
        private const val NOTIFICATION_ID = 1001
    }
}
