package com.safeguard.ui.screens.protectionstatus;

import androidx.lifecycle.SavedStateHandle;
import com.safeguard.core.domain.repository.InstalledAppsRepository;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.repository.ScanRepository;
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
public final class ProtectionStatusDetailViewModel_Factory implements Factory<ProtectionStatusDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ScanRepository> scanRepositoryProvider;

  private final Provider<QuarantineRepository> quarantineRepositoryProvider;

  private final Provider<InstalledAppsRepository> installedAppsRepositoryProvider;

  public ProtectionStatusDetailViewModel_Factory(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ScanRepository> scanRepositoryProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider,
      Provider<InstalledAppsRepository> installedAppsRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.scanRepositoryProvider = scanRepositoryProvider;
    this.quarantineRepositoryProvider = quarantineRepositoryProvider;
    this.installedAppsRepositoryProvider = installedAppsRepositoryProvider;
  }

  @Override
  public ProtectionStatusDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), scanRepositoryProvider.get(), quarantineRepositoryProvider.get(), installedAppsRepositoryProvider.get());
  }

  public static ProtectionStatusDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ScanRepository> scanRepositoryProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider,
      Provider<InstalledAppsRepository> installedAppsRepositoryProvider) {
    return new ProtectionStatusDetailViewModel_Factory(savedStateHandleProvider, scanRepositoryProvider, quarantineRepositoryProvider, installedAppsRepositoryProvider);
  }

  public static ProtectionStatusDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      ScanRepository scanRepository, QuarantineRepository quarantineRepository,
      InstalledAppsRepository installedAppsRepository) {
    return new ProtectionStatusDetailViewModel(savedStateHandle, scanRepository, quarantineRepository, installedAppsRepository);
  }
}
