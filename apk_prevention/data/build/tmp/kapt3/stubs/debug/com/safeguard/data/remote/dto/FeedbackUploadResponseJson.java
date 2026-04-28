package com.safeguard.data.remote.dto;

import com.squareup.moshi.Json;

/**
 * Server response to a feedback batch upload. `accepted_ids` lets the client delete only
 * the rows the server actually persisted — partial acceptance can happen if a single event
 * fails validation but the rest are valid (Pydantic returns 422 with details), or if the
 * server rate-limits / dedupes against a previously-seen UUID. If the field is absent,
 * the worker falls back to "all-or-nothing" — treat the whole batch as accepted only on a
 * 2xx with a non-error body.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B#\u0012\u0010\b\u0001\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0007J\u0011\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003\u00a2\u0006\u0002\u0010\u000bJ,\u0010\u000f\u001a\u00020\u00002\u0010\b\u0003\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00c6\u0001\u00a2\u0006\u0002\u0010\u0010J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0006H\u00d6\u0001J\t\u0010\u0015\u001a\u00020\u0004H\u00d6\u0001R\u0019\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0015\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\n\n\u0002\u0010\f\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0016"}, d2 = {"Lcom/safeguard/data/remote/dto/FeedbackUploadResponseJson;", "", "acceptedIds", "", "", "rejectedCount", "", "(Ljava/util/List;Ljava/lang/Integer;)V", "getAcceptedIds", "()Ljava/util/List;", "getRejectedCount", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "component1", "component2", "copy", "(Ljava/util/List;Ljava/lang/Integer;)Lcom/safeguard/data/remote/dto/FeedbackUploadResponseJson;", "equals", "", "other", "hashCode", "toString", "data_debug"})
public final class FeedbackUploadResponseJson {
    @org.jetbrains.annotations.Nullable
    private final java.util.List<java.lang.String> acceptedIds = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer rejectedCount = null;
    
    public FeedbackUploadResponseJson(@com.squareup.moshi.Json(name = "accepted_ids")
    @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> acceptedIds, @com.squareup.moshi.Json(name = "rejected_count")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer rejectedCount) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.List<java.lang.String> getAcceptedIds() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getRejectedCount() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.List<java.lang.String> component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.dto.FeedbackUploadResponseJson copy(@com.squareup.moshi.Json(name = "accepted_ids")
    @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> acceptedIds, @com.squareup.moshi.Json(name = "rejected_count")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer rejectedCount) {
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