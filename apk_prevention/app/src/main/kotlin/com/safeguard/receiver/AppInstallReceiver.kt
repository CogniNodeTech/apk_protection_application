package com.safeguard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.safeguard.worker.AppInstallScanWorker

/**
 * Triggers a background scan whenever an APK is **installed** or **updated** on the device.
 *
 * Listens for two complementary system broadcasts:
 *   - [Intent.ACTION_PACKAGE_ADDED]    — fresh installs (and updates with EXTRA_REPLACING=true,
 *                                        which we de-duplicate against PACKAGE_REPLACED).
 *   - [Intent.ACTION_PACKAGE_REPLACED] — completed package updates. Scanned because malicious
 *                                        actors weaponize updates of previously-benign apps
 *                                        (a recurring real-world Android campaign vector).
 *
 * Intentionally excludes [Intent.ACTION_PACKAGE_REMOVED] / [Intent.ACTION_PACKAGE_FULLY_REMOVED]
 * — there is no APK to scan once the package is uninstalled.
 */
class AppInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: return
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                // PACKAGE_ADDED with EXTRA_REPLACING=true also fires for updates. We rely on the
                // separate PACKAGE_REPLACED broadcast for that path so updates are not scanned
                // twice.
                if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return
                enqueueScan(context, packageName, reason = "fresh_install")
            }
            Intent.ACTION_PACKAGE_REPLACED -> {
                enqueueScan(context, packageName, reason = "package_update")
            }
        }
    }

    private fun enqueueScan(context: Context, packageName: String, reason: String) {
        Log.i(TAG, "Install event detected ($reason): $packageName — enqueueing background scan.")
        val workManager = WorkManager.getInstance(context)
        val scanRequest = OneTimeWorkRequestBuilder<AppInstallScanWorker>()
            .setInputData(
                Data.Builder()
                    .putString(AppInstallScanWorker.KEY_PACKAGE_NAME, packageName)
                    .build()
            )
            .addTag(TAG)
            .build()
        workManager.enqueue(scanRequest)
    }

    companion object {
        private const val TAG = "AppInstallReceiver"
    }
}
