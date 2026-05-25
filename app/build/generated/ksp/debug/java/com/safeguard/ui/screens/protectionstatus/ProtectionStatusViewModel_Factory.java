package com.safeguard.ui.screens.protectionstatus;

import com.safeguard.core.domain.repository.InstalledAppsRepository;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class ProtectionStatusViewModel_Factory implements Factory<ProtectionStatusViewModel> {
  private final Provider<ScanRepository> scanRepositoryProvider;

  private final Provider<QuarantineRepository> quarantineRepositoryProvider;

  private final Provider<InstalledAppsRepository> installedAppsRepositoryProvider;

  private final Provider<SecurePreferencesManager> preferencesProvider;

  public ProtectionStatusViewModel_Factory(Provider<ScanRepository> scanRepositoryProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider,
      Provider<InstalledAppsRepository> installedAppsRepositoryProvider,
      Provider<SecurePreferencesManager> preferencesProvider) {
    this.scanRepositoryProvider = scanRepositoryProvider;
    this.quarantineRepositoryProvider = quarantineRepositoryProvider;
    this.installedAppsRepositoryProvider = installedAppsRepositoryProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public ProtectionStatusViewModel get() {
    return newInstance(scanRepositoryProvider.get(), quarantineRepositoryProvider.get(), installedAppsRepositoryProvider.get(), preferencesProvider.get());
  }

  public static ProtectionStatusViewModel_Factory create(
      Provider<ScanRepository> scanRepositoryProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider,
      Provider<InstalledAppsRepository> installedAppsRepositoryProvider,
      Provider<SecurePreferencesManager> preferencesProvider) {
    return new ProtectionStatusViewModel_Factory(scanRepositoryProvider, quarantineRepositoryProvider, installedAppsRepositoryProvider, preferencesProvider);
  }

  public static ProtectionStatusViewModel newInstance(ScanRepository scanRepository,
      QuarantineRepository quarantineRepository, InstalledAppsRepository installedAppsRepository,
      SecurePreferencesManager preferences) {
    return new ProtectionStatusViewModel(scanRepository, quarantineRepository, installedAppsRepository, preferences);
  }
}
