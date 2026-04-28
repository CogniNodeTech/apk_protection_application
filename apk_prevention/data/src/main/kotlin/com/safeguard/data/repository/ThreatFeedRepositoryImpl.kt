package com.safeguard.data.repository

import android.util.Log
import com.safeguard.core.domain.repository.ThreatFeedCursorStore
import com.safeguard.core.domain.repository.ThreatFeedRepository
import com.safeguard.core.domain.repository.ThreatFeedStatus
import com.safeguard.core.domain.repository.ThreatFeedStatusStore
import com.safeguard.core.domain.repository.ThreatFeedSyncResult
import com.safeguard.data.local.database.dao.MalwareSignatureDao
import com.safeguard.data.local.database.entity.MalwareSignatureEntity
import com.safeguard.data.remote.api.ThreatIntelligenceApi
import com.safeguard.data.remote.dto.ThreatFeedItemJson
import com.safeguard.data.remote.dto.ThreatFeedResponseJson
import com.safeguard.data.remote.signing.ThreatFeedSignatureVerifier
import com.safeguard.data.remote.signing.VerifiedThreatFeed
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pulls fresh malware signatures from the SafeGuard threat-intel server (`GET /v1/threat-feed`)
 * and upserts them into the local Room DB so the on-device Layer 2 (HashValidator) actually
 * has a population to compare against. Without this sync, the device's malware table would
 * stay at whatever was bundled at install time and the new TLSH path from Phase 2.1 would
 * have nothing to score against.
 *
 * Cursor semantics (defensive against the failure modes I've seen in field deployments):
 *  - The cursor is read once at the start of [sync] and only persisted if the **whole**
 *    chain of batches finishes cleanly. If batch 3 of 5 fails, we still keep batches 1-2
 *    in the DB (they're idempotent upserts) but don't move the cursor — the next periodic
 *    tick re-fetches the same window, which is cheap because abuse.ch returns rows
 *    newest-first and the dedupe is on the SHA-256 PK.
 *  - The server's `next_cursor_ms` is the source of truth even for empty responses (so
 *    a quiet hour on abuse.ch advances us past the dead window, instead of looping over it
 *    forever once the server's `since` filter starts dropping all rows).
 */
@Singleton
class ThreatFeedRepositoryImpl @Inject constructor(
    private val api: ThreatIntelligenceApi,
    private val malwareDao: MalwareSignatureDao,
    private val cursorStore: ThreatFeedCursorStore,
    private val statusStore: ThreatFeedStatusStore,
    private val signatureVerifier: ThreatFeedSignatureVerifier,
    private val moshi: Moshi
) : ThreatFeedRepository {

    /**
     * Adapter for the *inner* payload (post-verification). Moshi will reject unexpected
     * top-level fields silently by default, but we don't enable `failOnUnknown` here so a
     * server adding new optional fields (e.g. `provider_metadata`) doesn't brick the sync.
     */
    private val payloadAdapter: JsonAdapter<ThreatFeedResponseJson> by lazy {
        moshi.adapter(ThreatFeedResponseJson::class.java).lenient()
    }

    /**
     * Wall-clock seam. Production reads `System.currentTimeMillis()`; tests override the
     * field reflectively (or via the test-only [withFixedNow] helper) so assertions on
     * persisted `lastAttemptMs` aren't flaky against the real clock.
     */
    internal var nowMs: () -> Long = { java.lang.System.currentTimeMillis() }

    override suspend fun sync(batchLimit: Int, maxBatches: Int): ThreatFeedSyncResult {
        val safeBatchLimit = batchLimit.coerceIn(1, MAX_BATCH_LIMIT)
        val safeMaxBatches = maxBatches.coerceIn(1, ABSOLUTE_BATCH_CAP)
        // Single read of the previous status so we don't see a torn snapshot if the worker
        // races with the dashboard reading `observeStatus()`. We carry forward `lastSuccessMs`
        // on non-success outcomes — the dashboard wants to keep showing "you last refreshed
        // 2 days ago" even if today's attempt failed.
        val attemptStartedAtMs = nowMs()
        val previousStatus = statusStore.snapshot()

        // First-sync sentinel = `null`. The server treats null as "no since filter" and
        // serves the latest [safeBatchLimit] rows. Subsequent syncs pass the persisted cursor.
        val initialCursor = cursorStore.cursorMs.takeIf { it > 0 }
        var cursor = initialCursor
        var insertedTotal = 0
        var batchesUsed = 0

        // Plain `for` loop instead of `repeat { ... }` because we need to break out early on
        // `has_more=false`. `return@repeat` only returns from the lambda — the surrounding
        // for-loop inside `repeat` keeps iterating, which would issue spurious follow-up
        // requests after the server already said the window was drained.
        for (batchIdx in 0 until safeMaxBatches) {
            val rawBody = try {
                api.getThreatFeed(since = cursor, limit = safeBatchLimit).use { it.string() }
            } catch (e: IOException) {
                Log.w(TAG, "Network failure during threat-feed sync (batch=${batchIdx + 1})", e)
                return failed("network: ${e.javaClass.simpleName}", insertedTotal, attemptStartedAtMs, previousStatus)
            } catch (e: HttpException) {
                Log.w(TAG, "Server error during threat-feed sync (status=${e.code()})", e)
                return failed("http_${e.code()}", insertedTotal, attemptStartedAtMs, previousStatus)
            } catch (e: RuntimeException) {
                Log.w(TAG, "Unexpected error during threat-feed sync", e)
                return failed("parse: ${e.javaClass.simpleName}", insertedTotal, attemptStartedAtMs, previousStatus)
            }

            // Step 1: signature verification. Phase 3.1 wraps the response in a signed
            // envelope; verifier returns either the *inner* payload JSON (Signed/Unsigned)
            // or a Rejected result that we surface as a typed sync failure. The reason
            // strings ("signature_invalid", "unsigned_response_in_signed_build", etc.) are
            // visible on the dashboard tile so users can tell a real MITM apart from a
            // stale build.
            val verified = signatureVerifier.verify(rawBody)
            val payloadJson = when (verified) {
                is VerifiedThreatFeed.Unsigned -> verified.payloadJson
                is VerifiedThreatFeed.Signed -> verified.payloadJson
                is VerifiedThreatFeed.Rejected -> {
                    Log.w(TAG, "Threat-feed signature rejected: ${verified.reason}")
                    return failed("sig: ${verified.reason}", insertedTotal, attemptStartedAtMs, previousStatus)
                }
            }

            // Step 2: inner-payload parse. Now that we trust the bytes, hand them to Moshi.
            val response = try {
                payloadAdapter.fromJson(payloadJson)
                    ?: return failed("payload_null", insertedTotal, attemptStartedAtMs, previousStatus)
            } catch (e: Exception) {
                Log.w(TAG, "Inner payload parse failed", e)
                return failed("parse_inner: ${e.javaClass.simpleName}", insertedTotal, attemptStartedAtMs, previousStatus)
            }

            batchesUsed = batchIdx + 1
            val entities = response.items.mapNotNull { it.toEntityOrNull() }
            if (entities.isNotEmpty()) {
                val dbResult = runCatching { malwareDao.insertAll(entities) }
                if (dbResult.isFailure) {
                    val e = dbResult.exceptionOrNull() ?: RuntimeException("unknown db error")
                    Log.w(TAG, "DB upsert failed during threat-feed sync", e)
                    return failed("db: ${e.javaClass.simpleName}", insertedTotal, attemptStartedAtMs, previousStatus)
                }
                insertedTotal += entities.size
            }

            cursor = response.nextCursorMs
            if (!response.hasMore) break
            // hasMore = true and we still have batch budget — loop again with the new cursor.
        }

        // Persist the cursor only after a complete successful pass through the loop.
        cursor?.let { cursorStore.cursorMs = it }
        // ALSO persist the user-facing status tile, *atomically*. Done after the cursor write
        // so a crash between the two leaves us with cursor-advanced-but-status-stale, which
        // is harmless (next sync will rewrite the status); the inverse — status-success but
        // cursor-not-advanced — would cause us to re-fetch the same window forever, hiding a
        // bug behind a "happy" dashboard tile.
        statusStore.update(
            ThreatFeedStatus(
                lastSuccessMs = attemptStartedAtMs,
                lastAttemptMs = attemptStartedAtMs,
                lastOutcome = ThreatFeedStatus.Outcome.SUCCESS,
                lastFailureReason = null,
                lastInsertedCount = insertedTotal
            )
        )
        Log.i(
            TAG,
            "Threat-feed sync done: inserted=$insertedTotal, batches=$batchesUsed," +
                " cursor=${cursor ?: "null"} (was=${initialCursor ?: "null"})"
        )
        return ThreatFeedSyncResult.Success(
            insertedCount = insertedTotal,
            batches = batchesUsed,
            cursorAdvancedTo = cursor ?: 0L
        )
    }

    override fun observeStatus(): Flow<ThreatFeedStatus> = statusStore.observe()

    /**
     * Persists a [ThreatFeedStatus.Outcome.FAILED] row that *preserves* the previous
     * [ThreatFeedStatus.lastSuccessMs] / [ThreatFeedStatus.lastInsertedCount]. The dashboard
     * uses those carried-forward values to render messaging like "Last refreshed 2 days ago
     * — last attempt failed (network)" instead of regressing the success counter to zero
     * the moment a single sync errors.
     */
    private fun failed(
        reason: String,
        insertedSoFar: Int,
        attemptMs: Long,
        previous: ThreatFeedStatus
    ): ThreatFeedSyncResult.Failed {
        statusStore.update(
            ThreatFeedStatus(
                lastSuccessMs = previous.lastSuccessMs,
                lastAttemptMs = attemptMs,
                lastOutcome = ThreatFeedStatus.Outcome.FAILED,
                lastFailureReason = reason,
                lastInsertedCount = previous.lastInsertedCount
            )
        )
        return ThreatFeedSyncResult.Failed(reason, insertedSoFar)
    }


    /**
     * Server-side validation already enforces the SHA-256 length / TLSH length contracts,
     * but we re-check defensively so a misbehaving proxy or cached MITM response can't
     * inject malformed rows that later poison TLSH similarity scoring.
     */
    private fun ThreatFeedItemJson.toEntityOrNull(): MalwareSignatureEntity? {
        val sha = sha256.trim().lowercase()
        if (sha.length != SHA256_HEX_LEN || !sha.all { it.isHexLower() }) return null
        val sha512Normalised = sha512?.trim()?.lowercase()?.takeIf { candidate ->
            candidate.length == SHA512_HEX_LEN && candidate.all { it.isHexLower() }
        }
        val fuzzy = fuzzyHash.trim().uppercase().removePrefix("T1")
        if (fuzzy.length != TLSH_HEX_LEN || !fuzzy.all { it.isHexUpper() }) return null
        val severityClamped = severity.coerceIn(0, 100)
        return MalwareSignatureEntity(
            sha256 = sha,
            sha512 = sha512Normalised,
            fuzzyHash = fuzzy,
            threatName = threatName.ifBlank { "Unknown" },
            threatFamily = threatFamily?.takeIf { it.isNotBlank() },
            severity = severityClamped,
            firstSeen = firstSeenMs,
            source = source?.takeIf { it.isNotBlank() } ?: "malwarebazaar"
        )
    }

    /**
     * Custom hex predicates instead of `Char.isDigit() || it in 'a'..'f'` to side-step a
     * name collision with Kotlin stdlib's `Char.isHexDigit()` (added in 2.0). The collision
     * triggers `'isHexDigit' is a member and an extension at the same time` on this module's
     * Kotlin compiler.
     */
    private fun Char.isHexLower(): Boolean =
        this in '0'..'9' || this in 'a'..'f'

    private fun Char.isHexUpper(): Boolean =
        this in '0'..'9' || this in 'A'..'F'

    @Suppress("unused") // Reserved for future reset-on-DB-wipe path.
    private fun ThreatFeedResponseJson.isEmpty(): Boolean = items.isEmpty()

    companion object {
        private const val TAG = "ThreatFeedRepository"
        private const val SHA256_HEX_LEN = 64
        private const val SHA512_HEX_LEN = 128
        private const val TLSH_HEX_LEN = 70
        private const val MAX_BATCH_LIMIT = 1000
        private const val ABSOLUTE_BATCH_CAP = 20
    }
}
