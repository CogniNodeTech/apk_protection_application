package com.safeguard.domain;

import com.safeguard.core.domain.model.Action;
import com.safeguard.core.domain.model.MalwareCategory;
import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.model.ThreatInfo;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.core.domain.telemetry.ScanTelemetry;
import com.safeguard.core.domain.usecase.ScanAPKUseCase;
import com.safeguard.core.orchestration.ScanOrchestrator;
import java.io.File;
import java.util.UUID;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.Dispatchers;
import android.content.Context;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\u0018\u00002\u00020\u0001B)\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ \u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0096@\u00a2\u0006\u0002\u0010\u0011J*\u0010\u0012\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u00102\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/safeguard/domain/ScanAPKUseCaseImpl;", "Lcom/safeguard/core/domain/usecase/ScanAPKUseCase;", "context", "Landroid/content/Context;", "orchestrator", "Lcom/safeguard/core/orchestration/ScanOrchestrator;", "scanRepository", "Lcom/safeguard/core/domain/repository/ScanRepository;", "telemetry", "Lcom/safeguard/core/domain/telemetry/ScanTelemetry;", "(Landroid/content/Context;Lcom/safeguard/core/orchestration/ScanOrchestrator;Lcom/safeguard/core/domain/repository/ScanRepository;Lcom/safeguard/core/domain/telemetry/ScanTelemetry;)V", "execute", "Lcom/safeguard/core/domain/model/ScanResult;", "apkFile", "Ljava/io/File;", "displayName", "", "(Ljava/io/File;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "trustedHostAppScanResult", "pkgInfo", "Landroid/content/pm/PackageInfo;", "startedAt", "", "app_debug"})
public final class ScanAPKUseCaseImpl implements com.safeguard.core.domain.usecase.ScanAPKUseCase {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.orchestration.ScanOrchestrator orchestrator = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ScanRepository scanRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.telemetry.ScanTelemetry telemetry = null;
    
    @javax.inject.Inject
    public ScanAPKUseCaseImpl(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.safeguard.core.orchestration.ScanOrchestrator orchestrator, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ScanRepository scanRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.telemetry.ScanTelemetry telemetry) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object execute(@org.jetbrains.annotations.NotNull
    java.io.File apkFile, @org.jetbrains.annotations.Nullable
    java.lang.String displayName, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.safeguard.core.domain.model.ScanResult> $completion) {
        return null;
    }
    
    /**
     * Never score the host app as malware (device scans include our own base.apk).
     */
    private final com.safeguard.core.domain.model.ScanResult trustedHostAppScanResult(java.io.File apkFile, java.lang.String displayName, android.content.pm.PackageInfo pkgInfo, long startedAt) {
        return null;
    }
}