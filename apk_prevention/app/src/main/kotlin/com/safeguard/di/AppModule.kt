package com.safeguard.di

import com.safeguard.core.domain.crash.CrashReporter
import com.safeguard.core.domain.telemetry.ScanTelemetry
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase
import com.safeguard.core.domain.usecase.ScanAPKUseCase
import com.safeguard.crash.FileCrashReporter
import com.safeguard.domain.QuarantineAPKUseCaseImpl
import com.safeguard.domain.ScanAPKUseCaseImpl
import com.safeguard.telemetry.PrivacyAwareScanTelemetry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindCrashReporter(impl: FileCrashReporter): CrashReporter

    @Binds
    @Singleton
    abstract fun bindScanTelemetry(impl: PrivacyAwareScanTelemetry): ScanTelemetry

    @Binds
    @Singleton
    abstract fun bindScanAPKUseCase(impl: ScanAPKUseCaseImpl): ScanAPKUseCase

    @Binds
    @Singleton
    abstract fun bindQuarantineAPKUseCase(impl: QuarantineAPKUseCaseImpl): QuarantineAPKUseCase
}
