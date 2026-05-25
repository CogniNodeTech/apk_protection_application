package com.safeguard.manager;

import android.content.Context;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
import com.safeguard.core.domain.usecase.ScanAPKUseCase;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DeviceScanManager_Factory implements Factory<DeviceScanManager> {
  private final Provider<Context> contextProvider;

  private final Provider<ScanAPKUseCase> scanAPKUseCaseProvider;

  private final Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider;

  private final Provider<QuarantineRepository> quarantineRepositoryProvider;

  private final Provider<SecurePreferencesManager> preferencesProvider;

  public DeviceScanManager_Factory(Provider<Context> contextProvider,
      Provider<ScanAPKUseCase> scanAPKUseCaseProvider,
      Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider,
      Provider<SecurePreferencesManager> preferencesProvider) {
    this.contextProvider = contextProvider;
    this.scanAPKUseCaseProvider = scanAPKUseCaseProvider;
    this.quarantineAPKUseCaseProvider = quarantineAPKUseCaseProvider;
    this.quarantineRepositoryProvider = quarantineRepositoryProvider;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public DeviceScanManager get() {
    return newInstance(contextProvider.get(), scanAPKUseCaseProvider.get(), quarantineAPKUseCaseProvider.get(), quarantineRepositoryProvider.get(), preferencesProvider.get());
  }

  public static DeviceScanManager_Factory create(Provider<Context> contextProvider,
      Provider<ScanAPKUseCase> scanAPKUseCaseProvider,
      Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider,
      Provider<SecurePreferencesManager> preferencesProvider) {
    return new DeviceScanManager_Factory(contextProvider, scanAPKUseCaseProvider, quarantineAPKUseCaseProvider, quarantineRepositoryProvider, preferencesProvider);
  }

  public static DeviceScanManager newInstance(Context context, ScanAPKUseCase scanAPKUseCase,
      QuarantineAPKUseCase quarantineAPKUseCase, QuarantineRepository quarantineRepository,
      SecurePreferencesManager preferences) {
    return new DeviceScanManager(context, scanAPKUseCase, quarantineAPKUseCase, quarantineRepository, preferences);
  }
}
