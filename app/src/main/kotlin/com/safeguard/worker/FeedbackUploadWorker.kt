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
import com.safeguard.core.domain.feedback.FeedbackPrivacyGate
import com.safeguard.core.domain.feedback.FeedbackUploadResult
import com.safeguard.core.domain.repository.ScanFeedbackRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Periodic uploader for the Phase 3.2 privacy-preserving feedback queue.
 *
 * Runs every ~6h under "connected + not low battery" constraints so a power-saving
 * device or a metered hotspot never gets surprised by a feedback flush. The worker is
 * intentionally separate from [ThreatFeedSyncWorker]: that one is a *pull* (we want
 * fresher signatures, server is authoritative) on a 12h cadence, this one is a *push*
 * (we want to hand off opt-in user data) on a 6h cadence so a chatty device doesn't
 * accumulate weeks of events in the local queue if the server drops a few uploads.
 *
 * Privacy gating is **double-checked** here even though the repository already enforces
 * it: the repository's gate guards the *send*, this worker's gate guards even *waking up*
 * to read the queue. Either is sufficient on its own; together they tolerate either layer
 * regressing in a future refactor without leaking events.
 */
@HiltWorker
class FeedbackUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: ScanFeedbackRepository,
    private val privacyGate: FeedbackPrivacyGate
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Outermost gate: even reading the queue is gated on the user's three feedback
        // toggles. Repository enforces the same predicate before any HTTP call, but this
        // worker check means we don't even spin up DB I/O when the user has opted out.
        if (!privacyGate.isFeedbackAllowed()) {
            Log.i(TAG, "Feedback upload skipped — feedback or telemetry pref disabled.")
            return@withContext Result.success()
        }

        when (val outcome = repository.drainOnce()) {
            is FeedbackUploadResult.Success -> {
                Log.i(TAG, "Feedback upload ok: uploaded=${outcome.uploadedCount}")
                Result.success()
            }
            is FeedbackUploadResult.Skipped -> {
                // Queue empty (typical happy path) or pref flipped between gate + drain.
                // Either way nothing to retry.
                Log.d(TAG, "Feedback upload skipped — nothing to send.")
                Result.success()
            }
            is FeedbackUploadResult.Failed -> {
                // 4xx (`http_4xx` reason): probably a server-side validator change. We
                // currently retry these too, because the alternative — silently dropping
                // queued rows the user *did* explicitly consent to share — is worse than
                // a few wasted retries that the server will reject again. Operators who
                // see persistent 4xx in metrics roll back the validator change.
                Log.w(TAG, "Feedback upload failed: ${outcome.reason}; retrying with backoff.")
                Result.retry()
            }
        }
    }

    companion object {
        private const val TAG = "FeedbackUploadWorker"
        private const val UNIQUE_WORK_NAME = "safeguard_feedback_upload"
        private const val UPLOAD_INTERVAL_HOURS = 6L
        private const val INITIAL_BACKOFF_MINUTES = 30L

        /**
         * Schedule (or refresh) the unique periodic upload. `KEEP` so we don't reset the
         * inter-upload timer on every app launch — that would let a user who repeatedly
         * cold-starts the app starve the queue forever.
         *
         * Idempotent. Safe to call from `SafeGuardApplication.onCreate`.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<FeedbackUploadWorker>(
                UPLOAD_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    INITIAL_BACKOFF_MINUTES,
                    TimeUnit.MINUTES
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /**
         * Cancel the scheduled upload. Called from the settings toggle when the user opts
         * out so the worker stops running entirely (the privacy gate would already make
         * each run a no-op, but cancelling is both cleaner and saves a few job slots).
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
