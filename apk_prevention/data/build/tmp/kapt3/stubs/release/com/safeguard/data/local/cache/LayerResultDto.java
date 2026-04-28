package com.safeguard.data.local.cache;

import com.squareup.moshi.Json;

/**
 * DTO for serializing LayerResult to JSON in DB.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\t\n\u0002\b\"\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001Bu\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0007\u001a\u00020\b\u0012\b\b\u0001\u0010\t\u001a\u00020\u0003\u0012\u000e\b\u0001\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b\u0012\b\b\u0001\u0010\f\u001a\u00020\r\u0012\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u0011J\t\u0010#\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010$\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010 J\t\u0010%\u001a\u00020\u0005H\u00c6\u0003J\t\u0010&\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\'\u001a\u00020\bH\u00c6\u0003J\t\u0010(\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00050\u000bH\u00c6\u0003J\t\u0010*\u001a\u00020\rH\u00c6\u0003J\u000b\u0010+\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010,\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J~\u0010-\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\b\b\u0003\u0010\u0006\u001a\u00020\u00052\b\b\u0003\u0010\u0007\u001a\u00020\b2\b\b\u0003\u0010\t\u001a\u00020\u00032\u000e\b\u0003\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\b\b\u0003\u0010\f\u001a\u00020\r2\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001\u00a2\u0006\u0002\u0010.J\u0013\u0010/\u001a\u0002002\b\u00101\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00102\u001a\u00020\u0003H\u00d6\u0001J\t\u00103\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0019R\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001bR\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001bR\u0015\u0010\u0010\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b\u001f\u0010 R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001b\u00a8\u00064"}, d2 = {"Lcom/safeguard/data/local/cache/LayerResultDto;", "", "layerId", "", "layerName", "", "verdict", "confidence", "", "riskScore", "evidence", "", "executionTimeMs", "", "threatName", "threatFamily", "threatRiskScore", "(ILjava/lang/String;Ljava/lang/String;FILjava/util/List;JLjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;)V", "getConfidence", "()F", "getEvidence", "()Ljava/util/List;", "getExecutionTimeMs", "()J", "getLayerId", "()I", "getLayerName", "()Ljava/lang/String;", "getRiskScore", "getThreatFamily", "getThreatName", "getThreatRiskScore", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getVerdict", "component1", "component10", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(ILjava/lang/String;Ljava/lang/String;FILjava/util/List;JLjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;)Lcom/safeguard/data/local/cache/LayerResultDto;", "equals", "", "other", "hashCode", "toString", "data_release"})
public final class LayerResultDto {
    private final int layerId = 0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String layerName = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String verdict = null;
    private final float confidence = 0.0F;
    private final int riskScore = 0;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.lang.String> evidence = null;
    private final long executionTimeMs = 0L;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String threatName = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String threatFamily = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer threatRiskScore = null;
    
    public LayerResultDto(@com.squareup.moshi.Json(name = "layerId")
    int layerId, @com.squareup.moshi.Json(name = "layerName")
    @org.jetbrains.annotations.NotNull
    java.lang.String layerName, @com.squareup.moshi.Json(name = "verdict")
    @org.jetbrains.annotations.NotNull
    java.lang.String verdict, @com.squareup.moshi.Json(name = "confidence")
    float confidence, @com.squareup.moshi.Json(name = "riskScore")
    int riskScore, @com.squareup.moshi.Json(name = "evidence")
    @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> evidence, @com.squareup.moshi.Json(name = "executionTimeMs")
    long executionTimeMs, @com.squareup.moshi.Json(name = "threatName")
    @org.jetbrains.annotations.Nullable
    java.lang.String threatName, @com.squareup.moshi.Json(name = "threatFamily")
    @org.jetbrains.annotations.Nullable
    java.lang.String threatFamily, @com.squareup.moshi.Json(name = "threatRiskScore")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer threatRiskScore) {
        super();
    }
    
    public final int getLayerId() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getLayerName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getVerdict() {
        return null;
    }
    
    public final float getConfidence() {
        return 0.0F;
    }
    
    public final int getRiskScore() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> getEvidence() {
        return null;
    }
    
    public final long getExecutionTimeMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getThreatName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getThreatFamily() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getThreatRiskScore() {
        return null;
    }
    
    public final int component1() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    public final float component4() {
        return 0.0F;
    }
    
    public final int component5() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> component6() {
        return null;
    }
    
    public final long component7() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.cache.LayerResultDto copy(@com.squareup.moshi.Json(name = "layerId")
    int layerId, @com.squareup.moshi.Json(name = "layerName")
    @org.jetbrains.annotations.NotNull
    java.lang.String layerName, @com.squareup.moshi.Json(name = "verdict")
    @org.jetbrains.annotations.NotNull
    java.lang.String verdict, @com.squareup.moshi.Json(name = "confidence")
    float confidence, @com.squareup.moshi.Json(name = "riskScore")
    int riskScore, @com.squareup.moshi.Json(name = "evidence")
    @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> evidence, @com.squareup.moshi.Json(name = "executionTimeMs")
    long executionTimeMs, @com.squareup.moshi.Json(name = "threatName")
    @org.jetbrains.annotations.Nullable
    java.lang.String threatName, @com.squareup.moshi.Json(name = "threatFamily")
    @org.jetbrains.annotations.Nullable
    java.lang.String threatFamily, @com.squareup.moshi.Json(name = "threatRiskScore")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer threatRiskScore) {
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