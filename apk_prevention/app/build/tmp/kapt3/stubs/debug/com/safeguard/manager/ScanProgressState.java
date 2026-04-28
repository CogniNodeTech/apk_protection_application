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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u001b\b\u0086\b\u0018\u00002\u00020\u0001Be\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0007\u0012\b\b\u0002\u0010\t\u001a\u00020\u0007\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f\u00a2\u0006\u0002\u0010\u0011J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010 \u001a\u00020\u0007H\u00c6\u0003J\t\u0010!\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0007H\u00c6\u0003J\t\u0010#\u001a\u00020\u000bH\u00c6\u0003J\t\u0010$\u001a\u00020\rH\u00c6\u0003J\u000f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fH\u00c6\u0003Ji\u0010&\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00072\b\b\u0002\u0010\t\u001a\u00020\u00072\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\r2\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fH\u00c6\u0001J\u0013\u0010\'\u001a\u00020\u00032\b\u0010(\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010)\u001a\u00020\u0007H\u00d6\u0001J\t\u0010*\u001a\u00020\u000bH\u00d6\u0001R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0018R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\u0018R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0018R\u0011\u0010\b\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\t\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001aR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001a\u00a8\u0006+"}, d2 = {"Lcom/safeguard/manager/ScanProgressState;", "", "isScanning", "", "isPaused", "isFinished", "totalApps", "", "scannedApps", "threatsFound", "currentAppLabel", "", "elapsedTimeSec", "", "findings", "", "Lcom/safeguard/manager/ScanFindings;", "(ZZZIIILjava/lang/String;JLjava/util/List;)V", "getCurrentAppLabel", "()Ljava/lang/String;", "getElapsedTimeSec", "()J", "getFindings", "()Ljava/util/List;", "()Z", "getScannedApps", "()I", "getThreatsFound", "getTotalApps", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class ScanProgressState {
    private final boolean isScanning = false;
    private final boolean isPaused = false;
    private final boolean isFinished = false;
    private final int totalApps = 0;
    private final int scannedApps = 0;
    private final int threatsFound = 0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String currentAppLabel = null;
    private final long elapsedTimeSec = 0L;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.safeguard.manager.ScanFindings> findings = null;
    
    public ScanProgressState(boolean isScanning, boolean isPaused, boolean isFinished, int totalApps, int scannedApps, int threatsFound, @org.jetbrains.annotations.NotNull
    java.lang.String currentAppLabel, long elapsedTimeSec, @org.jetbrains.annotations.NotNull
    java.util.List<com.safeguard.manager.ScanFindings> findings) {
        super();
    }
    
    public final boolean isScanning() {
        return false;
    }
    
    public final boolean isPaused() {
        return false;
    }
    
    public final boolean isFinished() {
        return false;
    }
    
    public final int getTotalApps() {
        return 0;
    }
    
    public final int getScannedApps() {
        return 0;
    }
    
    public final int getThreatsFound() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCurrentAppLabel() {
        return null;
    }
    
    public final long getElapsedTimeSec() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.safeguard.manager.ScanFindings> getFindings() {
        return null;
    }
    
    public ScanProgressState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final boolean component3() {
        return false;
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int component5() {
        return 0;
    }
    
    public final int component6() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component7() {
        return null;
    }
    
    public final long component8() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.safeguard.manager.ScanFindings> component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.manager.ScanProgressState copy(boolean isScanning, boolean isPaused, boolean isFinished, int totalApps, int scannedApps, int threatsFound, @org.jetbrains.annotations.NotNull
    java.lang.String currentAppLabel, long elapsedTimeSec, @org.jetbrains.annotations.NotNull
    java.util.List<com.safeguard.manager.ScanFindings> findings) {
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