package com.safeguard.ui.screens.auth;

import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.data.repository.AuthRepository;
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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<SecurePreferencesManager> prefsProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public AuthViewModel_Factory(Provider<SecurePreferencesManager> prefsProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.prefsProvider = prefsProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(prefsProvider.get(), authRepositoryProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<SecurePreferencesManager> prefsProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new AuthViewModel_Factory(prefsProvider, authRepositoryProvider);
  }

  public static AuthViewModel newInstance(SecurePreferencesManager prefs,
      AuthRepository authRepository) {
    return new AuthViewModel(prefs, authRepository);
  }
}
