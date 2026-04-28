package com.safeguard.data.di;

import com.safeguard.core.domain.repository.CloudVerificationRepository;
import com.safeguard.core.domain.repository.InstalledAppsRepository;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.repository.ScanFeedbackRepository;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.core.domain.repository.ThreatDatabaseRepository;
import com.safeguard.core.domain.repository.ThreatFeedRepository;
import com.safeguard.data.repository.CloudVerificationRepositoryImpl;
import com.safeguard.data.repository.InstalledAppsRepositoryImpl;
import com.safeguard.data.repository.QuarantineRepositoryImpl;
import com.safeguard.data.repository.ScanFeedbackRepositoryImpl;
import com.safeguard.data.repository.ScanRepositoryImpl;
import com.safeguard.data.repository.ThreatDatabaseRepositoryImpl;
import com.safeguard.data.repository.ThreatFeedRepositoryImpl;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\tH\'J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\fH\'J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u000fH\'J\u0010\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0005\u001a\u00020\u0012H\'J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0005\u001a\u00020\u0015H\'J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0005\u001a\u00020\u0018H\'\u00a8\u0006\u0019"}, d2 = {"Lcom/safeguard/data/di/RepositoryModule;", "", "()V", "bindCloudVerificationRepository", "Lcom/safeguard/core/domain/repository/CloudVerificationRepository;", "impl", "Lcom/safeguard/data/repository/CloudVerificationRepositoryImpl;", "bindInstalledAppsRepository", "Lcom/safeguard/core/domain/repository/InstalledAppsRepository;", "Lcom/safeguard/data/repository/InstalledAppsRepositoryImpl;", "bindQuarantineRepository", "Lcom/safeguard/core/domain/repository/QuarantineRepository;", "Lcom/safeguard/data/repository/QuarantineRepositoryImpl;", "bindScanFeedbackRepository", "Lcom/safeguard/core/domain/repository/ScanFeedbackRepository;", "Lcom/safeguard/data/repository/ScanFeedbackRepositoryImpl;", "bindScanRepository", "Lcom/safeguard/core/domain/repository/ScanRepository;", "Lcom/safeguard/data/repository/ScanRepositoryImpl;", "bindThreatDatabaseRepository", "Lcom/safeguard/core/domain/repository/ThreatDatabaseRepository;", "Lcom/safeguard/data/repository/ThreatDatabaseRepositoryImpl;", "bindThreatFeedRepository", "Lcom/safeguard/core/domain/repository/ThreatFeedRepository;", "Lcom/safeguard/data/repository/ThreatFeedRepositoryImpl;", "data_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class RepositoryModule {
    
    public RepositoryModule() {
        super();
    }
    
    @dagger.Binds
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.core.domain.repository.ScanRepository bindScanRepository(@org.jetbrains.annotations.NotNull
    com.safeguard.data.repository.ScanRepositoryImpl impl);
    
    @dagger.Binds
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.core.domain.repository.ThreatDatabaseRepository bindThreatDatabaseRepository(@org.jetbrains.annotations.NotNull
    com.safeguard.data.repository.ThreatDatabaseRepositoryImpl impl);
    
    @dagger.Binds
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.core.domain.repository.ThreatFeedRepository bindThreatFeedRepository(@org.jetbrains.annotations.NotNull
    com.safeguard.data.repository.ThreatFeedRepositoryImpl impl);
    
    @dagger.Binds
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.core.domain.repository.CloudVerificationRepository bindCloudVerificationRepository(@org.jetbrains.annotations.NotNull
    com.safeguard.data.repository.CloudVerificationRepositoryImpl impl);
    
    @dagger.Binds
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.core.domain.repository.QuarantineRepository bindQuarantineRepository(@org.jetbrains.annotations.NotNull
    com.safeguard.data.repository.QuarantineRepositoryImpl impl);
    
    @dagger.Binds
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.core.domain.repository.InstalledAppsRepository bindInstalledAppsRepository(@org.jetbrains.annotations.NotNull
    com.safeguard.data.repository.InstalledAppsRepositoryImpl impl);
    
    @dagger.Binds
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.core.domain.repository.ScanFeedbackRepository bindScanFeedbackRepository(@org.jetbrains.annotations.NotNull
    com.safeguard.data.repository.ScanFeedbackRepositoryImpl impl);
}