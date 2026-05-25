package com.safeguard.data.di

import com.safeguard.core.domain.repository.CloudVerificationRepository
import com.safeguard.core.domain.repository.InstalledAppsRepository
import com.safeguard.core.domain.repository.QuarantineRepository
import com.safeguard.core.domain.repository.ScanFeedbackRepository
import com.safeguard.core.domain.repository.ScanRepository
import com.safeguard.core.domain.repository.ThreatDatabaseRepository
import com.safeguard.core.domain.repository.ThreatFeedRepository
import com.safeguard.data.repository.CloudVerificationRepositoryImpl
import com.safeguard.data.repository.InstalledAppsRepositoryImpl
import com.safeguard.data.repository.QuarantineRepositoryImpl
import com.safeguard.data.repository.ScanFeedbackRepositoryImpl
import com.safeguard.data.repository.ScanRepositoryImpl
import com.safeguard.data.repository.ThreatDatabaseRepositoryImpl
import com.safeguard.data.repository.ThreatFeedRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindScanRepository(impl: ScanRepositoryImpl): ScanRepository

    @Binds
    abstract fun bindThreatDatabaseRepository(impl: ThreatDatabaseRepositoryImpl): ThreatDatabaseRepository

    @Binds
    abstract fun bindThreatFeedRepository(impl: ThreatFeedRepositoryImpl): ThreatFeedRepository

    @Binds
    abstract fun bindCloudVerificationRepository(impl: CloudVerificationRepositoryImpl): CloudVerificationRepository

    @Binds
    abstract fun bindQuarantineRepository(impl: QuarantineRepositoryImpl): QuarantineRepository

    @Binds
    abstract fun bindInstalledAppsRepository(impl: InstalledAppsRepositoryImpl): InstalledAppsRepository

    @Binds
    abstract fun bindScanFeedbackRepository(impl: ScanFeedbackRepositoryImpl): ScanFeedbackRepository
}
