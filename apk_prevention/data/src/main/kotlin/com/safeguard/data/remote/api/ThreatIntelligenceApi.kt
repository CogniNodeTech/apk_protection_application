package com.safeguard.data.remote.api

import com.safeguard.data.remote.dto.FeedbackUploadRequest
import com.safeguard.data.remote.dto.FeedbackUploadResponseJson
import com.safeguard.data.remote.dto.VerificationRequest
import com.safeguard.data.remote.dto.VerificationResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ThreatIntelligenceApi {
    @POST("v1/verify")
    suspend fun verifyAPK(@Body request: VerificationRequest): VerificationResponse

    /**
     * Phase 3.2 — privacy-preserving scan feedback. Send a batch of opt-in events whose
     * payload is strictly limited to hashes + structured scan metadata (no APK bytes, no
     * paths, no PII).
     *
     * Returns the raw [Response] envelope so [com.safeguard.data.repository.ScanFeedbackRepositoryImpl]
     * can distinguish 2xx success (body present, parse `accepted_ids`) from 4xx validation
     * failure (don't retry — the events are malformed) from 5xx / I/O failure (retry with
     * backoff).
     */
    @POST("v1/feedback")
    suspend fun uploadFeedback(@Body request: FeedbackUploadRequest): Response<FeedbackUploadResponseJson>

    /**
     * Bulk MalwareBazaar APK feed for on-device Layer 2 sync. The client persists
     * `next_cursor_ms` from the previous response and passes it back as [since] so each
     * call only ships rows whose `first_seen` is strictly newer than the cursor.
     *
     * Returns the raw response body (instead of a parsed `ThreatFeedResponseJson`) because
     * Phase 3.1 wraps the payload in an Ed25519 signed envelope. The repository must hold
     * onto the exact bytes the server signed in order to verify them — re-serializing a
     * Moshi-parsed object back to JSON would change byte-for-byte ordering and break
     * signature verification, even with sorted keys (different number serialization,
     * whitespace handling, etc.). So the API returns bytes, the verifier checks them, and
     * only the inner payload is then parsed with Moshi.
     */
    @GET("v1/threat-feed")
    suspend fun getThreatFeed(
        @Query("since") since: Long?,
        @Query("limit") limit: Int
    ): ResponseBody
}
