package com.safeguard.data.repository;

import com.safeguard.core.domain.repository.CloudVerificationRepository;
import com.safeguard.core.domain.repository.CloudVerificationResponse;
import com.safeguard.data.remote.api.ThreatIntelligenceApi;
import com.safeguard.core.domain.repository.DeviceCloudMetadata;
import com.safeguard.core.domain.repository.LocalLayerScores;
import com.safeguard.data.remote.dto.DeviceMetadataJson;
import com.safeguard.data.remote.dto.LocalLayerScoresJson;
import com.safeguard.data.remote.dto.VerificationRequest;
import retrofit2.HttpException;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004Jf\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\f0\u00122\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00102\b\u0010\u0016\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aH\u0096@\u00a2\u0006\u0002\u0010\u001bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/safeguard/data/repository/CloudVerificationRepositoryImpl;", "Lcom/safeguard/core/domain/repository/CloudVerificationRepository;", "api", "Lcom/safeguard/data/remote/api/ThreatIntelligenceApi;", "(Lcom/safeguard/data/remote/api/ThreatIntelligenceApi;)V", "circuitOpenUntil", "Ljava/util/concurrent/atomic/AtomicLong;", "consecutiveFailures", "Ljava/util/concurrent/atomic/AtomicInteger;", "verify", "Lcom/safeguard/core/domain/repository/CloudVerificationResponse;", "sha256", "", "sha512", "packageName", "versionCode", "", "permissions", "", "fileSize", "", "targetSdk", "signatureFingerprint", "localLayerScores", "Lcom/safeguard/core/domain/repository/LocalLayerScores;", "deviceMetadata", "Lcom/safeguard/core/domain/repository/DeviceCloudMetadata;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/util/List;JILjava/lang/String;Lcom/safeguard/core/domain/repository/LocalLayerScores;Lcom/safeguard/core/domain/repository/DeviceCloudMetadata;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class CloudVerificationRepositoryImpl implements com.safeguard.core.domain.repository.CloudVerificationRepository {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.remote.api.ThreatIntelligenceApi api = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.concurrent.atomic.AtomicInteger consecutiveFailures = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.concurrent.atomic.AtomicLong circuitOpenUntil = null;
    
    @javax.inject.Inject
    public CloudVerificationRepositoryImpl(@org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.api.ThreatIntelligenceApi api) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object verify(@org.jetbrains.annotations.NotNull
    java.lang.String sha256, @org.jetbrains.annotations.NotNull
    java.lang.String sha512, @org.jetbrains.annotations.NotNull
    java.lang.String packageName, int versionCode, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> permissions, long fileSize, int targetSdk, @org.jetbrains.annotations.Nullable
    java.lang.String signatureFingerprint, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.LocalLayerScores localLayerScores, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.DeviceCloudMetadata deviceMetadata, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.safeguard.core.domain.repository.CloudVerificationResponse> $completion) {
        return null;
    }
}