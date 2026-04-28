package com.safeguard.data.remote.dto;

import com.squareup.moshi.Json;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b \n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B{\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0007\u0012\u000e\b\u0001\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00030\t\u0012\b\b\u0001\u0010\n\u001a\u00020\u000b\u0012\b\b\u0001\u0010\f\u001a\u00020\u0007\u0012\n\b\u0001\u0010\r\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0001\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0001\u0010\u0010\u001a\u00020\u0011\u0012\b\b\u0001\u0010\u0012\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\u0013J\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\t\u0010&\u001a\u00020\u0011H\u00c6\u0003J\t\u0010\'\u001a\u00020\u000bH\u00c6\u0003J\t\u0010(\u001a\u00020\u0003H\u00c6\u0003J\t\u0010)\u001a\u00020\u0003H\u00c6\u0003J\t\u0010*\u001a\u00020\u0007H\u00c6\u0003J\u000f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00030\tH\u00c6\u0003J\t\u0010,\u001a\u00020\u000bH\u00c6\u0003J\t\u0010-\u001a\u00020\u0007H\u00c6\u0003J\u000b\u0010.\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010/\u001a\u00020\u000fH\u00c6\u0003J\u007f\u00100\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00032\b\b\u0003\u0010\u0005\u001a\u00020\u00032\b\b\u0003\u0010\u0006\u001a\u00020\u00072\u000e\b\u0003\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00030\t2\b\b\u0003\u0010\n\u001a\u00020\u000b2\b\b\u0003\u0010\f\u001a\u00020\u00072\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\u00032\b\b\u0003\u0010\u000e\u001a\u00020\u000f2\b\b\u0003\u0010\u0010\u001a\u00020\u00112\b\b\u0003\u0010\u0012\u001a\u00020\u000bH\u00c6\u0001J\u0013\u00101\u001a\u0002022\b\u00103\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00104\u001a\u00020\u0007H\u00d6\u0001J\t\u00105\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0015R\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0015R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00030\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0013\u0010\r\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0015R\u0011\u0010\f\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0011\u0010\u0012\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001aR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\"\u00a8\u00066"}, d2 = {"Lcom/safeguard/data/remote/dto/VerificationRequest;", "", "apkHashSha256", "", "apkHashSha512", "packageName", "versionCode", "", "permissions", "", "fileSize", "", "targetSdk", "signatureFingerprint", "localLayerScores", "Lcom/safeguard/data/remote/dto/LocalLayerScoresJson;", "deviceMetadata", "Lcom/safeguard/data/remote/dto/DeviceMetadataJson;", "timestamp", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/util/List;JILjava/lang/String;Lcom/safeguard/data/remote/dto/LocalLayerScoresJson;Lcom/safeguard/data/remote/dto/DeviceMetadataJson;J)V", "getApkHashSha256", "()Ljava/lang/String;", "getApkHashSha512", "getDeviceMetadata", "()Lcom/safeguard/data/remote/dto/DeviceMetadataJson;", "getFileSize", "()J", "getLocalLayerScores", "()Lcom/safeguard/data/remote/dto/LocalLayerScoresJson;", "getPackageName", "getPermissions", "()Ljava/util/List;", "getSignatureFingerprint", "getTargetSdk", "()I", "getTimestamp", "getVersionCode", "component1", "component10", "component11", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "toString", "data_debug"})
public final class VerificationRequest {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String apkHashSha256 = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String apkHashSha512 = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String packageName = null;
    private final int versionCode = 0;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.lang.String> permissions = null;
    private final long fileSize = 0L;
    private final int targetSdk = 0;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String signatureFingerprint = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.remote.dto.LocalLayerScoresJson localLayerScores = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.remote.dto.DeviceMetadataJson deviceMetadata = null;
    private final long timestamp = 0L;
    
    public VerificationRequest(@com.squareup.moshi.Json(name = "apk_hash_sha256")
    @org.jetbrains.annotations.NotNull
    java.lang.String apkHashSha256, @com.squareup.moshi.Json(name = "apk_hash_sha512")
    @org.jetbrains.annotations.NotNull
    java.lang.String apkHashSha512, @com.squareup.moshi.Json(name = "package_name")
    @org.jetbrains.annotations.NotNull
    java.lang.String packageName, @com.squareup.moshi.Json(name = "version_code")
    int versionCode, @com.squareup.moshi.Json(name = "permissions")
    @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> permissions, @com.squareup.moshi.Json(name = "file_size")
    long fileSize, @com.squareup.moshi.Json(name = "target_sdk")
    int targetSdk, @com.squareup.moshi.Json(name = "signature_fingerprint")
    @org.jetbrains.annotations.Nullable
    java.lang.String signatureFingerprint, @com.squareup.moshi.Json(name = "local_layer_scores")
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.LocalLayerScoresJson localLayerScores, @com.squareup.moshi.Json(name = "device_metadata")
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.DeviceMetadataJson deviceMetadata, @com.squareup.moshi.Json(name = "timestamp")
    long timestamp) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getApkHashSha256() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getApkHashSha512() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getPackageName() {
        return null;
    }
    
    public final int getVersionCode() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> getPermissions() {
        return null;
    }
    
    public final long getFileSize() {
        return 0L;
    }
    
    public final int getTargetSdk() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getSignatureFingerprint() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.dto.LocalLayerScoresJson getLocalLayerScores() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.dto.DeviceMetadataJson getDeviceMetadata() {
        return null;
    }
    
    public final long getTimestamp() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.dto.DeviceMetadataJson component10() {
        return null;
    }
    
    public final long component11() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    public final int component4() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> component5() {
        return null;
    }
    
    public final long component6() {
        return 0L;
    }
    
    public final int component7() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.dto.LocalLayerScoresJson component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.dto.VerificationRequest copy(@com.squareup.moshi.Json(name = "apk_hash_sha256")
    @org.jetbrains.annotations.NotNull
    java.lang.String apkHashSha256, @com.squareup.moshi.Json(name = "apk_hash_sha512")
    @org.jetbrains.annotations.NotNull
    java.lang.String apkHashSha512, @com.squareup.moshi.Json(name = "package_name")
    @org.jetbrains.annotations.NotNull
    java.lang.String packageName, @com.squareup.moshi.Json(name = "version_code")
    int versionCode, @com.squareup.moshi.Json(name = "permissions")
    @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> permissions, @com.squareup.moshi.Json(name = "file_size")
    long fileSize, @com.squareup.moshi.Json(name = "target_sdk")
    int targetSdk, @com.squareup.moshi.Json(name = "signature_fingerprint")
    @org.jetbrains.annotations.Nullable
    java.lang.String signatureFingerprint, @com.squareup.moshi.Json(name = "local_layer_scores")
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.LocalLayerScoresJson localLayerScores, @com.squareup.moshi.Json(name = "device_metadata")
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.DeviceMetadataJson deviceMetadata, @com.squareup.moshi.Json(name = "timestamp")
    long timestamp) {
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