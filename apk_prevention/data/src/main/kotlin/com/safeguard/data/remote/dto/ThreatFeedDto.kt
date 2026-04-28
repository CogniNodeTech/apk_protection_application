package com.safeguard.data.remote.dto

import com.squareup.moshi.Json

/**
 * On-the-wire representation of one MalwareBazaar APK sample as returned by the server's
 * `GET /v1/threat-feed`. Mirrors `ThreatFeedItem` in `server/main.py`.
 *
 * - [sha256] is required and lowercase hex (64 chars).
 * - [sha512] is optional — abuse.ch sometimes omits it for older submissions.
 * - [fuzzyHash] is the **70-char upper-case hex TLSH** the on-device `FuzzyHasher` expects.
 *   The server already strips any leading `T1` prefix and validates length, so the device
 *   can pass it straight through to Room without re-validation.
 * - [severity] is the bucketed score (0..100). The decision engine treats ≥ 90 as MALICIOUS
 *   on a SHA-256 hit; lower buckets only contribute via TLSH similarity.
 * - [firstSeenMs] is the source timestamp in epoch ms. Used purely as the cursor field on
 *   the next sync — the device does not currently store it for ranking.
 */
data class ThreatFeedItemJson(
    @Json(name = "sha256") val sha256: String,
    @Json(name = "sha512") val sha512: String?,
    @Json(name = "fuzzy_hash") val fuzzyHash: String,
    @Json(name = "threat_name") val threatName: String,
    @Json(name = "threat_family") val threatFamily: String?,
    @Json(name = "severity") val severity: Int,
    @Json(name = "first_seen_ms") val firstSeenMs: Long?,
    @Json(name = "source") val source: String?
)

/**
 * Response envelope. [nextCursorMs] is monotonically advancing — even an empty feed bumps it,
 * so a quiet hour on abuse.ch never causes the client to re-walk the same window forever.
 *
 * [hasMore] is the server's hint that more rows are available beyond [nextCursorMs] (i.e.,
 * the feed was capped at the requested `limit`); the client uses it to drive immediate
 * follow-up sync requests instead of waiting for the next periodic window.
 */
data class ThreatFeedResponseJson(
    @Json(name = "items") val items: List<ThreatFeedItemJson>,
    @Json(name = "next_cursor_ms") val nextCursorMs: Long,
    @Json(name = "has_more") val hasMore: Boolean
)
