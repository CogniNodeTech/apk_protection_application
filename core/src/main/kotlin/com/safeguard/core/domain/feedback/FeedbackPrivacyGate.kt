package com.safeguard.core.domain.feedback

/**
 * Read-only view of the three privacy switches that gate the Phase 3.2 scan feedback
 * channel. Extracted as an interface so the repository can stay free of any
 * Android-specific prefs class — and so JVM unit tests can supply a deterministic gate
 * without Robolectric or `EncryptedSharedPreferences` scaffolding.
 *
 * Production binding adapts [com.safeguard.data.local.preferences.SecurePreferencesManager];
 * tests can provide an in-memory implementation. All three flags must agree (feedback
 * opt-in true, telemetry master on, US-state opt-out off) before any feedback row is
 * persisted or uploaded — the [isFeedbackAllowed] convenience folds them into one call.
 */
interface FeedbackPrivacyGate {
    /** Phase 3.2 explicit feedback opt-in. Default false; user must flip on. */
    val isFeedbackOptInEnabled: Boolean

    /** Telemetry master switch (default on). When off, *all* analytics — feedback included — must stop. */
    val isTelemetryMasterEnabled: Boolean

    /** US state-privacy opt-out (default off). When true, no analytics may leave the device regardless of other toggles. */
    val isPrivacySharingOptedOut: Boolean

    /**
     * Conjunction of the three switches in the precise order callers should reason about
     * them (most specific opt-in first, most legally-binding kill switch last). Repository
     * implementations call this at every boundary so a future fourth toggle (e.g. EU-region
     * specific) only needs to be added in one place.
     */
    fun isFeedbackAllowed(): Boolean =
        isFeedbackOptInEnabled && isTelemetryMasterEnabled && !isPrivacySharingOptedOut
}
