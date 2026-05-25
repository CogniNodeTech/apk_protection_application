package com.safeguard.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest

/**
 * Deep storage scanning needs broad read access. On Android 11+ this maps to
 * [Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION] when available.
 */
object StorageAccessHelper {

    fun canReadStorageDeep(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) return true
        }
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return readGranted
    }

    /**
     * Open system screen to grant “All files access” for this app (Android 11+).
     */
    fun createManageAllFilesIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
    }
}
