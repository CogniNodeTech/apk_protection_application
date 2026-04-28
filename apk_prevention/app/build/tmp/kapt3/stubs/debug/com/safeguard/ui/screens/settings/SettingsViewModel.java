package com.safeguard.ui.screens.settings;

import androidx.lifecycle.ViewModel;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.repository.ScanFeedbackRepository;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0012\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\f\b\u0007\u0018\u00002\u00020\u0001:\u0001FB/\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u0016\u00104\u001a\u0002052\u000e\b\u0002\u00106\u001a\b\u0012\u0004\u0012\u00020507J\u0014\u00108\u001a\u0002052\f\u00106\u001a\b\u0012\u0004\u0012\u00020507J\u001a\u00109\u001a\u0002052\u0012\u0010:\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u0002050;J\u0006\u0010<\u001a\u000205J\u000e\u0010=\u001a\u0002052\u0006\u0010>\u001a\u00020\u000fJ\u000e\u0010?\u001a\u0002052\u0006\u0010>\u001a\u00020\u000fJ\u000e\u0010@\u001a\u0002052\u0006\u0010>\u001a\u00020\u000fJ\u000e\u0010A\u001a\u0002052\u0006\u0010B\u001a\u00020\u0013J\u000e\u0010C\u001a\u0002052\u0006\u0010>\u001a\u00020\u000fJ\u000e\u0010D\u001a\u0002052\u0006\u0010>\u001a\u00020\u000fJ\u000e\u0010E\u001a\u0002052\u0006\u0010>\u001a\u00020\u000fR\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00160\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u000f0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0017\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u000f0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001cRW\u0010\u001f\u001a>\u0012\u0018\u0012\u0016\u0012\u0004\u0012\u00020\" #*\n\u0012\u0004\u0012\u00020\"\u0018\u00010!0! #*\u001e\u0012\u0018\u0012\u0016\u0012\u0004\u0012\u00020\" #*\n\u0012\u0004\u0012\u00020\"\u0018\u00010!0!\u0018\u00010 0 8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b&\u0010\'\u001a\u0004\b$\u0010%R\u0017\u0010(\u001a\b\u0012\u0004\u0012\u00020\u000f0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001cR\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00130\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u001cR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010,\u001a\b\u0012\u0004\u0012\u00020\u000f0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010\u001cR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00160\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010\u001cR\u0017\u00100\u001a\b\u0012\u0004\u0012\u00020\u000f0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010\u001cR\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u00102\u001a\b\u0012\u0004\u0012\u00020\u000f0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010\u001c\u00a8\u0006G"}, d2 = {"Lcom/safeguard/ui/screens/settings/SettingsViewModel;", "Landroidx/lifecycle/ViewModel;", "preferences", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "scanRepository", "Lcom/safeguard/core/domain/repository/ScanRepository;", "quarantineRepository", "Lcom/safeguard/core/domain/repository/QuarantineRepository;", "scanFeedbackRepository", "Lcom/safeguard/core/domain/repository/ScanFeedbackRepository;", "moshi", "Lcom/squareup/moshi/Moshi;", "(Lcom/safeguard/data/local/preferences/SecurePreferencesManager;Lcom/safeguard/core/domain/repository/ScanRepository;Lcom/safeguard/core/domain/repository/QuarantineRepository;Lcom/safeguard/core/domain/repository/ScanFeedbackRepository;Lcom/squareup/moshi/Moshi;)V", "_cloudVerificationEnabled", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_deepScanEnabled", "_monitoringEnabled", "_notificationLevel", "", "_privacySharingOptOut", "_queuedFeedbackCount", "", "_scanFeedbackEnabled", "_scanTelemetryEnabled", "cloudVerificationEnabled", "Lkotlinx/coroutines/flow/StateFlow;", "getCloudVerificationEnabled", "()Lkotlinx/coroutines/flow/StateFlow;", "deepScanEnabled", "getDeepScanEnabled", "exportListAdapter", "Lcom/squareup/moshi/JsonAdapter;", "", "Lcom/safeguard/ui/screens/settings/SettingsViewModel$ScanExportRow;", "kotlin.jvm.PlatformType", "getExportListAdapter", "()Lcom/squareup/moshi/JsonAdapter;", "exportListAdapter$delegate", "Lkotlin/Lazy;", "monitoringEnabled", "getMonitoringEnabled", "notificationLevel", "getNotificationLevel", "privacySharingOptOut", "getPrivacySharingOptOut", "queuedFeedbackCount", "getQueuedFeedbackCount", "scanFeedbackEnabled", "getScanFeedbackEnabled", "scanTelemetryEnabled", "getScanTelemetryEnabled", "clearFeedbackQueue", "", "onComplete", "Lkotlin/Function0;", "deleteAllData", "exportScanHistoryJson", "onResult", "Lkotlin/Function1;", "refreshFeedbackCount", "setCloudVerification", "enabled", "setDeepScan", "setMonitoring", "setNotificationLevel", "level", "setPrivacySharingOptOut", "setScanFeedback", "setScanTelemetry", "ScanExportRow", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class SettingsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.preferences.SecurePreferencesManager preferences = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ScanRepository scanRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ScanFeedbackRepository scanFeedbackRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.squareup.moshi.Moshi moshi = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy exportListAdapter$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _monitoringEnabled = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> monitoringEnabled = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _deepScanEnabled = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> deepScanEnabled = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _notificationLevel = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> notificationLevel = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _cloudVerificationEnabled = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> cloudVerificationEnabled = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _scanTelemetryEnabled = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> scanTelemetryEnabled = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _privacySharingOptOut = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> privacySharingOptOut = null;
    
    /**
     * Phase 3.2 — opt-in scan feedback toggle. Default false; user must explicitly opt in
     * for any feedback rows to be persisted or uploaded.
     */
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _scanFeedbackEnabled = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> scanFeedbackEnabled = null;
    
    /**
     * Live count of queued (un-uploaded) feedback rows. Refreshed after toggle/purge so
     * the UI can show "12 events queued" alongside the toggle without polling.
     */
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _queuedFeedbackCount = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> queuedFeedbackCount = null;
    
    @javax.inject.Inject
    public SettingsViewModel(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager preferences, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ScanRepository scanRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ScanFeedbackRepository scanFeedbackRepository, @org.jetbrains.annotations.NotNull
    com.squareup.moshi.Moshi moshi) {
        super();
    }
    
    private final com.squareup.moshi.JsonAdapter<java.util.List<com.safeguard.ui.screens.settings.SettingsViewModel.ScanExportRow>> getExportListAdapter() {
        return null;
    }
    
    public final void deleteAllData(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onComplete) {
    }
    
    public final void exportScanHistoryJson(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onResult) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getMonitoringEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getDeepScanEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getNotificationLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getCloudVerificationEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getScanTelemetryEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getPrivacySharingOptOut() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getScanFeedbackEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getQueuedFeedbackCount() {
        return null;
    }
    
    public final void setMonitoring(boolean enabled) {
    }
    
    public final void setDeepScan(boolean enabled) {
    }
    
    public final void setNotificationLevel(@org.jetbrains.annotations.NotNull
    java.lang.String level) {
    }
    
    public final void setCloudVerification(boolean enabled) {
    }
    
    public final void setScanTelemetry(boolean enabled) {
    }
    
    public final void setPrivacySharingOptOut(boolean enabled) {
    }
    
    /**
     * Flip the Phase 3.2 feedback opt-in. Toggling off does NOT purge already-queued
     * rows on its own — that's a separate explicit action ([clearFeedbackQueue]) so
     * users can re-enable later without losing what they previously consented to share.
     */
    public final void setScanFeedback(boolean enabled) {
    }
    
    /**
     * Wipe every queued feedback row. Returns immediately; the actual count update is
     * pushed via [queuedFeedbackCount] once the DB delete completes.
     */
    public final void clearFeedbackQueue(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onComplete) {
    }
    
    /**
     * Pull the current queue count into [queuedFeedbackCount]. Cheap; safe to call from UI.
     */
    public final void refreshFeedbackCount() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0015\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0082\b\u0018\u00002\u00020\u0001B=\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\nH\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003JO\u0010\u001e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u001f\u001a\u00020 2\b\u0010!\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\"\u001a\u00020\nH\u00d6\u0001J\t\u0010#\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000eR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000eR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000eR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016\u00a8\u0006$"}, d2 = {"Lcom/safeguard/ui/screens/settings/SettingsViewModel$ScanExportRow;", "", "id", "", "apkName", "apkPath", "scanTimestamp", "", "finalVerdict", "overallRiskScore", "", "recommendedAction", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JLjava/lang/String;ILjava/lang/String;)V", "getApkName", "()Ljava/lang/String;", "getApkPath", "getFinalVerdict", "getId", "getOverallRiskScore", "()I", "getRecommendedAction", "getScanTimestamp", "()J", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
    static final class ScanExportRow {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String id = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String apkName = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String apkPath = null;
        private final long scanTimestamp = 0L;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String finalVerdict = null;
        private final int overallRiskScore = 0;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String recommendedAction = null;
        
        public ScanExportRow(@org.jetbrains.annotations.NotNull
        java.lang.String id, @org.jetbrains.annotations.NotNull
        java.lang.String apkName, @org.jetbrains.annotations.NotNull
        java.lang.String apkPath, long scanTimestamp, @org.jetbrains.annotations.NotNull
        java.lang.String finalVerdict, int overallRiskScore, @org.jetbrains.annotations.NotNull
        java.lang.String recommendedAction) {
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
        public final java.lang.String getApkPath() {
            return null;
        }
        
        public final long getScanTimestamp() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getFinalVerdict() {
            return null;
        }
        
        public final int getOverallRiskScore() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getRecommendedAction() {
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
        
        public final long component4() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component5() {
            return null;
        }
        
        public final int component6() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component7() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.safeguard.ui.screens.settings.SettingsViewModel.ScanExportRow copy(@org.jetbrains.annotations.NotNull
        java.lang.String id, @org.jetbrains.annotations.NotNull
        java.lang.String apkName, @org.jetbrains.annotations.NotNull
        java.lang.String apkPath, long scanTimestamp, @org.jetbrains.annotations.NotNull
        java.lang.String finalVerdict, int overallRiskScore, @org.jetbrains.annotations.NotNull
        java.lang.String recommendedAction) {
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