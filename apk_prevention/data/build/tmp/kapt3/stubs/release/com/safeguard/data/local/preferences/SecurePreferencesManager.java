package com.safeguard.data.local.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0015\n\u0002\u0018\u0002\n\u0002\b=\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u0000 \u008b\u00012\u00020\u0001:\u0002\u008b\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010z\u001a\u00020{J\u0006\u0010|\u001a\u00020\fJ\u0006\u0010}\u001a\u00020\fJ\u0006\u0010~\u001a\u00020{J\u0006\u0010\u007f\u001a\u00020{J4\u0010\u0080\u0001\u001a\t\u0012\u0005\u0012\u0003H\u0081\u00010\u0013\"\u0005\b\u0000\u0010\u0081\u00012\u0007\u0010\u0082\u0001\u001a\u00020\u00062\u0011\b\u0004\u0010\u0083\u0001\u001a\n\u0012\u0005\u0012\u0003H\u0081\u00010\u0084\u0001H\u0082\bJ6\u0010\u0085\u0001\u001a\u00020{2\u0007\u0010\u0086\u0001\u001a\u00020!2\u0007\u0010\u0087\u0001\u001a\u00020!2\u0007\u0010\u0088\u0001\u001a\u00020\u00062\t\u0010\u0089\u0001\u001a\u0004\u0018\u00010\u00062\u0007\u0010\u008a\u0001\u001a\u00020\'R(\u0010\u0007\u001a\u0004\u0018\u00010\u00062\b\u0010\u0005\u001a\u0004\u0018\u00010\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR$\u0010\r\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\f0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R$\u0010\u0016\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0017\u0010\u000f\"\u0004\b\u0018\u0010\u0011R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\f0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0015R$\u0010\u001b\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u001c\u0010\u000f\"\u0004\b\u001d\u0010\u0011R$\u0010\u001e\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u001e\u0010\u000f\"\u0004\b\u001f\u0010\u0011R\u0011\u0010 \u001a\u00020!8F\u00a2\u0006\u0006\u001a\u0004\b\"\u0010#R\u0013\u0010$\u001a\u0004\u0018\u00010\u00068F\u00a2\u0006\u0006\u001a\u0004\b%\u0010\tR\u0011\u0010&\u001a\u00020\'8F\u00a2\u0006\u0006\u001a\u0004\b(\u0010)R\u0011\u0010*\u001a\u00020\u00068F\u00a2\u0006\u0006\u001a\u0004\b+\u0010\tR\u0017\u0010,\u001a\b\u0012\u0004\u0012\u00020!0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010\u0015R\u0011\u0010.\u001a\u00020!8F\u00a2\u0006\u0006\u001a\u0004\b/\u0010#R$\u00100\u001a\u00020!2\u0006\u0010\u0005\u001a\u00020!8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b1\u0010#\"\u0004\b2\u00103R$\u00104\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b5\u0010\t\"\u0004\b6\u0010\u000bR\u0017\u00107\u001a\b\u0012\u0004\u0012\u00020\u00060\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010\u0015R$\u00109\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b:\u0010\u000f\"\u0004\b;\u0010\u0011R\u000e\u0010<\u001a\u00020=X\u0082\u0004\u00a2\u0006\u0002\n\u0000R$\u0010>\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b?\u0010\u000f\"\u0004\b@\u0010\u0011R$\u0010A\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bB\u0010\u000f\"\u0004\bC\u0010\u0011R\u0017\u0010D\u001a\b\u0012\u0004\u0012\u00020\f0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010\u0015R$\u0010F\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bG\u0010\u000f\"\u0004\bH\u0010\u0011R\u0017\u0010I\u001a\b\u0012\u0004\u0012\u00020\f0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\bJ\u0010\u0015R(\u0010K\u001a\u0004\u0018\u00010\u00062\b\u0010\u0005\u001a\u0004\u0018\u00010\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bL\u0010\t\"\u0004\bM\u0010\u000bR(\u0010N\u001a\u0004\u0018\u00010\u00062\b\u0010\u0005\u001a\u0004\u0018\u00010\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bO\u0010\t\"\u0004\bP\u0010\u000bR(\u0010Q\u001a\u0004\u0018\u00010\u00062\b\u0010\u0005\u001a\u0004\u0018\u00010\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bR\u0010\t\"\u0004\bS\u0010\u000bR(\u0010T\u001a\u0004\u0018\u00010\u00062\b\u0010\u0005\u001a\u0004\u0018\u00010\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bU\u0010\t\"\u0004\bV\u0010\u000bR$\u0010W\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bX\u0010\u000f\"\u0004\bY\u0010\u0011R$\u0010Z\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b[\u0010\u000f\"\u0004\b\\\u0010\u0011R$\u0010]\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b^\u0010\t\"\u0004\b_\u0010\u000bR\u0017\u0010`\u001a\b\u0012\u0004\u0012\u00020\u00060\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\ba\u0010\u0015R$\u0010b\u001a\u00020\'2\u0006\u0010\u0005\u001a\u00020\'8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bc\u0010)\"\u0004\bd\u0010eR\u0017\u0010f\u001a\b\u0012\u0004\u0012\u00020\'0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\bg\u0010\u0015R$\u0010h\u001a\u00020\'2\u0006\u0010\u0005\u001a\u00020\'8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bi\u0010)\"\u0004\bj\u0010eR\u0017\u0010k\u001a\b\u0012\u0004\u0012\u00020\'0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\bl\u0010\u0015R$\u0010m\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bn\u0010\u000f\"\u0004\bo\u0010\u0011R\u0017\u0010p\u001a\b\u0012\u0004\u0012\u00020\f0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\bq\u0010\u0015R$\u0010r\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bs\u0010\u000f\"\u0004\bt\u0010\u0011R$\u0010u\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bv\u0010\t\"\u0004\bw\u0010\u000bR\u0017\u0010x\u001a\b\u0012\u0004\u0012\u00020\u00060\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\by\u0010\u0015\u00a8\u0006\u008c\u0001"}, d2 = {"Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "value", "", "authAccessToken", "getAuthAccessToken", "()Ljava/lang/String;", "setAuthAccessToken", "(Ljava/lang/String;)V", "", "cloudVerificationEnabled", "getCloudVerificationEnabled", "()Z", "setCloudVerificationEnabled", "(Z)V", "cloudVerificationEnabledFlow", "Lkotlinx/coroutines/flow/Flow;", "getCloudVerificationEnabledFlow", "()Lkotlinx/coroutines/flow/Flow;", "deepScanModeEnabled", "getDeepScanModeEnabled", "setDeepScanModeEnabled", "deepScanModeEnabledFlow", "getDeepScanModeEnabledFlow", "hasCompletedInitialScan", "getHasCompletedInitialScan", "setHasCompletedInitialScan", "isAuthenticated", "setAuthenticated", "lastThreatFeedAttemptMs", "", "getLastThreatFeedAttemptMs", "()J", "lastThreatFeedFailureReason", "getLastThreatFeedFailureReason", "lastThreatFeedInsertedCount", "", "getLastThreatFeedInsertedCount", "()I", "lastThreatFeedOutcomeRaw", "getLastThreatFeedOutcomeRaw", "lastThreatFeedStatusEpochFlow", "getLastThreatFeedStatusEpochFlow", "lastThreatFeedSuccessMs", "getLastThreatFeedSuccessMs", "lastThreatFeedSyncMs", "getLastThreatFeedSyncMs", "setLastThreatFeedSyncMs", "(J)V", "notificationLevel", "getNotificationLevel", "setNotificationLevel", "notificationLevelFlow", "getNotificationLevelFlow", "permissionOnboardingAcknowledged", "getPermissionOnboardingAcknowledged", "setPermissionOnboardingAcknowledged", "prefs", "Landroid/content/SharedPreferences;", "privacyOnboardingAcknowledged", "getPrivacyOnboardingAcknowledged", "setPrivacyOnboardingAcknowledged", "privacySharingOptOut", "getPrivacySharingOptOut", "setPrivacySharingOptOut", "privacySharingOptOutFlow", "getPrivacySharingOptOutFlow", "realTimeMonitoringEnabled", "getRealTimeMonitoringEnabled", "setRealTimeMonitoringEnabled", "realTimeMonitoringEnabledFlow", "getRealTimeMonitoringEnabledFlow", "registeredEmail", "getRegisteredEmail", "setRegisteredEmail", "registeredFullName", "getRegisteredFullName", "setRegisteredFullName", "registeredPassword", "getRegisteredPassword", "setRegisteredPassword", "registeredPhone", "getRegisteredPhone", "setRegisteredPhone", "requireOnboardingAfterRegistration", "getRequireOnboardingAfterRegistration", "setRequireOnboardingAfterRegistration", "scanTelemetryEnabled", "getScanTelemetryEnabled", "setScanTelemetryEnabled", "scheduleFrequency", "getScheduleFrequency", "setScheduleFrequency", "scheduleFrequencyFlow", "getScheduleFrequencyFlow", "scheduleHour", "getScheduleHour", "setScheduleHour", "(I)V", "scheduleHourFlow", "getScheduleHourFlow", "scheduleMinute", "getScheduleMinute", "setScheduleMinute", "scheduleMinuteFlow", "getScheduleMinuteFlow", "scheduleScanEnabled", "getScheduleScanEnabled", "setScheduleScanEnabled", "scheduleScanEnabledFlow", "getScheduleScanEnabledFlow", "termsAndConditionsAccepted", "getTermsAndConditionsAccepted", "setTermsAndConditionsAccepted", "themeMode", "getThemeMode", "setThemeMode", "themeModeFlow", "getThemeModeFlow", "clearAuthSession", "", "hasAuthSessionPreference", "hasTermsAndConditionsPreference", "migrateAuthenticationForExistingUsers", "migrateTermsAcceptanceForExistingUsers", "prefsFlow", "T", "key", "read", "Lkotlin/Function0;", "writeThreatFeedStatus", "successMs", "attemptMs", "outcomeRaw", "failureReason", "insertedCount", "Companion", "data_release"})
public final class SecurePreferencesManager {
    @org.jetbrains.annotations.NotNull
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.Boolean> realTimeMonitoringEnabledFlow = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.String> notificationLevelFlow = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.Boolean> deepScanModeEnabledFlow = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.String> themeModeFlow = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.Boolean> cloudVerificationEnabledFlow = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.Boolean> privacySharingOptOutFlow = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.Boolean> scheduleScanEnabledFlow = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.Integer> scheduleHourFlow = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.Integer> scheduleMinuteFlow = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.String> scheduleFrequencyFlow = null;
    
    /**
     * Cold flow that fires once every time [writeThreatFeedStatus] commits a new bundle.
     * The emitted [Long] is the bumped epoch counter (opaque — observers should re-read
     * the status fields, not interpret the value). Used by the [ThreatFeedStatusStore]
     * adapter to drive `observe()`.
     */
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.Long> lastThreatFeedStatusEpochFlow = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SecurePreferences";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_MONITORING = "real_time_monitoring";
    
    /**
     * Real-time monitoring is ON by default for new users.
     */
    private static final boolean DEFAULT_REAL_TIME_MONITORING = true;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_NOTIFICATION_LEVEL = "notification_level";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_DEEP_SCAN = "deep_scan_mode";
    private static final boolean DEFAULT_DEEP_SCAN = true;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_THEME_MODE = "theme_mode";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_SCHEDULE_ENABLED = "schedule_scan_enabled";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_SCHEDULE_HOUR = "schedule_hour";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_SCHEDULE_MINUTE = "schedule_minute";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_SCHEDULE_FREQUENCY = "schedule_frequency";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String FREQ_DAILY = "daily";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String FREQ_WEEKLY = "weekly";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String THEME_DARK = "dark";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String THEME_LIGHT = "light";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String THEME_SYSTEM = "system";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String LEVEL_SILENT = "silent";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String LEVEL_NORMAL = "normal";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String LEVEL_VERBOSE = "verbose";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_INITIAL_SCAN = "has_completed_initial_scan";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_AUTH_IS_AUTHENTICATED = "auth_is_authenticated";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_AUTH_REGISTERED_EMAIL = "auth_registered_email";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_AUTH_REGISTERED_PASSWORD = "auth_registered_password";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_AUTH_REGISTERED_FULL_NAME = "auth_registered_full_name";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_AUTH_REGISTERED_PHONE = "auth_registered_phone";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_AUTH_ACCESS_TOKEN = "auth_access_token";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_REQUIRE_ONBOARDING_AFTER_REGISTRATION = "require_onboarding_after_registration";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_TERMS_CONDITIONS = "terms_and_conditions_accepted";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_PRIVACY_ONBOARDING = "privacy_onboarding_acknowledged";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_PERMISSION_ONBOARDING = "permission_onboarding_acknowledged";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_CLOUD_VERIFICATION = "cloud_verification_enabled";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_SCAN_TELEMETRY = "scan_telemetry_enabled";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_PRIVACY_SHARING_OPT_OUT = "privacy_sharing_opt_out";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_LAST_THREAT_FEED_SYNC = "last_threat_feed_sync_ms";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_LAST_THREAT_FEED_SUCCESS_MS = "last_threat_feed_success_ms";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_LAST_THREAT_FEED_ATTEMPT_MS = "last_threat_feed_attempt_ms";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_LAST_THREAT_FEED_OUTCOME = "last_threat_feed_outcome";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_LAST_THREAT_FEED_FAILURE_REASON = "last_threat_feed_failure_reason";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_LAST_THREAT_FEED_INSERTED_COUNT = "last_threat_feed_inserted_count";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_LAST_THREAT_FEED_STATUS_EPOCH = "last_threat_feed_status_epoch";
    
    /**
     * Default outcome string for fresh installs: matches `ThreatFeedStatus.Outcome.NEVER`
     * so the dashboard renders "Threat database not yet synced" until the first run
     * lands. Kept as a literal string here so this class doesn't have to import the
     * `core` enum.
     */
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String DEFAULT_THREAT_FEED_OUTCOME = "NEVER";
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.data.local.preferences.SecurePreferencesManager.Companion Companion = null;
    
    public SecurePreferencesManager(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    public final boolean getRealTimeMonitoringEnabled() {
        return false;
    }
    
    public final void setRealTimeMonitoringEnabled(boolean value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.Boolean> getRealTimeMonitoringEnabledFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getNotificationLevel() {
        return null;
    }
    
    public final void setNotificationLevel(@org.jetbrains.annotations.NotNull
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.String> getNotificationLevelFlow() {
        return null;
    }
    
    public final boolean getDeepScanModeEnabled() {
        return false;
    }
    
    public final void setDeepScanModeEnabled(boolean value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.Boolean> getDeepScanModeEnabledFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getThemeMode() {
        return null;
    }
    
    public final void setThemeMode(@org.jetbrains.annotations.NotNull
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.String> getThemeModeFlow() {
        return null;
    }
    
    public final boolean getHasCompletedInitialScan() {
        return false;
    }
    
    public final void setHasCompletedInitialScan(boolean value) {
    }
    
    public final boolean isAuthenticated() {
        return false;
    }
    
    public final void setAuthenticated(boolean value) {
    }
    
    public final boolean getRequireOnboardingAfterRegistration() {
        return false;
    }
    
    public final void setRequireOnboardingAfterRegistration(boolean value) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getRegisteredEmail() {
        return null;
    }
    
    public final void setRegisteredEmail(@org.jetbrains.annotations.Nullable
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getRegisteredPassword() {
        return null;
    }
    
    public final void setRegisteredPassword(@org.jetbrains.annotations.Nullable
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getRegisteredFullName() {
        return null;
    }
    
    public final void setRegisteredFullName(@org.jetbrains.annotations.Nullable
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getRegisteredPhone() {
        return null;
    }
    
    public final void setRegisteredPhone(@org.jetbrains.annotations.Nullable
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getAuthAccessToken() {
        return null;
    }
    
    public final void setAuthAccessToken(@org.jetbrains.annotations.Nullable
    java.lang.String value) {
    }
    
    /**
     * Clears session token, stored password, and auth flag (keeps email/name for convenience).
     */
    public final void clearAuthSession() {
    }
    
    /**
     * True if the auth session flag has been written before.
     */
    public final boolean hasAuthSessionPreference() {
        return false;
    }
    
    /**
     * One-time migration:
     * users who already used the app before auth was introduced should not be forced through
     * registration/login immediately after upgrade.
     */
    public final void migrateAuthenticationForExistingUsers() {
    }
    
    public final boolean getTermsAndConditionsAccepted() {
        return false;
    }
    
    public final void setTermsAndConditionsAccepted(boolean value) {
    }
    
    /**
     * True if [KEY_TERMS_CONDITIONS] was ever written (used to migrate existing installs).
     */
    public final boolean hasTermsAndConditionsPreference() {
        return false;
    }
    
    /**
     * One-time: users who already completed privacy onboarding before T&C existed are treated as having accepted.
     */
    public final void migrateTermsAcceptanceForExistingUsers() {
    }
    
    public final boolean getPrivacyOnboardingAcknowledged() {
        return false;
    }
    
    public final void setPrivacyOnboardingAcknowledged(boolean value) {
    }
    
    public final boolean getPermissionOnboardingAcknowledged() {
        return false;
    }
    
    public final void setPermissionOnboardingAcknowledged(boolean value) {
    }
    
    public final boolean getCloudVerificationEnabled() {
        return false;
    }
    
    public final void setCloudVerificationEnabled(boolean value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.Boolean> getCloudVerificationEnabledFlow() {
        return null;
    }
    
    public final boolean getScanTelemetryEnabled() {
        return false;
    }
    
    public final void setScanTelemetryEnabled(boolean value) {
    }
    
    public final boolean getPrivacySharingOptOut() {
        return false;
    }
    
    public final void setPrivacySharingOptOut(boolean value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.Boolean> getPrivacySharingOptOutFlow() {
        return null;
    }
    
    public final boolean getScheduleScanEnabled() {
        return false;
    }
    
    public final void setScheduleScanEnabled(boolean value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.Boolean> getScheduleScanEnabledFlow() {
        return null;
    }
    
    public final int getScheduleHour() {
        return 0;
    }
    
    public final void setScheduleHour(int value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.Integer> getScheduleHourFlow() {
        return null;
    }
    
    public final int getScheduleMinute() {
        return 0;
    }
    
    public final void setScheduleMinute(int value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.Integer> getScheduleMinuteFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getScheduleFrequency() {
        return null;
    }
    
    public final void setScheduleFrequency(@org.jetbrains.annotations.NotNull
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.String> getScheduleFrequencyFlow() {
        return null;
    }
    
    public final long getLastThreatFeedSyncMs() {
        return 0L;
    }
    
    public final void setLastThreatFeedSyncMs(long value) {
    }
    
    public final long getLastThreatFeedSuccessMs() {
        return 0L;
    }
    
    public final long getLastThreatFeedAttemptMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getLastThreatFeedOutcomeRaw() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLastThreatFeedFailureReason() {
        return null;
    }
    
    public final int getLastThreatFeedInsertedCount() {
        return 0;
    }
    
    /**
     * Atomic write of all threat-feed status fields. Bumps a single change-marker
     * ([KEY_LAST_THREAT_FEED_STATUS_EPOCH]) so [lastThreatFeedStatusEpochFlow] subscribers
     * see exactly one notification per worker run instead of an inconsistent torn snapshot
     * across five separate `apply()` calls.
     *
     * @param successMs preserve-on-failure: callers should pass the *previous*
     *  [lastThreatFeedSuccessMs] when writing a non-success outcome, so the dashboard can
     *  still display "last refreshed 3 days ago — last attempt failed".
     */
    public final void writeThreatFeedStatus(long successMs, long attemptMs, @org.jetbrains.annotations.NotNull
    java.lang.String outcomeRaw, @org.jetbrains.annotations.Nullable
    java.lang.String failureReason, int insertedCount) {
    }
    
    /**
     * Cold flow that fires once every time [writeThreatFeedStatus] commits a new bundle.
     * The emitted [Long] is the bumped epoch counter (opaque — observers should re-read
     * the status fields, not interpret the value). Used by the [ThreatFeedStatusStore]
     * adapter to drive `observe()`.
     */
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.Long> getLastThreatFeedStatusEpochFlow() {
        return null;
    }
    
    private final <T extends java.lang.Object>kotlinx.coroutines.flow.Flow<T> prefsFlow(java.lang.String key, kotlin.jvm.functions.Function0<? extends T> read) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\'\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\'\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010(\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010)\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010*\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010+\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010,\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006."}, d2 = {"Lcom/safeguard/data/local/preferences/SecurePreferencesManager$Companion;", "", "()V", "DEFAULT_DEEP_SCAN", "", "DEFAULT_REAL_TIME_MONITORING", "DEFAULT_THREAT_FEED_OUTCOME", "", "FREQ_DAILY", "FREQ_WEEKLY", "KEY_AUTH_ACCESS_TOKEN", "KEY_AUTH_IS_AUTHENTICATED", "KEY_AUTH_REGISTERED_EMAIL", "KEY_AUTH_REGISTERED_FULL_NAME", "KEY_AUTH_REGISTERED_PASSWORD", "KEY_AUTH_REGISTERED_PHONE", "KEY_CLOUD_VERIFICATION", "KEY_DEEP_SCAN", "KEY_INITIAL_SCAN", "KEY_LAST_THREAT_FEED_ATTEMPT_MS", "KEY_LAST_THREAT_FEED_FAILURE_REASON", "KEY_LAST_THREAT_FEED_INSERTED_COUNT", "KEY_LAST_THREAT_FEED_OUTCOME", "KEY_LAST_THREAT_FEED_STATUS_EPOCH", "KEY_LAST_THREAT_FEED_SUCCESS_MS", "KEY_LAST_THREAT_FEED_SYNC", "KEY_MONITORING", "KEY_NOTIFICATION_LEVEL", "KEY_PERMISSION_ONBOARDING", "KEY_PRIVACY_ONBOARDING", "KEY_PRIVACY_SHARING_OPT_OUT", "KEY_REQUIRE_ONBOARDING_AFTER_REGISTRATION", "KEY_SCAN_TELEMETRY", "KEY_SCHEDULE_ENABLED", "KEY_SCHEDULE_FREQUENCY", "KEY_SCHEDULE_HOUR", "KEY_SCHEDULE_MINUTE", "KEY_TERMS_CONDITIONS", "KEY_THEME_MODE", "LEVEL_NORMAL", "LEVEL_SILENT", "LEVEL_VERBOSE", "TAG", "THEME_DARK", "THEME_LIGHT", "THEME_SYSTEM", "data_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}