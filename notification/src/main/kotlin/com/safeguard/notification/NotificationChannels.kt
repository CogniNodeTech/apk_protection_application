package com.safeguard.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val CRITICAL = "safeguard_critical"
    const val WARNING = "safeguard_warning"
    const val INFO = "safeguard_info"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CRITICAL, "Malware Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Critical malware detection"
                enableVibration(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(WARNING, "Suspicious APK", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Suspicious app warnings"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(INFO, "Safe Install", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Safe app scan results"
            }
        )
    }
}
