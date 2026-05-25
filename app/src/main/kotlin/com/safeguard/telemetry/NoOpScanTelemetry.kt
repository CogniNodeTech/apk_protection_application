package com.safeguard.telemetry

import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.telemetry.ScanTelemetry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpScanTelemetry @Inject constructor() : ScanTelemetry {
    override fun onScanComplete(verdict: Verdict, riskScore: Int) {}
}
