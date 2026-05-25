package com.safeguard.data.repository

import android.util.Log
import com.safeguard.core.domain.feedback.FeedbackPrivacyGate
import com.safeguard.core.domain.feedback.FeedbackUploadResult
import com.safeguard.core.domain.feedback.ScanFeedbackEvent
import com.safeguard.core.domain.repository.ScanFeedbackRepository
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao
import com.safeguard.data.local.database.entity.ScanFeedbackEventEntity
import com.safeguard.data.remote.api.ThreatIntelligenceApi
import com.safeguard.data.remote.dto.FeedbackEventJson
import com.safeguard.data.remote.dto.FeedbackUploadRequest
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production binding for [ScanFeedbackRepository] (Phase 3.2).
 *
 * The privacy gate lives **here** rather than at every call site. Three things must all
 * be true (see [FeedbackPrivacyGate.isFeedbackAllowed]) before a single byte leaves the
 * device:
 *  1. The explicit feedback opt-in (default OFF).
 *  2. The telemetry master toggle (default ON, but if a user flips it off they expect
 *     *all* outbound analytics to stop — feedback is a strict subset of telemetry).
 *  3. The US-state sharing opt-out kill switch (default OFF; when ON, no analytics
 *     leave the device regardless of other toggles).
 *
 * If any predicate fails, [enqueue] silently returns `false` and [drainOnce] returns
 * [FeedbackUploadResult.Skipped]. We don't *delete* the queue when the toggle flips —
 * that's a separate user action exposed via [clearAll] so the user can confirm "yes,
 * forget what I already shared". Re-enabling the toggle resumes uploads from where we
 * left off.
 *
 * Serialisation note: layer scores and triggered rule names are stored as JSON strings
 * inside the entity rather than as related Room tables. The cardinality is tiny
 * (≤ 7 layer scores, typically 0–3 rules per event) so the indirection isn't worth the
 * migration cost — and storing them inline makes [clearAll] a single `DELETE` instead of
 * needing to walk foreign-key cascades.
 */
