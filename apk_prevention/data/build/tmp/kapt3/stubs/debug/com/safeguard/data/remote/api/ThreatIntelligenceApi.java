package com.safeguard.data.remote.api;

import com.safeguard.data.remote.dto.FeedbackUploadRequest;
import com.safeguard.data.remote.dto.FeedbackUploadResponseJson;
import com.safeguard.data.remote.dto.VerificationRequest;
import com.safeguard.data.remote.dto.VerificationResponse;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J$\u0010\u0002\u001a\u00020\u00032\n\b\u0001\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u001e\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\b\b\u0001\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0018\u0010\u000f\u001a\u00020\u00102\b\b\u0001\u0010\f\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0012\u00a8\u0006\u0013"}, d2 = {"Lcom/safeguard/data/remote/api/ThreatIntelligenceApi;", "", "getThreatFeed", "Lokhttp3/ResponseBody;", "since", "", "limit", "", "(Ljava/lang/Long;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadFeedback", "Lretrofit2/Response;", "Lcom/safeguard/data/remote/dto/FeedbackUploadResponseJson;", "request", "Lcom/safeguard/data/remote/dto/FeedbackUploadRequest;", "(Lcom/safeguard/data/remote/dto/FeedbackUploadRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyAPK", "Lcom/safeguard/data/remote/dto/VerificationResponse;", "Lcom/safeguard/data/remote/dto/VerificationRequest;", "(Lcom/safeguard/data/remote/dto/VerificationRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public abstract interface ThreatIntelligenceApi {
    
    @retrofit2.http.POST(value = "v1/verify")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object verifyAPK(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.VerificationRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.safeguard.data.remote.dto.VerificationResponse> $completion);
    
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
    @retrofit2.http.POST(value = "v1/feedback")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object uploadFeedback(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.FeedbackUploadRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.safeguard.data.remote.dto.FeedbackUploadResponseJson>> $completion);
    
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
    @retrofit2.http.GET(value = "v1/threat-feed")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getThreatFeed(@retrofit2.http.Query(value = "since")
    @org.jetbrains.annotations.Nullable
    java.lang.Long since, @retrofit2.http.Query(value = "limit")
    int limit, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super okhttp3.ResponseBody> $completion);
}