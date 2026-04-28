package com.safeguard.core.domain.telemetry

import com.safeguard.core.domain.model.Verdict

/**
 * Privacy-safe telemetry for scan verdicts (no PII).
 * Implement to send verdict/risk band to backend for tuning; default is no-op.
 */
interface ScanTelemetry {
    fun onScanComplete(verdict: Verdict, riskScore: Int)
}
