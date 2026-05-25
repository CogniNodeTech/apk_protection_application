package com.safeguard.core.domain.repository

import com.safeguard.core.domain.feedback.FeedbackUploadResult
import com.safeguard.core.domain.feedback.ScanFeedbackEvent

/**
 * Persistence + upload surface for the Phase 3.2 privacy-preserving feedback channel.
 *
 * The repository is the single choke point through which every feedback event must pass:
 *  - [enqueue] is called from the scan pipeline (or its facade) on every completed scan,
 *    *before* checking the user opt-in. The repository internally short-circuits the
 *    enqueue when the pref is off so the call sites stay simple. This is intentional —
 *    moving the opt-in check to the call site means every new caller has to remember to
 *    repeat it.
 *  - [drainOnce] is called by the [com.safeguard.worker.FeedbackUploadWorker] periodic job
 *    under network/battery constraints. It uploads at most [batchLimit] rows per call so
 *    a long-offline device doesn't hammer the backend in a single burst.
 *  - [clearAll] is exposed so the settings UI can offer "purge queued feedback" — useful
 *    when a privacy-conscious user opts out and wants to confirm nothing is left.
 *
 * No method here exposes the queued events to UI code. The events are intentionally
 * write-only on the device: the user can opt in / opt out / purge, but cannot inspect
 * individual rows. Surfacing them would invite "scrub these specific entries" workflows
 * that we don't want to take on (the events are already minimised at construction).
 */
interface ScanFeedbackRepository {

    /**
     * Persist [event] to the local queue if and only if the user has opted in. Returns
     * `true` when the event was enqueued, `false` when the opt-in was off (the default).
     *
     * This call MUST be cheap (O(1) DB insert) — the scan pipeline calls it on every
     * completion and we don't want to drag main-thread budget. Production binds this to
     * a Room INSERT inside `withContext(Dispatchers.IO)`.
     */
    suspend fun enqueue(event: ScanFeedbackEvent): Boolean

    /**
     * Upload up to [batchLimit] queued events, deleting on success.
     *
     * Returns:
     *  - [FeedbackUploadResult.Success] with the count actually uploaded,
     *  - [FeedbackUploadResult.Skipped] when the queue is empty or the pref is off (the
     *    worker uses this to early-exit without triggering retry/backoff),
     *  - [FeedbackUploadResult.Failed] on transient errors. The repository does NOT
     *    delete failed rows; the worker schedules a retry.
     *
     * @param batchLimit upper bound on rows shipped in one HTTP call. Larger ⇒ fewer
     *  network round-trips on a chatty device but heavier individual responses; the
     *  default of 50 is sized to fit comfortably under the 1 MB nginx body cap.
     */
    suspend fun drainOnce(batchLimit: Int = DEFAULT_BATCH_LIMIT): FeedbackUploadResult

    /** Returns the current count of queued events. Cheap; used by tests and UI badges. */
    suspend fun queuedCount(): Int

    /**
     * Wipe every queued event. Called from the settings "clear feedback queue" action.
     * Does NOT touch the user's opt-in pref; the caller decides whether to also flip the
     * toggle off.
     */
    suspend fun clearAll()

    companion object {
        const val DEFAULT_BATCH_LIMIT = 50
    }
}
