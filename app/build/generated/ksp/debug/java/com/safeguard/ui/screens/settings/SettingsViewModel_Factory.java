package com.safeguard.ui.screens.settings;

import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.repository.ScanFeedbackRepository;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.squareup.moshi.Moshi;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SecurePreferencesManager> preferencesProvider;

  private final Provider<ScanRepository> scanRepositoryProvider;

  private final Provider<QuarantineRepository> quarantineRepositoryProvider;

  private final Provider<ScanFeedbackRepository> scanFeedbackRepositoryProvider;

  private final Provider<Moshi> moshiProvider;

  public SettingsViewModel_Factory(Provider<SecurePreferencesManager> preferencesProvider,
      Provider<ScanRepository> scanRepositoryProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider,
      Provider<ScanFeedbackRepository> scanFeedbackRepositoryProvider,
      Provider<Moshi> moshiProvider) {
    this.preferencesProvider = preferencesProvider;
    this.scanRepositoryProvider = scanRepositoryProvider;
    this.quarantineRepositoryProvider = quarantineRepositoryProvider;
    this.scanFeedbackRepositoryProvider = scanFeedbackRepositoryProvider;
    this.moshiProvider = moshiProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(preferencesProvider.get(), scanRepositoryProvider.get(), quarantineRepositoryProvider.get(), scanFeedbackRepositoryProvider.get(), moshiProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SecurePreferencesManager> preferencesProvider,
      Provider<ScanRepository> scanRepositoryProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider,
      Provider<ScanFeedbackRepository> scanFeedbackRepositoryProvider,
      Provider<Moshi> moshiProvider) {
    return new SettingsViewModel_Factory(preferencesProvider, scanRepositoryProvider, quarantineRepositoryProvider, scanFeedbackRepositoryProvider, moshiProvider);
  }

  public static SettingsViewModel newInstance(SecurePreferencesManager preferences,
      ScanRepository scanRepository, QuarantineRepository quarantineRepository,
      ScanFeedbackRepository scanFeedbackRepository, Moshi moshi) {
    return new SettingsViewModel(preferences, scanRepository, quarantineRepository, scanFeedbackRepository, moshi);
  }
}
