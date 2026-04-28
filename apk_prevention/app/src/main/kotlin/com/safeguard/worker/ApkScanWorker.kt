package com.safeguard.worker

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.safeguard.core.domain.model.Action
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.repository.QuarantineRepository
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase
import com.safeguard.core.domain.usecase.ScanAPKUseCase
import com.safeguard.notification.SafeGuardNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import java.io.File
import java.security.MessageDigest
import java.util.UUID

@HiltWorker
class ApkScanWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val scanAPKUseCase: ScanAPKUseCase,
    private val quarantineAPKUseCase: QuarantineAPKUseCase,
    private val quarantineRepository: QuarantineRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val pathOrUri = inputData.getString(KEY_APK_PATH) ?: return@withContext Result.failure()
        val pathAppName = inputData.getString(KEY_APP_NAME)
        val (file, isTempFromUri) = resolveToFile(pathOrUri) ?: return@withContext Result.failure()
        var contentUriToDelete: Uri? = if (pathOrUri.startsWith("content://")) Uri.parse(pathOrUri) else null
        val displayName = pathAppName ?: if (contentUriToDelete != null) getDisplayName(contentUriToDelete) else null
        return@withContext try {
            // Check if this APK was previously permanently deleted (blocked)
            val apkName = displayName ?: file.name
            val isBlocked = try { quarantineRepository.isApkBlocked(apkName, sha256(file)) } catch (_: Exception) { false }
            if (isBlocked) {
                // Auto-delete blocked APKs without scanning
                Log.i(TAG, "Blocked APK detected; auto-deleting")
                file.delete()
                if (contentUriToDelete != null) {
                    try { context.contentResolver.delete(contentUriToDelete, null, null) } catch (_: Exception) { }
                }
                SafeGuardNotificationManager.showScanResult(
                    context,
                    apkName,
                    Verdict.MALICIOUS,
                    100,
                    "blocked_${System.currentTimeMillis()}"
                )
                return@withContext Result.success()
            }

            val result = scanAPKUseCase.execute(file, displayName)
            val shouldQuarantine = result.finalVerdict == Verdict.MALICIOUS ||
                result.finalVerdict == Verdict.SUSPICIOUS && (result.recommendedAction == Action.QUARANTINE || result.recommendedAction == Action.BLOCK)
            if (shouldQuarantine) {
                try {
                    quarantineAPKUseCase.execute(file.absolutePath, result)
                } catch (e: Exception) {
                    Log.e(TAG, "Quarantine failed", e)
                }
                if (contentUriToDelete != null) {
                    try { context.contentResolver.delete(contentUriToDelete, null, null) } catch (_: Exception) { /* not all URIs support delete */ }
                }
            } else {
                if (isTempFromUri) file.delete()
            }
            SafeGuardNotificationManager.showScanResult(
                context,
                result.apkName,
                result.finalVerdict,
                result.overallRiskScore,
                result.id
            )
            Result.success()
        } catch (e: Exception) {
            if (isTempFromUri) file.delete()
            if (runAttemptCount >= MAX_RUN_ATTEMPTS) Result.failure() else Result.retry()
        }
    }

    /** Resolves display name from a content URI (e.g. "MyApp.apk") for use in scan result. */
    private fun getDisplayName(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)?.takeIf { it.isNotBlank() }
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Returns (File, isTemp) or null. If pathOrUri starts with content://, copies to a temp file.
     */
    private fun resolveToFile(pathOrUri: String): Pair<File, Boolean>? {
        if (pathOrUri.startsWith("content://")) {
            val uri = Uri.parse(pathOrUri)
            val tempFile = File(context.cacheDir, "scan_${UUID.randomUUID()}.apk")
            return try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                if (tempFile.exists()) Pair(tempFile, true) else null
            } catch (e: Exception) {
                null
            }
        }
        val file = File(pathOrUri)
        return if (file.exists()) Pair(file, false) else null
    }

    companion object {
        private const val TAG = "SafeGuard"
        const val KEY_APK_PATH = "apk_path"
        const val KEY_APP_NAME = "app_name"
        private const val MAX_RUN_ATTEMPTS = 3
    }

    private fun sha256(file: File): String? {
        return try {
            if (!file.exists() || !file.isFile) return null
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } > 0) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            null
        }
    }
}
