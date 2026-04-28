package com.safeguard.ui.theme;

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
public final class ThemeViewModel_Factory implements Factory<ThemeViewModel> {
  private final Provider<SecurePreferencesManager> preferencesProvider;

  public ThemeViewModel_Factory(Provider<SecurePreferencesManager> preferencesProvider) {
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public ThemeViewModel get() {
    return newInstance(preferencesProvider.get());
  }

  public static ThemeViewModel_Factory create(
      Provider<SecurePreferencesManager> preferencesProvider) {
    return new ThemeViewModel_Factory(preferencesProvider);
  }

  public static ThemeViewModel newInstance(SecurePreferencesManager preferences) {
    return new ThemeViewModel(preferences);
  }
}
