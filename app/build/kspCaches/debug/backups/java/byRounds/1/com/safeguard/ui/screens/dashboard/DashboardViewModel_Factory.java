package com.safeguard.ui.screens.dashboard;

import android.content.Context;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.core.domain.repository.ThreatFeedRepository;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
import com.safeguard.core.domain.usecase.ScanAPKUseCase;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.manager.DeviceScanManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<ScanRepository> scanRepositoryProvider;

  private final Provider<QuarantineRepository> quarantineRepositoryProvider;

  private final Provider<SecurePreferencesManager> preferencesProvider;

  private final Provider<ScanAPKUseCase> scanAPKUseCaseProvider;

  private final Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider;

  private final Provider<DeviceScanManager> deviceScanManagerProvider;

  private final Provider<ThreatFeedRepository> threatFeedRepositoryProvider;

  public DashboardViewModel_Factory(Provider<Context> contextProvider,
      Provider<ScanRepository> scanRepositoryProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider,
      Provider<SecurePreferencesManager> preferencesProvider,
      Provider<ScanAPKUseCase> scanAPKUseCaseProvider,
      Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider,
      Provider<DeviceScanManager> deviceScanManagerProvider,
      Provider<ThreatFeedRepository> threatFeedRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.scanRepositoryProvider = scanRepositoryProvider;
    this.quarantineRepositoryProvider = quarantineRepositoryProvider;
    this.preferencesProvider = preferencesProvider;
    this.scanAPKUseCaseProvider = scanAPKUseCaseProvider;
    this.quarantineAPKUseCaseProvider = quarantineAPKUseCaseProvider;
    this.deviceScanManagerProvider = deviceScanManagerProvider;
    this.threatFeedRepositoryProvider = threatFeedRepositoryProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(contextProvider.get(), scanRepositoryProvider.get(), quarantineRepositoryProvider.get(), preferencesProvider.get(), scanAPKUseCaseProvider.get(), quarantineAPKUseCaseProvider.get(), deviceScanManagerProvider.get(), threatFeedRepositoryProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<Context> contextProvider,
      Provider<ScanRepository> scanRepositoryProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider,
      Provider<SecurePreferencesManager> preferencesProvider,
      Provider<ScanAPKUseCase> scanAPKUseCaseProvider,
      Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider,
      Provider<DeviceScanManager> deviceScanManagerProvider,
      Provider<ThreatFeedRepository> threatFeedRepositoryProvider) {
    return new DashboardViewModel_Factory(contextProvider, scanRepositoryProvider, quarantineRepositoryProvider, preferencesProvider, scanAPKUseCaseProvider, quarantineAPKUseCaseProvider, deviceScanManagerProvider, threatFeedRepositoryProvider);
  }

  public static DashboardViewModel newInstance(Context context, ScanRepository scanRepository,
      QuarantineRepository quarantineRepository, SecurePreferencesManager preferences,
      ScanAPKUseCase scanAPKUseCase, QuarantineAPKUseCase quarantineAPKUseCase,
      DeviceScanManager deviceScanManager, ThreatFeedRepository threatFeedRepository) {
    return new DashboardViewModel(context, scanRepository, quarantineRepository, preferences, scanAPKUseCase, quarantineAPKUseCase, deviceScanManager, threatFeedRepository);
  }
}
