package com.safeguard.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

class SecurePreferencesManager(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "safeguard_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e(TAG, "Secure prefs unavailable; refusing insecure fallback.", e)
        throw IllegalStateException("Secure storage initialization failed", e)
    }

    /** Real-time APK monitoring. Default ON for new installs. */
    var realTimeMonitoringEnabled: Boolean
        get() = prefs.getBoolean(KEY_MONITORING, DEFAULT_REAL_TIME_MONITORING)
        set(value) { prefs.edit().putBoolean(KEY_MONITORING, value).apply() }

    val realTimeMonitoringEnabledFlow: Flow<Boolean> =
        prefsFlow(KEY_MONITORING) { realTimeMonitoringEnabled }

    var notificationLevel: String
        get() = prefs.getString(KEY_NOTIFICATION_LEVEL, LEVEL_NORMAL) ?: LEVEL_NORMAL
        set(value) { prefs.edit().putString(KEY_NOTIFICATION_LEVEL, value).apply() }

    val notificationLevelFlow: Flow<String> =
        prefsFlow(KEY_NOTIFICATION_LEVEL) { notificationLevel }

    /** Deeper forensic pipeline; default ON (toggle can turn off for diagnostics only). */
    var deepScanModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DEEP_SCAN, DEFAULT_DEEP_SCAN)
        set(value) { prefs.edit().putBoolean(KEY_DEEP_SCAN, value).apply() }

    val deepScanModeEnabledFlow: Flow<Boolean> =
        prefsFlow(KEY_DEEP_SCAN) { deepScanModeEnabled }

    /** "dark" | "light" | "system". Default "system". */
    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
        set(value) { prefs.edit().putString(KEY_THEME_MODE, value).apply() }

    val themeModeFlow: Flow<String> =
        prefsFlow(KEY_THEME_MODE) { themeMode }

    /** Initial full device scan complete */
    var hasCompletedInitialScan: Boolean
        get() = prefs.getBoolean(KEY_INITIAL_SCAN, false)
        set(value) { prefs.edit().putBoolean(KEY_INITIAL_SCAN, value).apply() }

    /** Session/auth gate for app access. */
    var isAuthenticated: Boolean
        get() = prefs.getBoolean(KEY_AUTH_IS_AUTHENTICATED, false)
        set(value) { prefs.edit().putBoolean(KEY_AUTH_IS_AUTHENTICATED, value).apply() }

    /**
     * Show terms/privacy onboarding only after a fresh registration.
     * Existing users logging in should not be forced through onboarding again.
     */
    var requireOnboardingAfterRegistration: Boolean
        get() = prefs.getBoolean(KEY_REQUIRE_ONBOARDING_AFTER_REGISTRATION, false)
        set(value) { prefs.edit().putBoolean(KEY_REQUIRE_ONBOARDING_AFTER_REGISTRATION, value).apply() }

    var registeredEmail: String?
        get() = prefs.getString(KEY_AUTH_REGISTERED_EMAIL, null)
        set(value) { prefs.edit().putString(KEY_AUTH_REGISTERED_EMAIL, value).apply() }

    var registeredPassword: String?
        get() = prefs.getString(KEY_AUTH_REGISTERED_PASSWORD, null)
        set(value) { prefs.edit().putString(KEY_AUTH_REGISTERED_PASSWORD, value).apply() }

    var registeredFullName: String?
        get() = prefs.getString(KEY_AUTH_REGISTERED_FULL_NAME, null)
        set(value) { prefs.edit().putString(KEY_AUTH_REGISTERED_FULL_NAME, value).apply() }

    /** Phone from last successful registration / profile (optional). */
    var registeredPhone: String?
        get() = prefs.getString(KEY_AUTH_REGISTERED_PHONE, null)
        set(value) {
            if (value == null) prefs.edit().remove(KEY_AUTH_REGISTERED_PHONE).apply()
            else prefs.edit().putString(KEY_AUTH_REGISTERED_PHONE, value).apply()
        }

    /** Bearer token from POST /auth/login | /auth/register (not persisted across logout). */
    var authAccessToken: String?
        get() = prefs.getString(KEY_AUTH_ACCESS_TOKEN, null)
        set(value) {
            if (value == null) prefs.edit().remove(KEY_AUTH_ACCESS_TOKEN).apply()
            else prefs.edit().putString(KEY_AUTH_ACCESS_TOKEN, value).apply()
        }

    /** Clears session token, stored password, and auth flag (keeps email/name for convenience). */
    fun clearAuthSession() {
        prefs.edit()
            .remove(KEY_AUTH_ACCESS_TOKEN)
            .remove(KEY_AUTH_REGISTERED_PASSWORD)
            .putBoolean(KEY_AUTH_IS_AUTHENTICATED, false)
            .apply()
    }

    /** True if the auth session flag has been written before. */
    fun hasAuthSessionPreference(): Boolean = prefs.contains(KEY_AUTH_IS_AUTHENTICATED)

    /**
     * One-time migration:
     * users who already used the app before auth was introduced should not be forced through
     * registration/login immediately after upgrade.
     */
    fun migrateAuthenticationForExistingUsers() {
        if (!hasAuthSessionPreference() && (privacyOnboardingAcknowledged || hasCompletedInitialScan)) {
            prefs.edit().putBoolean(KEY_AUTH_IS_AUTHENTICATED, true).apply()
        }
    }

    /** First-run terms & conditions acceptance. Required before privacy onboarding on new installs. */
    var termsAndConditionsAccepted: Boolean
        get() = prefs.getBoolean(KEY_TERMS_CONDITIONS, false)
        set(value) { prefs.edit().putBoolean(KEY_TERMS_CONDITIONS, value).apply() }

    /** True if [KEY_TERMS_CONDITIONS] was ever written (used to migrate existing installs). */
    fun hasTermsAndConditionsPreference(): Boolean = prefs.contains(KEY_TERMS_CONDITIONS)

    /**
     * One-time: users who already completed privacy onboarding before T&C existed are treated as having accepted.
     */
    fun migrateTermsAcceptanceForExistingUsers() {
        if (privacyOnboardingAcknowledged && !hasTermsAndConditionsPreference()) {
            prefs.edit().putBoolean(KEY_TERMS_CONDITIONS, true).apply()
        }
    }

    /** First-run privacy / data practices disclosure acknowledged. Until true, cloud verification is skipped. */
    var privacyOnboardingAcknowledged: Boolean
        get() = prefs.getBoolean(KEY_PRIVACY_ONBOARDING, false)
        set(value) { prefs.edit().putBoolean(KEY_PRIVACY_ONBOARDING, value).apply() }

    /**
     * First-run "all files access" permission disclosure acknowledged. Shown once per device on
     * Android 11+ to explain why MANAGE_EXTERNAL_STORAGE unlocks deep scans of WhatsApp/Telegram
     * etc. folders. Decoupled from [privacyOnboardingAcknowledged] so existing users also see it
     * once after upgrading to a build that ships the permission flow.
     */
    var permissionOnboardingAcknowledged: Boolean
        get() = prefs.getBoolean(KEY_PERMISSION_ONBOARDING, false)
        set(value) { prefs.edit().putBoolean(KEY_PERMISSION_ONBOARDING, value).apply() }

    /** When false, Layer 6 does not call the cloud API (local layers still run). Default true after onboarding. */
    var cloudVerificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_CLOUD_VERIFICATION, true)
        set(value) { prefs.edit().putBoolean(KEY_CLOUD_VERIFICATION, value).apply() }

    val cloudVerificationEnabledFlow: Flow<Boolean> =
        prefsFlow(KEY_CLOUD_VERIFICATION) { cloudVerificationEnabled }

    /** Privacy-safe scan telemetry (see TELEMETRY_CONTRACT). Default on. */
    var scanTelemetryEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCAN_TELEMETRY, true)
        set(value) { prefs.edit().putBoolean(KEY_SCAN_TELEMETRY, value).apply() }

    /**
     * Phase 3.2 — opt-in privacy-preserving scan feedback channel. **Default OFF.**
     *
     * When `true`, the scan pipeline enqueues a [com.safeguard.core.domain.feedback.ScanFeedbackEvent]
     * after every completed scan; the [com.safeguard.worker.FeedbackUploadWorker] periodically
     * drains the queue under network/battery constraints. Toggling this off does NOT
     * implicitly purge the queue — that's exposed as a separate user action so power users
     * can opt out without losing the events they already explicitly chose to share.
     *
     * Distinct from [scanTelemetryEnabled] (a pure verdict counter, on by default) and
     * from [privacySharingOptOut] (US-state legal kill switch). All three must agree before
     * an event leaves the device.
     */
    var scanFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCAN_FEEDBACK, false)
        set(value) { prefs.edit().putBoolean(KEY_SCAN_FEEDBACK, value).apply() }

    val scanFeedbackEnabledFlow: Flow<Boolean> =
        prefsFlow(KEY_SCAN_FEEDBACK) { scanFeedbackEnabled }

    /**
     * US state privacy: opt out of sale / sharing of personal information for analytics/telemetry.
     * When true, scan telemetry is not sent (even if [scanTelemetryEnabled] is true).
     */
    var privacySharingOptOut: Boolean
        get() = prefs.getBoolean(KEY_PRIVACY_SHARING_OPT_OUT, false)
        set(value) { prefs.edit().putBoolean(KEY_PRIVACY_SHARING_OPT_OUT, value).apply() }

    val privacySharingOptOutFlow: Flow<Boolean> =
        prefsFlow(KEY_PRIVACY_SHARING_OPT_OUT) { privacySharingOptOut }

    // Scheduled scan
    var scheduleScanEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCHEDULE_ENABLED, false)
        set(value) { prefs.edit().putBoolean(KEY_SCHEDULE_ENABLED, value).apply() }
    val scheduleScanEnabledFlow: Flow<Boolean> =
        prefsFlow(KEY_SCHEDULE_ENABLED) { scheduleScanEnabled }

    var scheduleHour: Int
        get() = prefs.getInt(KEY_SCHEDULE_HOUR, 9).coerceIn(0, 23)
        set(value) { prefs.edit().putInt(KEY_SCHEDULE_HOUR, value.coerceIn(0, 23)).apply() }
    val scheduleHourFlow: Flow<Int> =
        prefsFlow(KEY_SCHEDULE_HOUR) { scheduleHour }

    var scheduleMinute: Int
        get() = prefs.getInt(KEY_SCHEDULE_MINUTE, 0).coerceIn(0, 59)
        set(value) { prefs.edit().putInt(KEY_SCHEDULE_MINUTE, value.coerceIn(0, 59)).apply() }
    val scheduleMinuteFlow: Flow<Int> =
        prefsFlow(KEY_SCHEDULE_MINUTE) { scheduleMinute }

    /** "daily" | "weekly" */
    var scheduleFrequency: String
        get() = prefs.getString(KEY_SCHEDULE_FREQUENCY, FREQ_DAILY) ?: FREQ_DAILY
        set(value) { prefs.edit().putString(KEY_SCHEDULE_FREQUENCY, value).apply() }
    val scheduleFrequencyFlow: Flow<String> =
        prefsFlow(KEY_SCHEDULE_FREQUENCY) { scheduleFrequency }

    /**
     * Cursor for the threat-feed sync worker. Stores the `next_cursor_ms` returned by the
     * most recent successful `GET /v1/threat-feed` call (epoch ms). `0` means "never synced
     * yet" and tells the repository to omit the `since` query param so the server hands
     * back the most recent batch of malware signatures.
     */
    var lastThreatFeedSyncMs: Long
        get() = prefs.getLong(KEY_LAST_THREAT_FEED_SYNC, 0L)
        set(value) { prefs.edit().putLong(KEY_LAST_THREAT_FEED_SYNC, value).apply() }

    /**
     * Wall-clock timestamp (device epoch-ms) of the most recent **successful** threat-feed
     * sync. Distinct from [lastThreatFeedSyncMs] — that's the server-side cursor (a
     * monotonic key into the server's row stream), this is the user-visible "you last
     * refreshed at" displayed on the dashboard. `0` ⇒ device has never seen a successful
     * sync (fresh install, or every attempt to date has failed/been skipped).
     */
    val lastThreatFeedSuccessMs: Long
        get() = prefs.getLong(KEY_LAST_THREAT_FEED_SUCCESS_MS, 0L)

    /**
     * Wall-clock timestamp of the most recent worker run, regardless of outcome. Used by
     * the dashboard to distinguish "we tried recently, network died" from "we haven't even
     * tried in a week" — both leave [lastThreatFeedSuccessMs] stale, but mean different
     * things to the user. `0` ⇒ worker has never run (or never written a status row).
     */
    val lastThreatFeedAttemptMs: Long
        get() = prefs.getLong(KEY_LAST_THREAT_FEED_ATTEMPT_MS, 0L)

    /**
     * Persisted name of the [ThreatFeedStatus.Outcome] enum value from the last run.
     * Returned as a raw string so [SecurePreferencesManager] can stay free of `core` type
     * imports; the [ThreatFeedStatusStore] adapter in `DatabaseModule` handles the enum
     * conversion. Defaults to [DEFAULT_THREAT_FEED_OUTCOME] (== "NEVER") on fresh installs.
     */
    val lastThreatFeedOutcomeRaw: String
        get() = prefs.getString(KEY_LAST_THREAT_FEED_OUTCOME, DEFAULT_THREAT_FEED_OUTCOME)
            ?: DEFAULT_THREAT_FEED_OUTCOME

    /**
     * Last failure reason from the worker, or `null` if the most recent run did not fail.
     * Cleared (set to `null`) whenever a non-failure outcome is written, so the dashboard
     * never displays a stale error message after recovery.
     */
    val lastThreatFeedFailureReason: String?
        get() = prefs.getString(KEY_LAST_THREAT_FEED_FAILURE_REASON, null)

    /**
     * Number of rows upserted in the most recent **successful** sync. Reset to `0`
     * alongside any non-success outcome so the dashboard's "added N signatures" tile
     * doesn't lie about the latest attempt.
     */
    val lastThreatFeedInsertedCount: Int
        get() = prefs.getInt(KEY_LAST_THREAT_FEED_INSERTED_COUNT, 0)

    /**
     * Atomic write of all threat-feed status fields. Bumps a single change-marker
     * ([KEY_LAST_THREAT_FEED_STATUS_EPOCH]) so [lastThreatFeedStatusEpochFlow] subscribers
     * see exactly one notification per worker run instead of an inconsistent torn snapshot
     * across five separate `apply()` calls.
     *
     * @param successMs preserve-on-failure: callers should pass the *previous*
     *   [lastThreatFeedSuccessMs] when writing a non-success outcome, so the dashboard can
     *   still display "last refreshed 3 days ago — last attempt failed".
     */
    fun writeThreatFeedStatus(
        successMs: Long,
        attemptMs: Long,
        outcomeRaw: String,
        failureReason: String?,
        insertedCount: Int
    ) {
        val editor = prefs.edit()
            .putLong(KEY_LAST_THREAT_FEED_SUCCESS_MS, successMs)
            .putLong(KEY_LAST_THREAT_FEED_ATTEMPT_MS, attemptMs)
            .putString(KEY_LAST_THREAT_FEED_OUTCOME, outcomeRaw)
            .putInt(KEY_LAST_THREAT_FEED_INSERTED_COUNT, insertedCount)
            .putLong(
                KEY_LAST_THREAT_FEED_STATUS_EPOCH,
                prefs.getLong(KEY_LAST_THREAT_FEED_STATUS_EPOCH, 0L) + 1L
            )
        if (failureReason == null) {
            editor.remove(KEY_LAST_THREAT_FEED_FAILURE_REASON)
        } else {
            editor.putString(KEY_LAST_THREAT_FEED_FAILURE_REASON, failureReason)
        }
        editor.apply()
    }

    /**
     * Cold flow that fires once every time [writeThreatFeedStatus] commits a new bundle.
     * The emitted [Long] is the bumped epoch counter (opaque — observers should re-read
     * the status fields, not interpret the value). Used by the [ThreatFeedStatusStore]
     * adapter to drive `observe()`.
     */
    val lastThreatFeedStatusEpochFlow: Flow<Long> =
        prefsFlow(KEY_LAST_THREAT_FEED_STATUS_EPOCH) {
            prefs.getLong(KEY_LAST_THREAT_FEED_STATUS_EPOCH, 0L)
        }

    private inline fun <T> prefsFlow(
        key: String,
        crossinline read: () -> T
    ): Flow<T> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) trySend(read())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(read())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.conflate()

    companion object {
        private const val TAG = "SecurePreferences"
        private const val KEY_MONITORING = "real_time_monitoring"
        /** Real-time monitoring is ON by default for new users. */
        private const val DEFAULT_REAL_TIME_MONITORING = true
        private const val KEY_NOTIFICATION_LEVEL = "notification_level"
        private const val KEY_DEEP_SCAN = "deep_scan_mode"
        private const val DEFAULT_DEEP_SCAN = true
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SCHEDULE_ENABLED = "schedule_scan_enabled"
        private const val KEY_SCHEDULE_HOUR = "schedule_hour"
        private const val KEY_SCHEDULE_MINUTE = "schedule_minute"
        private const val KEY_SCHEDULE_FREQUENCY = "schedule_frequency"
        const val FREQ_DAILY = "daily"
        const val FREQ_WEEKLY = "weekly"
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_SYSTEM = "system"
        const val LEVEL_SILENT = "silent"
        const val LEVEL_NORMAL = "normal"
        const val LEVEL_VERBOSE = "verbose"
        private const val KEY_INITIAL_SCAN = "has_completed_initial_scan"
        private const val KEY_AUTH_IS_AUTHENTICATED = "auth_is_authenticated"
        private const val KEY_AUTH_REGISTERED_EMAIL = "auth_registered_email"
        private const val KEY_AUTH_REGISTERED_PASSWORD = "auth_registered_password"
        private const val KEY_AUTH_REGISTERED_FULL_NAME = "auth_registered_full_name"
        private const val KEY_AUTH_REGISTERED_PHONE = "auth_registered_phone"
        private const val KEY_AUTH_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REQUIRE_ONBOARDING_AFTER_REGISTRATION = "require_onboarding_after_registration"
        private const val KEY_TERMS_CONDITIONS = "terms_and_conditions_accepted"
        private const val KEY_PRIVACY_ONBOARDING = "privacy_onboarding_acknowledged"
        private const val KEY_PERMISSION_ONBOARDING = "permission_onboarding_acknowledged"
        private const val KEY_CLOUD_VERIFICATION = "cloud_verification_enabled"
        private const val KEY_SCAN_TELEMETRY = "scan_telemetry_enabled"
        private const val KEY_SCAN_FEEDBACK = "scan_feedback_enabled"
        private const val KEY_PRIVACY_SHARING_OPT_OUT = "privacy_sharing_opt_out"
        private const val KEY_LAST_THREAT_FEED_SYNC = "last_threat_feed_sync_ms"
        private const val KEY_LAST_THREAT_FEED_SUCCESS_MS = "last_threat_feed_success_ms"
        private const val KEY_LAST_THREAT_FEED_ATTEMPT_MS = "last_threat_feed_attempt_ms"
        private const val KEY_LAST_THREAT_FEED_OUTCOME = "last_threat_feed_outcome"
        private const val KEY_LAST_THREAT_FEED_FAILURE_REASON = "last_threat_feed_failure_reason"
        private const val KEY_LAST_THREAT_FEED_INSERTED_COUNT = "last_threat_feed_inserted_count"
        private const val KEY_LAST_THREAT_FEED_STATUS_EPOCH = "last_threat_feed_status_epoch"

        /**
         * Default outcome string for fresh installs: matches `ThreatFeedStatus.Outcome.NEVER`
         * so the dashboard renders "Threat database not yet synced" until the first run
         * lands. Kept as a literal string here so this class doesn't have to import the
         * `core` enum.
         */
        const val DEFAULT_THREAT_FEED_OUTCOME = "NEVER"
    }
}
