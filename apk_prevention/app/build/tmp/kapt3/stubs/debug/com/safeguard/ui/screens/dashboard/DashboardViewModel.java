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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00ae\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u0000 D2\u00020\u0001:\u0002DEBI\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\u0002\u0010\u0012J\u0006\u0010\u001e\u001a\u00020\u001fJ\u001e\u0010 \u001a\u00020!2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020$0#2\u0006\u0010%\u001a\u00020!H\u0002J \u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020!2\u0006\u0010)\u001a\u00020!2\u0006\u0010*\u001a\u00020+H\u0002J\u0006\u0010,\u001a\u00020\u001fJ\u0010\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u000200H\u0002J\u0010\u00101\u001a\u00020+2\u0006\u00102\u001a\u00020\'H\u0002J\u0006\u00103\u001a\u00020\u001fJ\u0006\u00104\u001a\u00020\u001fJ.\u00105\u001a\u00020\u001f2\u0006\u00106\u001a\u0002072\n\b\u0002\u00108\u001a\u0004\u0018\u00010+2\u0012\u00109\u001a\u000e\u0012\u0004\u0012\u00020+\u0012\u0004\u0012\u00020\u001f0:J&\u0010;\u001a\u00020\u001f2\u0006\u0010<\u001a\u00020=2\u0006\u0010(\u001a\u00020!2\u0006\u0010)\u001a\u00020!2\u0006\u0010*\u001a\u00020+J\u000e\u0010>\u001a\u00020\u001f2\u0006\u0010<\u001a\u00020=J\u000e\u0010?\u001a\u00020\u001f2\u0006\u0010@\u001a\u00020+J\u0010\u0010A\u001a\u00020B2\u0006\u0010C\u001a\u00020$H\u0002R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00150\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00150\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001d\u00a8\u0006F"}, d2 = {"Lcom/safeguard/ui/screens/dashboard/DashboardViewModel;", "Landroidx/lifecycle/ViewModel;", "context", "Landroid/content/Context;", "scanRepository", "Lcom/safeguard/core/domain/repository/ScanRepository;", "quarantineRepository", "Lcom/safeguard/core/domain/repository/QuarantineRepository;", "preferences", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "scanAPKUseCase", "Lcom/safeguard/core/domain/usecase/ScanAPKUseCase;", "quarantineAPKUseCase", "Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;", "deviceScanManager", "Lcom/safeguard/manager/DeviceScanManager;", "threatFeedRepository", "Lcom/safeguard/core/domain/repository/ThreatFeedRepository;", "(Landroid/content/Context;Lcom/safeguard/core/domain/repository/ScanRepository;Lcom/safeguard/core/domain/repository/QuarantineRepository;Lcom/safeguard/data/local/preferences/SecurePreferencesManager;Lcom/safeguard/core/domain/usecase/ScanAPKUseCase;Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;Lcom/safeguard/manager/DeviceScanManager;Lcom/safeguard/core/domain/repository/ThreatFeedRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/safeguard/ui/screens/dashboard/DashboardUiState;", "derivedState", "Lkotlinx/coroutines/flow/StateFlow;", "prefsState", "Lkotlinx/coroutines/flow/Flow;", "Lcom/safeguard/ui/screens/dashboard/DashboardViewModel$SchedulePrefs;", "uiState", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearScanError", "", "computeSecurityScore", "", "scans", "", "Lcom/safeguard/core/domain/model/ScanResult;", "quarantineCount", "delayUntilNext", "", "hour", "minute", "frequency", "", "dismissInitialScanPrompt", "formatFeedStatus", "Lcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter$Display;", "status", "Lcom/safeguard/core/domain/repository/ThreatFeedStatus;", "formatTimeAgo", "timestamp", "onScanProgressNavigated", "runInitialDeviceScan", "runScan", "apkFile", "Ljava/io/File;", "displayName", "onNavigateToScanResults", "Lkotlin/Function1;", "saveSchedule", "enabled", "", "setMonitoringEnabled", "setScanError", "message", "toRecentScanItem", "Lcom/safeguard/ui/screens/dashboard/RecentScanItem;", "r", "Companion", "SchedulePrefs", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class DashboardViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ScanRepository scanRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.preferences.SecurePreferencesManager preferences = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.usecase.ScanAPKUseCase scanAPKUseCase = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.usecase.QuarantineAPKUseCase quarantineAPKUseCase = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.manager.DeviceScanManager deviceScanManager = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ThreatFeedRepository threatFeedRepository = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.safeguard.ui.screens.dashboard.DashboardUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<com.safeguard.ui.screens.dashboard.DashboardViewModel.SchedulePrefs> prefsState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.dashboard.DashboardUiState> derivedState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.dashboard.DashboardUiState> uiState = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SafeGuard";
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.ui.screens.dashboard.DashboardViewModel.Companion Companion = null;
    
    @javax.inject.Inject
    public DashboardViewModel(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ScanRepository scanRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager preferences, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.usecase.ScanAPKUseCase scanAPKUseCase, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.usecase.QuarantineAPKUseCase quarantineAPKUseCase, @org.jetbrains.annotations.NotNull
    com.safeguard.manager.DeviceScanManager deviceScanManager, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ThreatFeedRepository threatFeedRepository) {
        super();
    }
    
    /**
     * Snapshot the wall clock once per emission so the UI's "Updated 4 hr ago" string
     * doesn't mutate while the dashboard is sitting still — the formatter is otherwise
     * pure, but consumers will keep observing the same `Display` for as long as the
     * underlying status row doesn't change. (Refresh on dashboard re-entry is handled by
     * [SharingStarted.WhileSubscribed]'s replay.)
     */
    private final com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Display formatFeedStatus(com.safeguard.core.domain.repository.ThreatFeedStatus status) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.dashboard.DashboardUiState> getUiState() {
        return null;
    }
    
    private final com.safeguard.ui.screens.dashboard.RecentScanItem toRecentScanItem(com.safeguard.core.domain.model.ScanResult r) {
        return null;
    }
    
    private final java.lang.String formatTimeAgo(long timestamp) {
        return null;
    }
    
    /**
     * Runs full scan on the given APK file; on success navigates with scan ID, on error sets scanError.
     */
    public final void runScan(@org.jetbrains.annotations.NotNull
    java.io.File apkFile, @org.jetbrains.annotations.Nullable
    java.lang.String displayName, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToScanResults) {
    }
    
    public final void clearScanError() {
    }
    
    public final void setScanError(@org.jetbrains.annotations.NotNull
    java.lang.String message) {
    }
    
    public final void setMonitoringEnabled(boolean enabled) {
    }
    
    public final void saveSchedule(boolean enabled, int hour, int minute, @org.jetbrains.annotations.NotNull
    java.lang.String frequency) {
    }
    
    private final long delayUntilNext(int hour, int minute, java.lang.String frequency) {
        return 0L;
    }
    
    public final void dismissInitialScanPrompt() {
    }
    
    public final void runInitialDeviceScan() {
    }
    
    public final void onScanProgressNavigated() {
    }
    
    private final int computeSecurityScore(java.util.List<com.safeguard.core.domain.model.ScanResult> scans, int quarantineCount) {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/safeguard/ui/screens/dashboard/DashboardViewModel$Companion;", "", "()V", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0014\b\u0082\b\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\tH\u00c6\u0003J;\u0010\u0018\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\tH\u00c6\u0001J\u0013\u0010\u0019\u001a\u00020\u00032\b\u0010\u001a\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001b\u001a\u00020\u0006H\u00d6\u0001J\t\u0010\u001c\u001a\u00020\tH\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\fR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011\u00a8\u0006\u001d"}, d2 = {"Lcom/safeguard/ui/screens/dashboard/DashboardViewModel$SchedulePrefs;", "", "monitoring", "", "scheduleEnabled", "scheduleHour", "", "scheduleMinute", "scheduleFrequency", "", "(ZZIILjava/lang/String;)V", "getMonitoring", "()Z", "getScheduleEnabled", "getScheduleFrequency", "()Ljava/lang/String;", "getScheduleHour", "()I", "getScheduleMinute", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
    static final class SchedulePrefs {
        private final boolean monitoring = false;
        private final boolean scheduleEnabled = false;
        private final int scheduleHour = 0;
        private final int scheduleMinute = 0;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String scheduleFrequency = null;
        
        public SchedulePrefs(boolean monitoring, boolean scheduleEnabled, int scheduleHour, int scheduleMinute, @org.jetbrains.annotations.NotNull
        java.lang.String scheduleFrequency) {
            super();
        }
        
        public final boolean getMonitoring() {
            return false;
        }
        
        public final boolean getScheduleEnabled() {
            return false;
        }
        
        public final int getScheduleHour() {
            return 0;
        }
        
        public final int getScheduleMinute() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getScheduleFrequency() {
            return null;
        }
        
        public final boolean component1() {
            return false;
        }
        
        public final boolean component2() {
            return false;
        }
        
        public final int component3() {
            return 0;
        }
        
        public final int component4() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.safeguard.ui.screens.dashboard.DashboardViewModel.SchedulePrefs copy(boolean monitoring, boolean scheduleEnabled, int scheduleHour, int scheduleMinute, @org.jetbrains.annotations.NotNull
        java.lang.String scheduleFrequency) {
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
}