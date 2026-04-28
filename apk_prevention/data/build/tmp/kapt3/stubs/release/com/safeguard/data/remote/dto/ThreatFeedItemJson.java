package com.safeguard.data.remote.dto;

import com.squareup.moshi.Json;

/**
 * On-the-wire representation of one MalwareBazaar APK sample as returned by the server's
 * `GET /v1/threat-feed`. Mirrors `ThreatFeedItem` in `server/main.py`.
 *
 * - [sha256] is required and lowercase hex (64 chars).
 * - [sha512] is optional — abuse.ch sometimes omits it for older submissions.
 * - [fuzzyHash] is the **70-char upper-case hex TLSH** the on-device `FuzzyHasher` expects.
 *  The server already strips any leading `T1` prefix and validates length, so the device
 *  can pass it straight through to Room without re-validation.
 * - [severity] is the bucketed score (0..100). The decision engine treats ≥ 90 as MALICIOUS
 *  on a SHA-256 hit; lower buckets only contribute via TLSH similarity.
 * - [firstSeenMs] is the source timestamp in epoch ms. Used purely as the cursor field on
 *  the next sync — the device does not currently store it for ranking.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0019\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B]\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0001\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0001\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0003\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0001\u0010\b\u001a\u00020\t\u0012\n\b\u0001\u0010\n\u001a\u0004\u0018\u00010\u000b\u0012\n\b\u0001\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\rJ\t\u0010\u001a\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u001b\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\tH\u00c6\u0003J\u0010\u0010 \u001a\u0004\u0018\u00010\u000bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u000fJ\u000b\u0010!\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003Jf\u0010\"\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00032\b\b\u0003\u0010\u0005\u001a\u00020\u00032\b\b\u0003\u0010\u0006\u001a\u00020\u00032\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00032\b\b\u0003\u0010\b\u001a\u00020\t2\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u000b2\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001\u00a2\u0006\u0002\u0010#J\u0013\u0010$\u001a\u00020%2\b\u0010&\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\'\u001a\u00020\tH\u00d6\u0001J\t\u0010(\u001a\u00020\u0003H\u00d6\u0001R\u0015\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\n\n\u0002\u0010\u0010\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0012R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0012R\u0013\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0012R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0012R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0012\u00a8\u0006)"}, d2 = {"Lcom/safeguard/data/remote/dto/ThreatFeedItemJson;", "", "sha256", "", "sha512", "fuzzyHash", "threatName", "threatFamily", "severity", "", "firstSeenMs", "", "source", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Long;Ljava/lang/String;)V", "getFirstSeenMs", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getFuzzyHash", "()Ljava/lang/String;", "getSeverity", "()I", "getSha256", "getSha512", "getSource", "getThreatFamily", "getThreatName", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Long;Ljava/lang/String;)Lcom/safeguard/data/remote/dto/ThreatFeedItemJson;", "equals", "", "other", "hashCode", "toString", "data_release"})
public final class ThreatFeedItemJson {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String sha256 = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String sha512 = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String fuzzyHash = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String threatName = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String threatFamily = null;
    private final int severity = 0;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Long firstSeenMs = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String source = null;
    
    public ThreatFeedItemJson(@com.squareup.moshi.Json(name = "sha256")
    @org.jetbrains.annotations.NotNull
    java.lang.String sha256, @com.squareup.moshi.Json(name = "sha512")
    @org.jetbrains.annotations.Nullable
    java.lang.String sha512, @com.squareup.moshi.Json(name = "fuzzy_hash")
    @org.jetbrains.annotations.NotNull
    java.lang.String fuzzyHash, @com.squareup.moshi.Json(name = "threat_name")
    @org.jetbrains.annotations.NotNull
    java.lang.String threatName, @com.squareup.moshi.Json(name = "threat_family")
    @org.jetbrains.annotations.Nullable
    java.lang.String threatFamily, @com.squareup.moshi.Json(name = "severity")
    int severity, @com.squareup.moshi.Json(name = "first_seen_ms")
    @org.jetbrains.annotations.Nullable
    java.lang.Long firstSeenMs, @com.squareup.moshi.Json(name = "source")
    @org.jetbrains.annotations.Nullable
    java.lang.String source) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getSha256() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getSha512() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFuzzyHash() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getThreatName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getThreatFamily() {
        return null;
    }
    
    public final int getSeverity() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Long getFirstSeenMs() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getSource() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component5() {
        return null;
    }
    
    public final int component6() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Long component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.dto.ThreatFeedItemJson copy(@com.squareup.moshi.Json(name = "sha256")
    @org.jetbrains.annotations.NotNull
    java.lang.String sha256, @com.squareup.moshi.Json(name = "sha512")
    @org.jetbrains.annotations.Nullable
    java.lang.String sha512, @com.squareup.moshi.Json(name = "fuzzy_hash")
    @org.jetbrains.annotations.NotNull
    java.lang.String fuzzyHash, @com.squareup.moshi.Json(name = "threat_name")
    @org.jetbrains.annotations.NotNull
    java.lang.String threatName, @com.squareup.moshi.Json(name = "threat_family")
    @org.jetbrains.annotations.Nullable
    java.lang.String threatFamily, @com.squareup.moshi.Json(name = "severity")
    int severity, @com.squareup.moshi.Json(name = "first_seen_ms")
    @org.jetbrains.annotations.Nullable
    java.lang.Long firstSeenMs, @com.squareup.moshi.Json(name = "source")
    @org.jetbrains.annotations.Nullable
    java.lang.String source) {
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