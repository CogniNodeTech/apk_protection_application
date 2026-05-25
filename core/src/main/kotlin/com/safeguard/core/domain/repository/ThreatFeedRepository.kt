package com.safeguard.core.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Sync-side counterpart to [ThreatDatabaseRepository]. While [ThreatDatabaseRepository]
 * answers per-APK lookups during a scan, [ThreatFeedRepository] keeps that table fresh by
 * pulling incremental updates from the SafeGuard threat-intel server (which in turn proxies
 * abuse.ch's MalwareBazaar APK feed).
 *
 * Implementations:
 *  - persist a monotonically advancing cursor so repeat syncs are O(new) instead of O(all);
 *  - tolerate network failures by *not* advancing the cursor (next call will retry the same
 *    window) — only successful upserts move the cursor forward;
 *  - dedupe by SHA-256 (the existing primary key on `MalwareSignatureEntity`) so re-fetching
 *    a window doesn't double-insert rows;
 *  - expose a [Flow]<[ThreatFeedStatus]> via [observeStatus] so the dashboard can warn the
 *    user when their on-device threat data has gone stale.
 */
interface ThreatFeedRepository {

    /**
     * Pulls the next batch of malware signatures (since the last successful cursor),
     * upserts them into the local DB, and returns a [ThreatFeedSyncResult] for the caller
     * (a worker) to log.
     *
     * @param batchLimit upper bound on how many rows to request per HTTP call. Larger ⇒
     *  fewer round trips on a cold install, but heavier individual responses.
     * @param maxBatches safety cap on follow-up calls when the server reports `hasMore`.
     *  Guards against pathological loops (broken cursor, server bug) draining the device's
     *  battery / data.
     */
    suspend fun sync(batchLimit: Int = DEFAULT_BATCH_LIMIT, maxBatches: Int = DEFAULT_MAX_BATCHES): ThreatFeedSyncResult

    /**
     * Cold flow of the latest persisted [ThreatFeedStatus]. Re-emits whenever the worker
     * writes a new status (after each `doWork` invocation). Subscribers always see the
     * stored state on first collection — there is no "loading" transient state since the
     * status is plain `SharedPreferences`-backed.
     */
    fun observeStatus(): Flow<ThreatFeedStatus>

    companion object {
        const val DEFAULT_BATCH_LIMIT = 200
        const val DEFAULT_MAX_BATCHES = 5
    }
}

/**
 * Persistence boundary for the threat-feed cursor. Extracted so the repository can stay
 * unit-testable without standing up `EncryptedSharedPreferences` (which needs an Android
 * `Context`). Production binds this to `SecurePreferencesManager.lastThreatFeedSyncMs`.
 *
 * Contract:
 *  - `0L` is the sentinel for "never synced". Implementations must persist `0L` only if
 *    the value has never been written; once a real cursor is stored, it is monotonically
 *    non-decreasing.
 *  - Writes must be durable across process death (the worker may run after the app process
 *    has been killed for memory pressure).
 */
interface ThreatFeedCursorStore {
    var cursorMs: Long
}

/**
 * Persistence boundary for the *observable* part of threat-feed sync state. Separated from
 * [ThreatFeedCursorStore] because:
 *  - the cursor is server-coordinate (advances only on full success), whereas this status
 *    captures *every* attempt (success / skip / failure) so the dashboard can show the user
 *    why their threat database hasn't refreshed in 3 days even though `cursorMs` looks fine;
 *  - exposing a [Flow] keeps the repository declarative (`observeStatus()` is a pure
 *    `combine` over the underlying flows) without forcing the cursor store to also be
 *    flow-aware (it only ever needs synchronous reads on the worker thread).
 *
 * Implementations must persist all four fields atomically enough that an observer never sees
 * a torn snapshot (e.g. a `Failed` outcome with no `failureReason`). The default
 * `SharedPreferences`-backed binding writes them in a single `commit()`.
 */
interface ThreatFeedStatusStore {
    /**
     * Snapshot accessor for `worker`-side reads. Must be cheap (used at end of every `doWork`).
     */
    fun snapshot(): ThreatFeedStatus

    /**
     * Persist [status] and notify observers. Called after each [ThreatFeedRepository.sync]
     * call from the worker.
     */
    fun update(status: ThreatFeedStatus)

    /**
     * Cold flow that always emits the current persisted [ThreatFeedStatus] on collection
     * and re-emits on every [update] call.
     */
    fun observe(): Flow<ThreatFeedStatus>
}

/**
 * User-facing snapshot of the threat-feed sync. Drives the dashboard's "Threat database"
 * card and the staleness warning.
 *
 * @property lastSuccessMs epoch-ms of the last [ThreatFeedSyncResult.Success] we observed,
 *   or `0L` if the device has never synced. Stored separately from [lastAttemptMs] so a
 *   month-long offline streak doesn't overwrite the still-valid (if old) success timestamp.
 * @property lastAttemptMs epoch-ms of the most recent worker run (success, skip, or failure).
 *   Used to detect "we tried recently but keep failing" vs "we haven't even tried lately".
 * @property lastOutcome verdict from the last [ThreatFeedSyncResult]. The dashboard renders
 *   different messaging per outcome.
 * @property lastFailureReason populated only when [lastOutcome] is [Outcome.FAILED]; cleared
 *   on success/skip so the dashboard never shows a stale error message.
 * @property lastInsertedCount rows upserted in the most recent successful run. `0` for
 *   never-synced devices and after pure-failure runs.
 */
data class ThreatFeedStatus(
    val lastSuccessMs: Long = 0L,
    val lastAttemptMs: Long = 0L,
    val lastOutcome: Outcome = Outcome.NEVER,
    val lastFailureReason: String? = null,
    val lastInsertedCount: Int = 0
) {
    enum class Outcome {
        /** Device has never run the worker (fresh install, or cloud verification opted out). */
        NEVER,

        /** Last run completed end-to-end. */
        SUCCESS,

        /**
         * Last run was a deliberate no-op (cloud verification disabled, etc.). Distinct from
         * [SUCCESS] because the cursor did not advance — we don't want to claim we just
         * refreshed when we actually skipped.
         */
        SKIPPED,

        /** Last run errored out (network, server, DB, parser). */
        FAILED
    }
}

/**
 * Outcome of one [ThreatFeedRepository.sync] call. The worker logs and surfaces these in
 * telemetry; [Failed] is also used to drive WorkManager retry/backoff decisions.
 */
sealed interface ThreatFeedSyncResult {
    /**
     * The sync ran end-to-end. [insertedCount] is the number of rows successfully upserted
     * across all batches. [batches] is how many HTTP calls were made (≥ 1, ≤ maxBatches).
     * [cursorAdvancedTo] is the new cursor persisted to prefs (0 on first-run; otherwise
     * monotonically ≥ the previous cursor).
     */
    data class Success(
        val insertedCount: Int,
        val batches: Int,
        val cursorAdvancedTo: Long
    ) : ThreatFeedSyncResult

    /**
     * Sync was a no-op because the device is offline / cloud verification was disabled / a
     * required dependency was missing. The cursor is **not** advanced; the next periodic
     * tick will retry without backoff.
     */
    data class Skipped(val reason: String) : ThreatFeedSyncResult

    /**
     * Sync failed mid-flight. The cursor is **not** advanced; WorkManager should retry with
     * exponential backoff. [insertedCount] reflects rows that *were* committed before the
     * failure (so partial progress survives).
     */
    data class Failed(
        val reason: String,
        val insertedCount: Int = 0
    ) : ThreatFeedSyncResult
}
