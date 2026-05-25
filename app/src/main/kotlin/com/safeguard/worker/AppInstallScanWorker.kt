package com.safeguard.worker

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.safeguard.core.domain.model.Action
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase
import com.safeguard.core.domain.usecase.ScanAPKUseCase
import com.safeguard.notification.SafeGuardNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@HiltWorker
class AppInstallScanWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val scanAPKUseCase: ScanAPKUseCase,
    private val quarantineAPKUseCase: QuarantineAPKUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val packageName = inputData.getString(KEY_PACKAGE_NAME) ?: return@withContext Result.failure()
        
        Log.i(TAG, "Background installed-app scan triggered")
        
        return@withContext try {
            val pm = context.packageManager
            val pkgInfo = pm.getPackageInfo(packageName, 0)
            val appInfo = pkgInfo.applicationInfo ?: return@withContext Result.failure()
            val apkFile = File(appInfo.sourceDir)
            
            if (!apkFile.exists()) {
                Log.e(TAG, "Installed package APK file not found")
                return@withContext Result.failure()
            }

            val appLabel = pm.getApplicationLabel(appInfo).toString()
            
            // Execute scan
            val result = scanAPKUseCase.execute(apkFile, appLabel)
            Log.i(TAG, "Installed-app scan completed with verdict=${result.finalVerdict}")

            val shouldQuarantine = result.finalVerdict == Verdict.MALICIOUS ||
                (result.finalVerdict == Verdict.SUSPICIOUS && (result.recommendedAction == Action.QUARANTINE || result.recommendedAction == Action.BLOCK))

            if (shouldQuarantine) {
                try {
                    quarantineAPKUseCase.execute(apkFile.absolutePath, result)
                    Log.i(TAG, "Installed-app auto-quarantined")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to quarantine installed-app", e)
                }
            }

            // Show specialized installation notification
            SafeGuardNotificationManager.showInstallScanResult(
                context = context,
                apkName = result.apkName,
                verdict = result.finalVerdict,
                source = result.installerSource,
                scanId = result.id
            )

            Result.success()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Installed package no longer found on device", e)
            Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning installed package", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val TAG = "AppInstallScanWorker"
        const val KEY_PACKAGE_NAME = "package_name"
    }
}
