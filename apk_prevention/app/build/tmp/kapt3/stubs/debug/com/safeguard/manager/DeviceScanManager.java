package com.safeguard.manager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.safeguard.core.domain.model.Action;
import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
import com.safeguard.core.domain.usecase.ScanAPKUseCase;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.notification.SafeGuardNotificationManager;
import com.safeguard.scan.DeepApkCollector;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import java.io.File;
import java.security.MessageDigest;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B1\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u0006\u0010\u0019\u001a\u00020\u001aJ\u0006\u0010\u001b\u001a\u00020\u001aJ\b\u0010\u001c\u001a\u00020\u001aH\u0002J\u0012\u0010\u001d\u001a\u0004\u0018\u00010\u001e2\u0006\u0010\u001f\u001a\u00020 H\u0002J\u0006\u0010!\u001a\u00020\u001aJ\b\u0010\"\u001a\u00020\u001aH\u0002J\u0006\u0010#\u001a\u00020\u001aJ\b\u0010$\u001a\u00020\u001aH\u0002R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lcom/safeguard/manager/DeviceScanManager;", "", "context", "Landroid/content/Context;", "scanAPKUseCase", "Lcom/safeguard/core/domain/usecase/ScanAPKUseCase;", "quarantineAPKUseCase", "Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;", "quarantineRepository", "Lcom/safeguard/core/domain/repository/QuarantineRepository;", "preferences", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "(Landroid/content/Context;Lcom/safeguard/core/domain/usecase/ScanAPKUseCase;Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;Lcom/safeguard/core/domain/repository/QuarantineRepository;Lcom/safeguard/data/local/preferences/SecurePreferencesManager;)V", "_scanState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/safeguard/manager/ScanProgressState;", "managerScope", "Lkotlinx/coroutines/CoroutineScope;", "scanJob", "Lkotlinx/coroutines/Job;", "scanState", "Lkotlinx/coroutines/flow/StateFlow;", "getScanState", "()Lkotlinx/coroutines/flow/StateFlow;", "timerJob", "pauseScan", "", "resetState", "resumeScan", "sha256", "", "file", "Ljava/io/File;", "startFullDeviceScan", "startTimer", "stopScan", "stopTimer", "app_debug"})
public final class DeviceScanManager {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.usecase.ScanAPKUseCase scanAPKUseCase = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.usecase.QuarantineAPKUseCase quarantineAPKUseCase = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.preferences.SecurePreferencesManager preferences = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.safeguard.manager.ScanProgressState> _scanState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.safeguard.manager.ScanProgressState> scanState = null;
    @org.jetbrains.annotations.Nullable
    private kotlinx.coroutines.Job scanJob;
    @org.jetbrains.annotations.Nullable
    private kotlinx.coroutines.Job timerJob;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineScope managerScope = null;
    
    @javax.inject.Inject
    public DeviceScanManager(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.usecase.ScanAPKUseCase scanAPKUseCase, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.usecase.QuarantineAPKUseCase quarantineAPKUseCase, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager preferences) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.safeguard.manager.ScanProgressState> getScanState() {
        return null;
    }
    
    public final void startFullDeviceScan() {
    }
    
    public final void pauseScan() {
    }
    
    private final void resumeScan() {
    }
    
    public final void stopScan() {
    }
    
    public final void resetState() {
    }
    
    private final void startTimer() {
    }
    
    private final void stopTimer() {
    }
    
    private final java.lang.String sha256(java.io.File file) {
        return null;
    }
}