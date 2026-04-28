package com.safeguard.di;

import com.safeguard.core.domain.crash.CrashReporter;
import com.safeguard.core.domain.telemetry.ScanTelemetry;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
import com.safeguard.core.domain.usecase.ScanAPKUseCase;
import com.safeguard.crash.FileCrashReporter;
import com.safeguard.domain.QuarantineAPKUseCaseImpl;
import com.safeguard.domain.ScanAPKUseCaseImpl;
import com.safeguard.telemetry.PrivacyAwareScanTelemetry;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\tH\'J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\fH\'J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u000fH\'\u00a8\u0006\u0010"}, d2 = {"Lcom/safeguard/di/AppModule;", "", "()V", "bindCrashReporter", "Lcom/safeguard/core/domain/crash/CrashReporter;", "impl", "Lcom/safeguard/crash/FileCrashReporter;", "bindQuarantineAPKUseCase", "Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;", "Lcom/safeguard/domain/QuarantineAPKUseCaseImpl;", "bindScanAPKUseCase", "Lcom/safeguard/core/domain/usecase/ScanAPKUseCase;", "Lcom/safeguard/domain/ScanAPKUseCaseImpl;", "bindScanTelemetry", "Lcom/safeguard/core/domain/telemetry/ScanTelemetry;", "Lcom/safeguard/telemetry/PrivacyAwareScanTelemetry;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class AppModule {
    
    public AppModule() {
        super();
    }
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.core.domain.crash.CrashReporter bindCrashReporter(@org.jetbrains.annotations.NotNull
    com.safeguard.crash.FileCrashReporter impl);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.core.domain.telemetry.ScanTelemetry bindScanTelemetry(@org.jetbrains.annotations.NotNull
    com.safeguard.telemetry.PrivacyAwareScanTelemetry impl);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.core.domain.usecase.ScanAPKUseCase bindScanAPKUseCase(@org.jetbrains.annotations.NotNull
    com.safeguard.domain.ScanAPKUseCaseImpl impl);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.core.domain.usecase.QuarantineAPKUseCase bindQuarantineAPKUseCase(@org.jetbrains.annotations.NotNull
    com.safeguard.domain.QuarantineAPKUseCaseImpl impl);
}