@Singleton
class ScanFeedbackRepositoryImpl @Inject constructor(
    private val dao: ScanFeedbackEventDao,
    private val api: ThreatIntelligenceApi,
    private val privacyGate: FeedbackPrivacyGate,
    moshi: Moshi
) : ScanFeedbackRepository {

    private val layerScoresAdapter: JsonAdapter<Map<String, Float>> by lazy {
        moshi.adapter<Map<String, Float>>(
            Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Float::class.java)
        )
    }
    private val rulesAdapter: JsonAdapter<List<String>> by lazy {
        moshi.adapter<List<String>>(
            Types.newParameterizedType(List::class.java, String::class.java)
        )
    }

    /**
     * Wall-clock seam — production reads `System.currentTimeMillis()`, tests override the
     * field reflectively for deterministic `uploaded_at_ms` assertions.
     */
    internal var nowMs: () -> Long = { java.lang.System.currentTimeMillis() }

    override suspend fun enqueue(event: ScanFeedbackEvent): Boolean {
        if (!privacyGate.isFeedbackAllowed()) {
            // Important: silently drop, do not throw. The scan pipeline calls this on every
            // completion regardless of pref state; making it throw would force every caller
            // to wrap in a try/catch that re-implements the same gate.
            return false
        }
        try {
            dao.insert(event.toEntity())
        } catch (e: Exception) {
            // Persisting is best-effort — losing one feedback row is preferable to crashing
            // the scan pipeline. Log so dev builds catch unexpected DB errors in tests.
            Log.w(TAG, "Failed to enqueue scan feedback event id=${event.id}", e)
            return false
        }
        return true
    }

    override suspend fun drainOnce(batchLimit: Int): FeedbackUploadResult {
        if (!privacyGate.isFeedbackAllowed()) {
            // The user opted out between enqueue and drain. The queue may still contain
            // rows that were enqueued while the toggle was on — we deliberately keep them
            // until the user either flips the toggle back on (resume) or hits "purge" in
            // settings ([clearAll]). Skipping here means the worker won't retry-loop.
            return FeedbackUploadResult.Skipped
        }
        val batch = try {
            dao.nextBatch(batchLimit.coerceIn(1, MAX_BATCH))
        } catch (e: Exception) {
            Log.w(TAG, "DB read failed during feedback drain", e)
            return FeedbackUploadResult.Failed("db: ${e.javaClass.simpleName}")
        }
        if (batch.isEmpty()) return FeedbackUploadResult.Skipped

        val request = FeedbackUploadRequest(
            events = batch.map { it.toJsonOrNull() }.filterNotNull(),
            clientAppVersionCode = batch.first().appVersionCode,
            clientAndroidApiLevel = batch.first().androidApiLevel,
            uploadedAtMs = nowMs()
        )
        // If everything in the batch failed to deserialise (e.g. the JSON columns got
        // truncated by an external storage corruption tool), the request would be empty.
        // Drop the rows so we don't retry-loop forever; they're already useless to the server.
        if (request.events.isEmpty()) {
            try {
                dao.deleteByIds(batch.map { it.id })
            } catch (e: Exception) {
                Log.w(TAG, "Failed to drop unrecoverable feedback rows", e)
            }
            return FeedbackUploadResult.Failed("no_recoverable_rows")
        }

        val response = try {
            api.uploadFeedback(request)
        } catch (e: IOException) {
            return FeedbackUploadResult.Failed("network: ${e.javaClass.simpleName}")
        } catch (e: HttpException) {
            return FeedbackUploadResult.Failed("http_${e.code()}")
        } catch (e: Exception) {
            return FeedbackUploadResult.Failed("api: ${e.javaClass.simpleName}")
        }

        if (!response.isSuccessful) {
            // 4xx (validation) ⇒ no point retrying; tell the worker but flag as such so it
            // can decide whether to drop the rows. We currently keep rows on 4xx because
            // a server bug shipping a stricter validator would otherwise silently flush our
            // queue. Operators inspect the dev log sink to spot persistent 4xx and roll back.
            return FeedbackUploadResult.Failed("http_${response.code()}")
        }

        // Prefer the server's accepted_ids if present — gives us partial-success semantics
        // for free. Fall back to "all rows in the request were accepted" on older server
        // builds that don't return the field.
        val acceptedIds = response.body()?.acceptedIds
            ?.takeIf { it.isNotEmpty() }
            ?: request.events.map { it.id }

        try {
            dao.deleteByIds(acceptedIds)
        } catch (e: Exception) {
            Log.w(TAG, "DB delete failed after successful feedback upload", e)
            // The rows uploaded fine; we just can't delete them locally. Returning Failed
            // would re-upload them next tick (server dedupes by id), which is safe but
            // wasteful. Surface as Failed so the worker schedules backoff and the local DB
            // gets a chance to recover.
            return FeedbackUploadResult.Failed("db_delete: ${e.javaClass.simpleName}")
        }
        return FeedbackUploadResult.Success(uploadedCount = acceptedIds.size)
    }

    override suspend fun queuedCount(): Int = try {
        dao.count()
    } catch (e: Exception) {
        Log.w(TAG, "DB count failed", e)
        0
    }

    override suspend fun clearAll() {
        try {
            dao.deleteAll()
        } catch (e: Exception) {
            Log.w(TAG, "DB delete-all failed during feedback purge", e)
        }
    }

    private fun ScanFeedbackEvent.toEntity(): ScanFeedbackEventEntity =
        ScanFeedbackEventEntity(
            id = id,
            createdAtMs = createdAtMs,
            sha256 = sha256,
            verdict = verdict,
            confidence = confidence,
            packageName = packageName,
            versionCode = versionCode,
            layerScoresJson = layerScoresAdapter.toJson(layerScores),
            triggeredRulesJson = rulesAdapter.toJson(triggeredRules),
            androidApiLevel = androidApiLevel,
            appVersionCode = appVersionCode
        )

    private fun ScanFeedbackEventEntity.toJsonOrNull(): FeedbackEventJson? {
        val layers = runCatching { layerScoresAdapter.fromJson(layerScoresJson) }.getOrNull() ?: return null
        val rules = runCatching { rulesAdapter.fromJson(triggeredRulesJson) }.getOrNull() ?: return null
        return FeedbackEventJson(
            id = id,
            createdAtMs = createdAtMs,
            sha256 = sha256,
            verdict = verdict,
            confidence = confidence,
            packageName = packageName,
            versionCode = versionCode,
            layerScores = layers,
            triggeredRules = rules
        )
    }

    companion object {
        private const val TAG = "ScanFeedbackRepo"
        private const val MAX_BATCH = 200
    }
}
