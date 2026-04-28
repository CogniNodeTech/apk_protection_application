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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b2\b\u0086\b\u0018\u00002\u00020\u0001B\u00d1\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\t\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0005\u0012\u000e\b\u0002\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011\u0012\b\b\u0002\u0010\u0013\u001a\u00020\t\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0017\u001a\u00020\t\u0012\b\b\u0002\u0010\u0018\u001a\u00020\t\u0012\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u001a\u00a2\u0006\u0002\u0010\u001bJ\t\u00104\u001a\u00020\u0003H\u00c6\u0003J\t\u00105\u001a\u00020\u0005H\u00c6\u0003J\t\u00106\u001a\u00020\u0005H\u00c6\u0003J\u000f\u00107\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011H\u00c6\u0003J\t\u00108\u001a\u00020\tH\u00c6\u0003J\t\u00109\u001a\u00020\u0005H\u00c6\u0003J\t\u0010:\u001a\u00020\u0005H\u00c6\u0003J\t\u0010;\u001a\u00020\u0003H\u00c6\u0003J\t\u0010<\u001a\u00020\tH\u00c6\u0003J\t\u0010=\u001a\u00020\tH\u00c6\u0003J\u000b\u0010>\u001a\u0004\u0018\u00010\u001aH\u00c6\u0003J\t\u0010?\u001a\u00020\u0005H\u00c6\u0003J\t\u0010@\u001a\u00020\u0003H\u00c6\u0003J\t\u0010A\u001a\u00020\u0005H\u00c6\u0003J\t\u0010B\u001a\u00020\tH\u00c6\u0003J\t\u0010C\u001a\u00020\tH\u00c6\u0003J\u000b\u0010D\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010E\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010F\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u00d5\u0001\u0010G\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u000e\u001a\u00020\u00052\b\b\u0002\u0010\u000f\u001a\u00020\u00052\u000e\b\u0002\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u00112\b\b\u0002\u0010\u0013\u001a\u00020\t2\b\b\u0002\u0010\u0014\u001a\u00020\u00052\b\b\u0002\u0010\u0015\u001a\u00020\u00052\b\b\u0002\u0010\u0016\u001a\u00020\u00032\b\b\u0002\u0010\u0017\u001a\u00020\t2\b\b\u0002\u0010\u0018\u001a\u00020\t2\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u00c6\u0001J\u0013\u0010H\u001a\u00020\t2\b\u0010I\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010J\u001a\u00020\u0005H\u00d6\u0001J\t\u0010K\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u000e\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0013\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0013\u0010\r\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001fR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0011\u0010\u0018\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\"R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001fR\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001dR\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u001fR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001fR\u0011\u0010\n\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\"R\u0011\u0010\u0013\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\"R\u0011\u0010\u0016\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u001fR\u0011\u0010\u0014\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010\u001dR\u0011\u0010\u0015\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\u001dR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010\u001dR\u0011\u0010\u0017\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010\"R\u0013\u0010\u0019\u001a\u0004\u0018\u00010\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u00102R\u0011\u0010\u000f\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010\u001d\u00a8\u0006L"}, d2 = {"Lcom/safeguard/ui/screens/dashboard/DashboardUiState;", "", "protectionStatus", "", "securityScore", "", "recentScansSummary", "quarantineCount", "monitoringEnabled", "", "scanInProgress", "scanError", "environmentWarning", "lastScanAgo", "appsScannedToday", "threatsBlockedToday", "recentScans", "", "Lcom/safeguard/ui/screens/dashboard/RecentScanItem;", "scheduleEnabled", "scheduleHour", "scheduleMinute", "scheduleFrequency", "showInitialScanPrompt", "navigateToScanProgress", "threatFeedStatus", "Lcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter$Display;", "(Ljava/lang/String;ILjava/lang/String;IZZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;IILjava/util/List;ZIILjava/lang/String;ZZLcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter$Display;)V", "getAppsScannedToday", "()I", "getEnvironmentWarning", "()Ljava/lang/String;", "getLastScanAgo", "getMonitoringEnabled", "()Z", "getNavigateToScanProgress", "getProtectionStatus", "getQuarantineCount", "getRecentScans", "()Ljava/util/List;", "getRecentScansSummary", "getScanError", "getScanInProgress", "getScheduleEnabled", "getScheduleFrequency", "getScheduleHour", "getScheduleMinute", "getSecurityScore", "getShowInitialScanPrompt", "getThreatFeedStatus", "()Lcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter$Display;", "getThreatsBlockedToday", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class DashboardUiState {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String protectionStatus = null;
    private final int securityScore = 0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String recentScansSummary = null;
    private final int quarantineCount = 0;
    private final boolean monitoringEnabled = false;
    private final boolean scanInProgress = false;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String scanError = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String environmentWarning = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String lastScanAgo = null;
    private final int appsScannedToday = 0;
    private final int threatsBlockedToday = 0;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.safeguard.ui.screens.dashboard.RecentScanItem> recentScans = null;
    private final boolean scheduleEnabled = false;
    private final int scheduleHour = 0;
    private final int scheduleMinute = 0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String scheduleFrequency = null;
    private final boolean showInitialScanPrompt = false;
    private final boolean navigateToScanProgress = false;
    
    /**
     * Formatted threat-feed sync status. `null` until the first emission from
     * [ThreatFeedRepository.observeStatus] arrives — the dashboard should hide the tile
     * during that brief window rather than render with placeholder strings.
     */
    @org.jetbrains.annotations.Nullable
    private final com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Display threatFeedStatus = null;
    
    public DashboardUiState(@org.jetbrains.annotations.NotNull
    java.lang.String protectionStatus, int securityScore, @org.jetbrains.annotations.NotNull
    java.lang.String recentScansSummary, int quarantineCount, boolean monitoringEnabled, boolean scanInProgress, @org.jetbrains.annotations.Nullable
    java.lang.String scanError, @org.jetbrains.annotations.Nullable
    java.lang.String environmentWarning, @org.jetbrains.annotations.Nullable
    java.lang.String lastScanAgo, int appsScannedToday, int threatsBlockedToday, @org.jetbrains.annotations.NotNull
    java.util.List<com.safeguard.ui.screens.dashboard.RecentScanItem> recentScans, boolean scheduleEnabled, int scheduleHour, int scheduleMinute, @org.jetbrains.annotations.NotNull
    java.lang.String scheduleFrequency, boolean showInitialScanPrompt, boolean navigateToScanProgress, @org.jetbrains.annotations.Nullable
    com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Display threatFeedStatus) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getProtectionStatus() {
        return null;
    }
    
    public final int getSecurityScore() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getRecentScansSummary() {
        return null;
    }
    
    public final int getQuarantineCount() {
        return 0;
    }
    
    public final boolean getMonitoringEnabled() {
        return false;
    }
    
    public final boolean getScanInProgress() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getScanError() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getEnvironmentWarning() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLastScanAgo() {
        return null;
    }
    
    public final int getAppsScannedToday() {
        return 0;
    }
    
    public final int getThreatsBlockedToday() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.safeguard.ui.screens.dashboard.RecentScanItem> getRecentScans() {
        return null;
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
    
    public final boolean getShowInitialScanPrompt() {
        return false;
    }
    
    public final boolean getNavigateToScanProgress() {
        return false;
    }
    
    /**
     * Formatted threat-feed sync status. `null` until the first emission from
     * [ThreatFeedRepository.observeStatus] arrives — the dashboard should hide the tile
     * during that brief window rather than render with placeholder strings.
     */
    @org.jetbrains.annotations.Nullable
    public final com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Display getThreatFeedStatus() {
        return null;
    }
    
    public DashboardUiState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    public final int component10() {
        return 0;
    }
    
    public final int component11() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.safeguard.ui.screens.dashboard.RecentScanItem> component12() {
        return null;
    }
    
    public final boolean component13() {
        return false;
    }
    
    public final int component14() {
        return 0;
    }
    
    public final int component15() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component16() {
        return null;
    }
    
    public final boolean component17() {
        return false;
    }
    
    public final boolean component18() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Display component19() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    public final int component4() {
        return 0;
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component7() {
        return null;
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
    public final com.safeguard.ui.screens.dashboard.DashboardUiState copy(@org.jetbrains.annotations.NotNull
    java.lang.String protectionStatus, int securityScore, @org.jetbrains.annotations.NotNull
    java.lang.String recentScansSummary, int quarantineCount, boolean monitoringEnabled, boolean scanInProgress, @org.jetbrains.annotations.Nullable
    java.lang.String scanError, @org.jetbrains.annotations.Nullable
    java.lang.String environmentWarning, @org.jetbrains.annotations.Nullable
    java.lang.String lastScanAgo, int appsScannedToday, int threatsBlockedToday, @org.jetbrains.annotations.NotNull
    java.util.List<com.safeguard.ui.screens.dashboard.RecentScanItem> recentScans, boolean scheduleEnabled, int scheduleHour, int scheduleMinute, @org.jetbrains.annotations.NotNull
    java.lang.String scheduleFrequency, boolean showInitialScanPrompt, boolean navigateToScanProgress, @org.jetbrains.annotations.Nullable
    com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Display threatFeedStatus) {
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