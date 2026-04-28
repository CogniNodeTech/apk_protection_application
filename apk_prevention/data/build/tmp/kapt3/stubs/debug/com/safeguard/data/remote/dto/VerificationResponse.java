package com.safeguard.data.remote.dto;

import com.squareup.moshi.Json;

/**
 * Layer 6 cloud response. [virustotalLink] is the JSON field name for historical reasons;
 * the server may set it to a MalwareBazaar sample URL or another intel permalink — not necessarily VirusTotal.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u001e\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B\u007f\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\b\u001a\u0004\u0018\u00010\t\u0012\n\b\u0001\u0010\n\u001a\u0004\u0018\u00010\t\u0012\n\b\u0001\u0010\u000b\u001a\u0004\u0018\u00010\t\u0012\n\b\u0001\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0001\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000e\u0012\n\b\u0001\u0010\u000f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u0010J\t\u0010 \u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010!\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010#\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010$\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u0010%\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0012J\u0010\u0010&\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0012J\u0010\u0010\'\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0012J\u000b\u0010(\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u0010)\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000eH\u00c6\u0003J\u0088\u0001\u0010*\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\t2\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\t2\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\t2\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\u00032\u0010\b\u0003\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000e2\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001\u00a2\u0006\u0002\u0010+J\u0013\u0010,\u001a\u00020-2\b\u0010.\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010/\u001a\u00020\tH\u00d6\u0001J\t\u00100\u001a\u00020\u0003H\u00d6\u0001R\u0015\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u0011\u0010\u0012R\u0015\u0010\u000b\u001a\u0004\u0018\u00010\t\u00a2\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u0014\u0010\u0012R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0019\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001aR\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001aR\u0015\u0010\n\u001a\u0004\u0018\u00010\t\u00a2\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u001d\u0010\u0012R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001aR\u0013\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001a\u00a8\u00061"}, d2 = {"Lcom/safeguard/data/remote/dto/VerificationResponse;", "", "verdict", "", "confidence", "", "threatName", "threatFamily", "avDetections", "", "totalAvScanned", "communityReports", "virustotalLink", "evidence", "", "recommendation", "(Ljava/lang/String;FLjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;)V", "getAvDetections", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getCommunityReports", "getConfidence", "()F", "getEvidence", "()Ljava/util/List;", "getRecommendation", "()Ljava/lang/String;", "getThreatFamily", "getThreatName", "getTotalAvScanned", "getVerdict", "getVirustotalLink", "component1", "component10", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;FLjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;)Lcom/safeguard/data/remote/dto/VerificationResponse;", "equals", "", "other", "hashCode", "toString", "data_debug"})
public final class VerificationResponse {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String verdict = null;
    private final float confidence = 0.0F;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String threatName = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String threatFamily = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer avDetections = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer totalAvScanned = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer communityReports = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String virustotalLink = null;
    @org.jetbrains.annotations.Nullable
    private final java.util.List<java.lang.String> evidence = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String recommendation = null;
    
    public VerificationResponse(@com.squareup.moshi.Json(name = "verdict")
    @org.jetbrains.annotations.NotNull
    java.lang.String verdict, @com.squareup.moshi.Json(name = "confidence")
    float confidence, @com.squareup.moshi.Json(name = "threat_name")
    @org.jetbrains.annotations.Nullable
    java.lang.String threatName, @com.squareup.moshi.Json(name = "threat_family")
    @org.jetbrains.annotations.Nullable
    java.lang.String threatFamily, @com.squareup.moshi.Json(name = "av_detections")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer avDetections, @com.squareup.moshi.Json(name = "total_av_scanned")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer totalAvScanned, @com.squareup.moshi.Json(name = "community_reports")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer communityReports, @com.squareup.moshi.Json(name = "virustotal_link")
    @org.jetbrains.annotations.Nullable
    java.lang.String virustotalLink, @com.squareup.moshi.Json(name = "evidence")
    @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> evidence, @com.squareup.moshi.Json(name = "recommendation")
    @org.jetbrains.annotations.Nullable
    java.lang.String recommendation) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getVerdict() {
        return null;
    }
    
    public final float getConfidence() {
        return 0.0F;
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
    public final java.lang.Integer getAvDetections() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getTotalAvScanned() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getCommunityReports() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getVirustotalLink() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.List<java.lang.String> getEvidence() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getRecommendation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component10() {
        return null;
    }
    
    public final float component2() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.List<java.lang.String> component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.dto.VerificationResponse copy(@com.squareup.moshi.Json(name = "verdict")
    @org.jetbrains.annotations.NotNull
    java.lang.String verdict, @com.squareup.moshi.Json(name = "confidence")
    float confidence, @com.squareup.moshi.Json(name = "threat_name")
    @org.jetbrains.annotations.Nullable
    java.lang.String threatName, @com.squareup.moshi.Json(name = "threat_family")
    @org.jetbrains.annotations.Nullable
    java.lang.String threatFamily, @com.squareup.moshi.Json(name = "av_detections")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer avDetections, @com.squareup.moshi.Json(name = "total_av_scanned")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer totalAvScanned, @com.squareup.moshi.Json(name = "community_reports")
    @org.jetbrains.annotations.Nullable
    java.lang.Integer communityReports, @com.squareup.moshi.Json(name = "virustotal_link")
    @org.jetbrains.annotations.Nullable
    java.lang.String virustotalLink, @com.squareup.moshi.Json(name = "evidence")
    @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> evidence, @com.squareup.moshi.Json(name = "recommendation")
    @org.jetbrains.annotations.Nullable
    java.lang.String recommendation) {
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