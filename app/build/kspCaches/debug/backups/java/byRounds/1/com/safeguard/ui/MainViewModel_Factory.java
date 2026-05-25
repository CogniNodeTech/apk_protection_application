package com.safeguard.ui;

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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<SecurePreferencesManager> prefsProvider;

  public MainViewModel_Factory(Provider<SecurePreferencesManager> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(prefsProvider.get());
  }

  public static MainViewModel_Factory create(Provider<SecurePreferencesManager> prefsProvider) {
    return new MainViewModel_Factory(prefsProvider);
  }

  public static MainViewModel newInstance(SecurePreferencesManager prefs) {
    return new MainViewModel(prefs);
  }
}
