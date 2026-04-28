package com.safeguard.data.remote.dto;

import com.squareup.moshi.Json;

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
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B3\u0012\u000e\b\u0001\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\b\b\u0001\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0001\u0010\u0007\u001a\u00020\u0006\u0012\b\b\u0001\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\tH\u00c6\u0003J7\u0010\u0016\u001a\u00020\u00002\u000e\b\u0003\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0003\u0010\u0005\u001a\u00020\u00062\b\b\u0003\u0010\u0007\u001a\u00020\u00062\b\b\u0003\u0010\b\u001a\u00020\tH\u00c6\u0001J\u0013\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001a\u001a\u00020\u0006H\u00d6\u0001J\t\u0010\u001b\u001a\u00020\u001cH\u00d6\u0001R\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\fR\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006\u001d"}, d2 = {"Lcom/safeguard/data/remote/dto/FeedbackUploadRequest;", "", "events", "", "Lcom/safeguard/data/remote/dto/FeedbackEventJson;", "clientAppVersionCode", "", "clientAndroidApiLevel", "uploadedAtMs", "", "(Ljava/util/List;IIJ)V", "getClientAndroidApiLevel", "()I", "getClientAppVersionCode", "getEvents", "()Ljava/util/List;", "getUploadedAtMs", "()J", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "toString", "", "data_debug"})
public final class FeedbackUploadRequest {
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.safeguard.data.remote.dto.FeedbackEventJson> events = null;
    private final int clientAppVersionCode = 0;
    private final int clientAndroidApiLevel = 0;
    private final long uploadedAtMs = 0L;
    
    public FeedbackUploadRequest(@com.squareup.moshi.Json(name = "events")
    @org.jetbrains.annotations.NotNull
    java.util.List<com.safeguard.data.remote.dto.FeedbackEventJson> events, @com.squareup.moshi.Json(name = "client_app_version_code")
    int clientAppVersionCode, @com.squareup.moshi.Json(name = "client_android_api_level")
    int clientAndroidApiLevel, @com.squareup.moshi.Json(name = "uploaded_at_ms")
    long uploadedAtMs) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.safeguard.data.remote.dto.FeedbackEventJson> getEvents() {
        return null;
    }
    
    public final int getClientAppVersionCode() {
        return 0;
    }
    
    public final int getClientAndroidApiLevel() {
        return 0;
    }
    
    public final long getUploadedAtMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.safeguard.data.remote.dto.FeedbackEventJson> component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int component3() {
        return 0;
    }
    
    public final long component4() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.dto.FeedbackUploadRequest copy(@com.squareup.moshi.Json(name = "events")
    @org.jetbrains.annotations.NotNull
    java.util.List<com.safeguard.data.remote.dto.FeedbackEventJson> events, @com.squareup.moshi.Json(name = "client_app_version_code")
    int clientAppVersionCode, @com.squareup.moshi.Json(name = "client_android_api_level")
    int clientAndroidApiLevel, @com.squareup.moshi.Json(name = "uploaded_at_ms")
    long uploadedAtMs) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}