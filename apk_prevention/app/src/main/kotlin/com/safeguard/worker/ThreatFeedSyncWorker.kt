package com.safeguard.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.safeguard.core.domain.repository.ThreatFeedRepository
import com.safeguard.core.domain.repository.ThreatFeedStatus
import com.safeguard.core.domain.repository.ThreatFeedStatusStore
import com.safeguard.core.domain.repository.ThreatFeedSyncResult
import com.safeguard.data.local.preferences.SecurePreferencesManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Periodically pulls fresh malware signatures from the SafeGuard threat-intel server and
 * upserts them into the on-device DB. Without this, Layer 2's hash + TLSH lookups score
 * against whatever subset of MalwareBazaar was bundled at install time, which goes stale
 * fast (abuse.ch ingests new APK samples every hour).
 *
 * Why 12 h and not "real-time" or "hourly"?
 *  - We refresh on a *device* schedule, not a *threat* schedule. The decision engine is
 *    online via Layer 6 (`/v1/verify`) for SHA-256 hits, so the local feed is mainly for
 *    TLSH variant detection (resilient to repacks) — daily-ish freshness is plenty.
 *  - Each pull is up to ~200 rows × ~150 bytes ≈ 30 KB. Twice a day keeps mobile-data
 *    impact negligible while still catching same-day campaigns.
 *  - Sync is gated on user-consented [SecurePreferencesManager.cloudVerificationEnabled];
 *    privacy onboarding flips this on after the user accepts.
 */
@HiltWorker
class ThreatFeedSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: ThreatFeedRepository,
    private val preferences: SecurePreferencesManager,
    private val statusStore: ThreatFeedStatusStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Honour the user's cloud-verification opt-out: that toggle gates *all* outbound
        // calls to the threat-intel server, including the threat-feed pull. We still write
        // a `SKIPPED` status row so the dashboard can explain *why* the threat database
        // looks stale ("Cloud verification disabled — sync paused"), instead of falling back
        // to the generic "you haven't synced lately" warning.
        if (!preferences.cloudVerificationEnabled) {
            Log.i(TAG, "Cloud verification disabled by user — skipping threat-feed sync.")
            val previous = statusStore.snapshot()
            statusStore.update(
                ThreatFeedStatus(
                    lastSuccessMs = previous.lastSuccessMs,
                    lastAttemptMs = System.currentTimeMillis(),
                    lastOutcome = ThreatFeedStatus.Outcome.SKIPPED,
                    lastFailureReason = "cloud_verification_disabled",
                    lastInsertedCount = previous.lastInsertedCount
                )
            )
            return@withContext Result.success()
        }

        when (val result = repository.sync()) {
            is ThreatFeedSyncResult.Success -> {
                Log.i(
                    TAG,
                    "Threat-feed sync ok: inserted=${result.insertedCount}," +
                        " batches=${result.batches}, cursor=${result.cursorAdvancedTo}"
                )
                Result.success()
            }
            is ThreatFeedSyncResult.Skipped -> {
                Log.i(TAG, "Threat-feed sync skipped: ${result.reason}")
                Result.success()
            }
            is ThreatFeedSyncResult.Failed -> {
                // Retryable: WorkManager's exponential backoff (configured in [schedule]) will
                // wait progressively longer before the next attempt. Inserted rows so far are
                // still durably in the DB because the repository only refuses to advance the
                // cursor — it does not roll back upserts. The repository has already written
                // the FAILED status row at the point we return here.
                Log.w(TAG, "Threat-feed sync failed: ${result.reason} (inserted=${result.insertedCount}); retrying.")
                Result.retry()
            }
        }
    }

    companion object {
        private const val TAG = "ThreatFeedSyncWorker"
        private const val UNIQUE_WORK_NAME = "safeguard_threat_feed_sync"
        private const val SYNC_INTERVAL_HOURS = 12L
        private const val INITIAL_BACKOFF_MINUTES = 30L

        /**
         * Enqueue (or refresh) the unique periodic sync. Safe to call multiple times — `KEEP`
         * preserves an existing schedule so we don't reset the inter-sync timer on every app
         * launch. Called from `SafeGuardApplication.onCreate`.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<ThreatFeedSyncWorker>(
                SYNC_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, INITIAL_BACKOFF_MINUTES, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
