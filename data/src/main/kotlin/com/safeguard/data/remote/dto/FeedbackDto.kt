package com.safeguard.data.remote.dto

import com.squareup.moshi.Json

/**
 * Wire payload for the Phase 3.2 feedback endpoint (`POST /v1/feedback`).
 *
 * The on-device contract for *what fields are present* is intentionally narrower than the
 * server's eventual schema. We will only ever send what the local
 * [com.safeguard.core.domain.feedback.ScanFeedbackEvent] already minimised; the server
 * additionally rejects rows containing any extra field (Pydantic `extra="forbid"`) so a
 * future client that accidentally adds a path or filename can't sneak it past review.
 *
 * The `client_*` fields are populated once at the worker level so a single batch upload
 * carries the build/OS context just once instead of repeating it in every event row.
 */
data class FeedbackUploadRequest(
    @Json(name = "events") val events: List<FeedbackEventJson>,
    @Json(name = "client_app_version_code") val clientAppVersionCode: Int,
    @Json(name = "client_android_api_level") val clientAndroidApiLevel: Int,
    @Json(name = "uploaded_at_ms") val uploadedAtMs: Long
)

/**
 * One serialised [com.safeguard.core.domain.feedback.ScanFeedbackEvent]. Field names match
 * the server's Pydantic model so we can `extra="forbid"` validate without adapter shimming.
 *
 * Privacy invariants (must remain true forever):
 *  - `sha256` is a hash, not the file. The server stores it only because malware corpora
 *    are keyed on hashes — there is no path back to the user's installed APK.
 *  - `package_name` is opaque to a person; no installation path, no display label.
 *  - No filename, no installer, no SD card UUID, no account info.
 */
data class FeedbackEventJson(
    @Json(name = "id") val id: String,
    @Json(name = "created_at_ms") val createdAtMs: Long,
    @Json(name = "sha256") val sha256: String,
    @Json(name = "verdict") val verdict: String,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "package_name") val packageName: String?,
    @Json(name = "version_code") val versionCode: Int?,
    @Json(name = "layer_scores") val layerScores: Map<String, Float>,
    @Json(name = "triggered_rules") val triggeredRules: List<String>
)

/**
 * Server response to a feedback batch upload. `accepted_ids` lets the client delete only
 * the rows the server actually persisted — partial acceptance can happen if a single event
 * fails validation but the rest are valid (Pydantic returns 422 with details), or if the
 * server rate-limits / dedupes against a previously-seen UUID. If the field is absent,
 * the worker falls back to "all-or-nothing" — treat the whole batch as accepted only on a
 * 2xx with a non-error body.
 */
data class FeedbackUploadResponseJson(
    @Json(name = "accepted_ids") val acceptedIds: List<String>?,
    @Json(name = "rejected_count") val rejectedCount: Int?
)
