package com.safeguard.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persisted form of [com.safeguard.core.domain.feedback.ScanFeedbackEvent] (Phase 3.2).
 * Stored in the encrypted SQLCipher DB so the queue is not readable by other on-device
 * processes even with root.
 *
 * Schema notes:
 *  - [layerScoresJson] and [triggeredRulesJson] hold serialised JSON (Moshi). Storing them
 *    inline avoids a child table for the rare event payloads we'll actually see (≤ 7 layer
 *    scores per row, typically 0–3 rule names) and keeps the queue self-contained for the
 *    "purge everything" privacy guarantee — `DELETE FROM scan_feedback_queue` reaches all
 *    fields without needing CASCADE plumbing.
 *  - [createdAtMs] is indexed because the upload worker drains in FIFO order (oldest
 *    first) so transient outages don't starve the head of the queue.
 *  - The primary key is [id] (a UUID) rather than `sha256`. Re-scanning the same APK on
 *    the same device should produce a *new* row; the server collapses by `sha256` after
 *    aggregation. If we keyed on `sha256` we'd lose the time-of-day signal that's useful
 *    for diagnosing "this APK was clean last week, now it's flagged" regressions.
 */
@Entity(
    tableName = "scan_feedback_queue",
    indices = [Index(value = ["createdAtMs"])]
)
data class ScanFeedbackEventEntity(
    @PrimaryKey val id: String,
    val createdAtMs: Long,
    val sha256: String,
    val verdict: String,
    val confidence: Float,
    val packageName: String?,
    val versionCode: Int?,
    val layerScoresJson: String,
    val triggeredRulesJson: String,
    val androidApiLevel: Int,
    val appVersionCode: Int
)
