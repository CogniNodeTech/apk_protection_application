package com.safeguard.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.safeguard.core.domain.model.Verdict

object SafeGuardNotificationManager {

    private const val NOTIFICATION_ID_SCHEDULED = 2000

    /** Shows a reminder that it's time for the user's scheduled scan; tap opens the app. */
    fun showScheduledScanReminder(context: Context) {
        NotificationChannels.create(context)
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, NotificationChannels.INFO)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Scheduled scan")
            .setContentText("Time for your scheduled APK scan. Tap to open SafeGuard.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SCHEDULED, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showScanResult(context: Context, apkName: String, verdict: Verdict, riskScore: Int, scanId: String? = null) {
        NotificationChannels.create(context)
        val (channelId, title, text) = when (verdict) {
            Verdict.MALICIOUS -> Triple(
                NotificationChannels.CRITICAL,
                "DANGER - MALWARE BLOCKED",
                "\"$apkName\" is dangerous. We blocked it and moved it to quarantine. Tap to see details."
            )
            Verdict.SUSPICIOUS -> Triple(
                NotificationChannels.WARNING,
                "WARNING - Suspicious App",
                "\"$apkName\" has concerning signs. Risk: $riskScore/100. Tap to see details."
            )
            Verdict.SAFE -> Triple(
                NotificationChannels.INFO,
                "Safe to Install",
                "\"$apkName\" passed all security checks. Tap to open SafeGuard."
            )
            Verdict.UNKNOWN -> Triple(
                NotificationChannels.WARNING,
                "Scan Complete",
                "\"$apkName\" - result inconclusive. Tap to open SafeGuard."
            )
        }
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (scanId != null) putExtra(EXTRA_SCAN_ID, scanId)
        } ?: return
        val pendingIntent = PendingIntent.getActivity(
            context,
            (scanId ?: apkName).hashCode() and 0x7FFFFFFF,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        try {
            NotificationManagerCompat.from(context).notify(apkName.hashCode(), builder.build())
        } catch (_: SecurityException) {}
    }

    /**
     * Specialized notification for background installation scans.
     * Shows source and verdict clearly.
     */
    fun showInstallScanResult(
        context: Context,
        apkName: String,
        verdict: Verdict,
        source: String?,
        scanId: String? = null
    ) {
        NotificationChannels.create(context)
        val sourceText = source ?: "an unknown source"
        val (channelId, title, text) = when (verdict) {
            Verdict.MALICIOUS -> Triple(
                NotificationChannels.CRITICAL,
                "Threat Detected & Quarantined",
                "App \"$apkName\" installed from $sourceText is Malicious. It has been moved to quarantine."
            )
            Verdict.SUSPICIOUS -> Triple(
                NotificationChannels.WARNING,
                "Suspicious App Detected",
                "App \"$apkName\" installed from $sourceText shows risk indicators. Tap for details."
            )
            Verdict.SAFE -> Triple(
                NotificationChannels.INFO,
                "App Scanned & Safe",
                "App \"$apkName\" installed from $sourceText is Safe. No threats detected."
            )
            Verdict.UNKNOWN -> Triple(
                NotificationChannels.WARNING,
                "App Scan Complete",
                "App \"$apkName\" installed from $sourceText was scanned. Result inconclusive."
            )
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (scanId != null) putExtra(EXTRA_SCAN_ID, scanId)
        } ?: return

        val pendingIntent = PendingIntent.getActivity(
            context,
            (scanId ?: apkName).hashCode() and 0x7FFFFFFF,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        try {
            NotificationManagerCompat.from(context).notify(apkName.hashCode(), builder.build())
        } catch (_: SecurityException) {}
    }

    const val EXTRA_SCAN_ID = "com.safeguard.EXTRA_SCAN_ID"
}
