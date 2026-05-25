package com.safeguard.telemetry

import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.telemetry.ScanTelemetry
import com.safeguard.data.local.preferences.SecurePreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the concrete telemetry implementation and respects user privacy toggles
 * ([SecurePreferencesManager.scanTelemetryEnabled], [SecurePreferencesManager.privacySharingOptOut]).
 */
@Singleton
class PrivacyAwareScanTelemetry @Inject constructor(
    private val preferences: SecurePreferencesManager,
    private val delegate: NoOpScanTelemetry
) : ScanTelemetry {

    override fun onScanComplete(verdict: Verdict, riskScore: Int) {
        if (!preferences.scanTelemetryEnabled || preferences.privacySharingOptOut) return
        delegate.onScanComplete(verdict, riskScore)
    }
}
