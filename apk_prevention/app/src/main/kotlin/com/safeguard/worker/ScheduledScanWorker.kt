package com.safeguard.worker

import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.safeguard.data.local.preferences.SecurePreferencesManager
import com.safeguard.notification.SafeGuardNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Runs at the scheduled time: shows a "Time for your scheduled scan" notification,
 * then enqueues the next run (daily or weekly at the same time).
 */
@HiltWorker
class ScheduledScanWorker @AssistedInject constructor(
    @Assisted private val context: android.content.Context,
    @Assisted params: WorkerParameters,
    private val preferences: SecurePreferencesManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!preferences.scheduleScanEnabled) return Result.success()
        SafeGuardNotificationManager.showScheduledScanReminder(context)
        scheduleNext()
        return Result.success()
    }

    private fun scheduleNext() {
        val hour = inputData.getInt(KEY_HOUR, preferences.scheduleHour).coerceIn(0, 23)
        val minute = inputData.getInt(KEY_MINUTE, preferences.scheduleMinute).coerceIn(0, 59)
        val frequency = inputData.getString(KEY_FREQUENCY) ?: preferences.scheduleFrequency
        val delayMillis = nextDelayMillis(hour, minute, frequency)
        if (delayMillis <= 0) return
        
        val constraints = androidx.work.Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()

        val request = OneTimeWorkRequestBuilder<ScheduledScanWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setInputData(
                Data.Builder()
                    .putInt(KEY_HOUR, hour)
                    .putInt(KEY_MINUTE, minute)
                    .putString(KEY_FREQUENCY, frequency)
                    .build()
            )
            .addTag(TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun nextDelayMillis(hour: Int, minute: Int, frequency: String): Long {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        var next = cal.timeInMillis
        if (next <= now) {
            if (frequency == SecurePreferencesManager.FREQ_WEEKLY) {
                cal.add(Calendar.DAY_OF_YEAR, 7)
            } else {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            next = cal.timeInMillis
        }
        return next - now
    }

    companion object {
        const val TAG = "scheduled_scan"
        const val UNIQUE_NAME = "scheduled_scan"
        const val KEY_HOUR = "schedule_hour"
        const val KEY_MINUTE = "schedule_minute"
        const val KEY_FREQUENCY = "schedule_frequency"
    }
}
