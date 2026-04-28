package com.safeguard.telemetry;

import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.telemetry.ScanTelemetry;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Wraps the concrete telemetry implementation and respects user privacy toggles
 * ([SecurePreferencesManager.scanTelemetryEnabled], [SecurePreferencesManager.privacySharingOptOut]).
 */
@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/safeguard/telemetry/PrivacyAwareScanTelemetry;", "Lcom/safeguard/core/domain/telemetry/ScanTelemetry;", "preferences", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "delegate", "Lcom/safeguard/telemetry/NoOpScanTelemetry;", "(Lcom/safeguard/data/local/preferences/SecurePreferencesManager;Lcom/safeguard/telemetry/NoOpScanTelemetry;)V", "onScanComplete", "", "verdict", "Lcom/safeguard/core/domain/model/Verdict;", "riskScore", "", "app_debug"})
public final class PrivacyAwareScanTelemetry implements com.safeguard.core.domain.telemetry.ScanTelemetry {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.preferences.SecurePreferencesManager preferences = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.telemetry.NoOpScanTelemetry delegate = null;
    
    @javax.inject.Inject
    public PrivacyAwareScanTelemetry(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager preferences, @org.jetbrains.annotations.NotNull
    com.safeguard.telemetry.NoOpScanTelemetry delegate) {
        super();
    }
    
    @java.lang.Override
    public void onScanComplete(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.Verdict verdict, int riskScore) {
    }
}