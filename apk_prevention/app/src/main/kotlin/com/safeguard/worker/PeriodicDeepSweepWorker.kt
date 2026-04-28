package com.safeguard.worker

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.safeguard.scan.DeepApkCollector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Periodic backstop for [com.safeguard.service.FileObserverService].
 *
 * Real-time `FileObserver` watches can miss APK drops in three known cases:
 *   1. The service was not running (boot in progress, force-stop, low-memory kill).
 *   2. The recursive watcher hit its inotify cap and a deep subtree was unwatched.
 *   3. Files were created **before** the user granted MANAGE_EXTERNAL_STORAGE.
 *
 * Every ~6 hours we re-run the deep collector, find APK candidates (extension match **and**
 * magic-byte / `AndroidManifest.xml` confirmation for disguised drops), and enqueue an
 * [ApkScanWorker] per file. Existing per-scan de-duplication
 * (quarantine block list, SHA-256 cache, debounce in `FileObserverService`) prevents redundant
 * work when the same file was already scanned via the realtime path.
 *
 * Constraints: BATTERY_NOT_LOW + DEVICE_IDLE preferred (Android M+) so we never disrupt the
 * user. Off-network — this is purely local I/O.
 */
@HiltWorker
class PeriodicDeepSweepWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val externalRoot = Environment.getExternalStorageDirectory()
        if (externalRoot == null || !externalRoot.exists()) {
            Log.w(TAG, "External storage root unavailable; skipping sweep.")
            return@withContext Result.success()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            // Without all-files access this can only see a fraction of storage. The sweep still
            // helps in that subset, but we log so users wondering "why didn't it find X" have a
            // breadcrumb to follow.
            Log.i(TAG, "Sweep running without MANAGE_EXTERNAL_STORAGE; coverage will be partial.")
        }

        val sweepStart = System.currentTimeMillis()
        val result = DeepApkCollector.collectApks(externalRoot)
        Log.i(
            TAG,
            "Periodic sweep: dirs=${result.directoriesVisited}, apks=${result.apkFiles.size}," +
                " disguised=${result.disguisedApkCount}, truncated=${result.truncated}," +
                " elapsedMs=${System.currentTimeMillis() - sweepStart}"
        )

        val workManager = WorkManager.getInstance(context)
        result.apkFiles.forEach { apk ->
            val request = OneTimeWorkRequestBuilder<ApkScanWorker>()
                .setInputData(
                    Data.Builder().putString(ApkScanWorker.KEY_APK_PATH, apk.absolutePath).build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(TAG_SCAN_REQUEST)
                .build()
            workManager.enqueue(request)
        }
        Result.success()
    }

    companion object {
        private const val TAG = "PeriodicDeepSweepWorker"
        private const val UNIQUE_WORK_NAME = "safeguard_periodic_deep_sweep"
        private const val TAG_SCAN_REQUEST = "safeguard_sweep_scan"
        private const val SWEEP_INTERVAL_HOURS = 6L

        /**
         * Enqueues (or refreshes) the unique periodic sweep. Called from
         * `SafeGuardApplication.onCreate`; safe to call multiple times — `KEEP` policy means an
         * already-scheduled sweep is preserved.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setRequiresDeviceIdle(false)
                    }
                }
                .build()
            val request = PeriodicWorkRequestBuilder<PeriodicDeepSweepWorker>(
                SWEEP_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
