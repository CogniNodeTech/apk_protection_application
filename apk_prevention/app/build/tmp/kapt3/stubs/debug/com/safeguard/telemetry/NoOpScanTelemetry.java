package com.safeguard.telemetry;

import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.telemetry.ScanTelemetry;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0016\u00a8\u0006\t"}, d2 = {"Lcom/safeguard/telemetry/NoOpScanTelemetry;", "Lcom/safeguard/core/domain/telemetry/ScanTelemetry;", "()V", "onScanComplete", "", "verdict", "Lcom/safeguard/core/domain/model/Verdict;", "riskScore", "", "app_debug"})
public final class NoOpScanTelemetry implements com.safeguard.core.domain.telemetry.ScanTelemetry {
    
    @javax.inject.Inject
    public NoOpScanTelemetry() {
        super();
    }
    
    @java.lang.Override
    public void onScanComplete(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.Verdict verdict, int riskScore) {
    }
}