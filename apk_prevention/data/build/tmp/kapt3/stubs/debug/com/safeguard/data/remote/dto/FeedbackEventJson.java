package com.safeguard.data.remote.dto;

import com.squareup.moshi.Json;

/**
 * One serialised [com.safeguard.core.domain.feedback.ScanFeedbackEvent]. Field names match
 * the server's Pydantic model so we can `extra="forbid"` validate without adapter shimming.
 *
 * Privacy invariants (must remain true forever):
 * - `sha256` is a hash, not the file. The server stores it only because malware corpora
 *   are keyed on hashes — there is no path back to the user's installed APK.
 * - `package_name` is opaque to a person; no installation path, no display label.
 * - No filename, no installer, no SD card UUID, no account info.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010$\n\u0000\n\u0002\u0010 \n\u0002\b\u001d\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001Bu\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0001\u0010\b\u001a\u00020\t\u0012\n\b\u0001\u0010\n\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\u0014\b\u0001\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\t0\u000e\u0012\u000e\b\u0001\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00030\u0010\u00a2\u0006\u0002\u0010\u0011J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\t\u0010#\u001a\u00020\u0005H\u00c6\u0003J\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\t\u0010&\u001a\u00020\tH\u00c6\u0003J\u000b\u0010\'\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u0010(\u001a\u0004\u0018\u00010\fH\u00c6\u0003\u00a2\u0006\u0002\u0010 J\u0015\u0010)\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\t0\u000eH\u00c6\u0003J\u000f\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00030\u0010H\u00c6\u0003J~\u0010+\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\b\b\u0003\u0010\u0006\u001a\u00020\u00032\b\b\u0003\u0010\u0007\u001a\u00020\u00032\b\b\u0003\u0010\b\u001a\u00020\t2\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0014\b\u0003\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\t0\u000e2\u000e\b\u0003\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00030\u0010H\u00c6\u0001\u00a2\u0006\u0002\u0010,J\u0013\u0010-\u001a\u00020.2\b\u0010/\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00100\u001a\u00020\fH\u00d6\u0001J\t\u00101\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u001d\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\t0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0013\u0010\n\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0017R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0017R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00030\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0017R\u0015\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b\u001f\u0010 \u00a8\u00062"}, d2 = {"Lcom/safeguard/data/remote/dto/FeedbackEventJson;", "", "id", "", "createdAtMs", "", "sha256", "verdict", "confidence", "", "packageName", "versionCode", "", "layerScores", "", "triggeredRules", "", "(Ljava/lang/String;JLjava/lang/String;Ljava/lang/String;FLjava/lang/String;Ljava/lang/Integer;Ljava/util/Map;Ljava/util/List;)V", "getConfidence", "()F", "getCreatedAtMs", "()J", "getId", "()Ljava/lang/String;", "getLayerScores", "()Ljava/util/Map;", "getPackageName", "getSha256", "getTriggeredRules", "()Ljava/util/List;", "getVerdict", "getVersionCode", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;JLjava/lang/String;Ljava/lang/String;FLjava/lang/String;Ljava/lang/Integer;Ljava/util/Map;Ljava/util/List;)Lcom/safeguard/data/remote/dto/FeedbackEventJson;", "equals", "", "other", "hashCode", "toString", "data_debug"})
public final class FeedbackEventJson {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    private final long createdAtMs = 0L;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String sha256 = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String verdict = null;
    private final float confidence = 0.0F;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String packageName = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer versionCode = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.Map<java.lang.String, java.lang.Float> layerScores = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.lang.String> triggeredRules = null;
    
    public FeedbackEventJson(@com.squareup.moshi.Json(name = "id")
    @org.jetbrains.annotations.NotNull
    java.lang.String id, @com.squareup.moshi.Json(name = "created_at_ms")
    long createdAtMs, @com.squareup.moshi.Json(name = "sha256")
    @org.jetbrains.annotations.NotNull
    java.lang.String sha256, @com.squareup.moshi.Json(name = "verdict")
    @org.jetbrains.annotations.NotNull
    java.lang.String verdict, @com.squareup.moshi.Json(name = "confidence")
    float confidence, @com.squareup.moshi.Json(name = "package_name")
    @org.jetbrains.annotations.Nullable
    java.lang.String packageName, @com.squareup.moshi.Json(name = "version_code")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer versionCode, @com.squareup.moshi.Json(name = "layer_scores")
    @org.jetbrains.annotations.NotNull
    java.util.Map<java.lang.String, java.lang.Float> layerScores, @com.squareup.moshi.Json(name = "triggered_rules")
    @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> triggeredRules) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    public final long getCreatedAtMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getSha256() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getVerdict() {
        return null;
    }
    
    public final float getConfidence() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getPackageName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getVersionCode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.Map<java.lang.String, java.lang.Float> getLayerScores() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> getTriggeredRules() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    public final long component2() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component4() {
        return null;
    }
    
    public final float component5() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.Map<java.lang.String, java.lang.Float> component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.dto.FeedbackEventJson copy(@com.squareup.moshi.Json(name = "id")
    @org.jetbrains.annotations.NotNull
    java.lang.String id, @com.squareup.moshi.Json(name = "created_at_ms")
    long createdAtMs, @com.squareup.moshi.Json(name = "sha256")
    @org.jetbrains.annotations.NotNull
    java.lang.String sha256, @com.squareup.moshi.Json(name = "verdict")
    @org.jetbrains.annotations.NotNull
    java.lang.String verdict, @com.squareup.moshi.Json(name = "confidence")
    float confidence, @com.squareup.moshi.Json(name = "package_name")
    @org.jetbrains.annotations.Nullable
    java.lang.String packageName, @com.squareup.moshi.Json(name = "version_code")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer versionCode, @com.squareup.moshi.Json(name = "layer_scores")
    @org.jetbrains.annotations.NotNull
    java.util.Map<java.lang.String, java.lang.Float> layerScores, @com.squareup.moshi.Json(name = "triggered_rules")
    @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> triggeredRules) {
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