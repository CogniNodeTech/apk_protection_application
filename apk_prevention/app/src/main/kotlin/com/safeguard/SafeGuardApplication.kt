package com.safeguard

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.safeguard.core.domain.crash.CrashReporter
import com.safeguard.data.local.preferences.SecurePreferencesManager
import com.safeguard.service.FileObserverService
import com.safeguard.worker.FeedbackUploadWorker
import com.safeguard.worker.PeriodicDeepSweepWorker
import com.safeguard.worker.ThreatFeedSyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SafeGuardApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var crashReporter: CrashReporter

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var preferences: SecurePreferencesManager

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "SafeGuard application started")

        installCrashHandler()
        if (preferences.realTimeMonitoringEnabled) {
            val intent = Intent(this, FileObserverService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
        // Schedule the periodic deep sweep as a backstop for any APK drops the realtime
        // FileObserver missed (service not running, inotify cap, files older than the install).
        PeriodicDeepSweepWorker.schedule(this)
        // Threat-feed sync keeps the local malware/TLSH index fresh from MalwareBazaar via
        // the SafeGuard server. Idempotent — KEEP policy preserves an already-scheduled run.
        ThreatFeedSyncWorker.schedule(this)
        // Phase 3.2 feedback upload — drains the privacy-preserving feedback queue when
        // the user has opted in. Gated internally on the same toggles, so scheduling
        // unconditionally is safe; the worker is a no-op for users who never opt in.
        FeedbackUploadWorker.schedule(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun installCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception", throwable)
            if (::crashReporter.isInitialized) {
                try {
                    crashReporter.logException(thread, throwable)
                } catch (_: Exception) { /* fallback below */ }
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        private const val TAG = "SafeGuard"
    }
}
