package com.safeguard.ui.screens.dashboard;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;
import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.core.domain.repository.ThreatFeedRepository;
import com.safeguard.core.domain.repository.ThreatFeedStatus;
import com.safeguard.core.domain.usecase.ScanAPKUseCase;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.security.integrity.AntiDebugChecker;
import com.safeguard.security.integrity.RuntimeEnvironmentChecker;
import com.safeguard.notification.SafeGuardNotificationManager;
import com.safeguard.worker.ScheduledScanWorker;
import com.safeguard.core.domain.model.Action;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import java.io.File;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\tH\u00c6\u0003J;\u0010\u0017\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\tH\u00c6\u0001J\u0013\u0010\u0018\u001a\u00020\u00072\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001a\u001a\u00020\u001bH\u00d6\u0001J\t\u0010\u001c\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u000eR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\fR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006\u001d"}, d2 = {"Lcom/safeguard/ui/screens/dashboard/RecentScanItem;", "", "id", "", "apkName", "timeAgo", "isThreat", "", "verdict", "Lcom/safeguard/core/domain/model/Verdict;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLcom/safeguard/core/domain/model/Verdict;)V", "getApkName", "()Ljava/lang/String;", "getId", "()Z", "getTimeAgo", "getVerdict", "()Lcom/safeguard/core/domain/model/Verdict;", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class RecentScanItem {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String apkName = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String timeAgo = null;
    private final boolean isThreat = false;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.model.Verdict verdict = null;
    
    public RecentScanItem(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String apkName, @org.jetbrains.annotations.NotNull
    java.lang.String timeAgo, boolean isThreat, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.Verdict verdict) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getApkName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getTimeAgo() {
        return null;
    }
    
    public final boolean isThreat() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.model.Verdict getVerdict() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
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
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.model.Verdict component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.ui.screens.dashboard.RecentScanItem copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String apkName, @org.jetbrains.annotations.NotNull
    java.lang.String timeAgo, boolean isThreat, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.Verdict verdict) {
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