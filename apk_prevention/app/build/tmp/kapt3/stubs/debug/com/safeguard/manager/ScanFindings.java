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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u000e\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0007H\u00c6\u0003J\'\u0010\u0012\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0013\u001a\u00020\u00072\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0018"}, d2 = {"Lcom/safeguard/manager/ScanFindings;", "", "appName", "", "verdict", "Lcom/safeguard/core/domain/model/Verdict;", "quarantined", "", "(Ljava/lang/String;Lcom/safeguard/core/domain/model/Verdict;Z)V", "getAppName", "()Ljava/lang/String;", "getQuarantined", "()Z", "getVerdict", "()Lcom/safeguard/core/domain/model/Verdict;", "component1", "component2", "component3", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class ScanFindings {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String appName = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.model.Verdict verdict = null;
    private final boolean quarantined = false;
    
    public ScanFindings(@org.jetbrains.annotations.NotNull
    java.lang.String appName, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.Verdict verdict, boolean quarantined) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getAppName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.model.Verdict getVerdict() {
        return null;
    }
    
    public final boolean getQuarantined() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.model.Verdict component2() {
        return null;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.manager.ScanFindings copy(@org.jetbrains.annotations.NotNull
    java.lang.String appName, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.Verdict verdict, boolean quarantined) {
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