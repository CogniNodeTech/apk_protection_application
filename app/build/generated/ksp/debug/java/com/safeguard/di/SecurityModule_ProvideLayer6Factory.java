package com.safeguard.di;

import android.content.Context;
import com.safeguard.core.domain.integrity.PlayIntegrityChecker;
import com.safeguard.core.domain.repository.CloudVerificationRepository;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.security.layers.layer6.CloudVerifier;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class SecurityModule_ProvideLayer6Factory implements Factory<CloudVerifier> {
  private final Provider<CloudVerificationRepository> cloudRepoProvider;

  private final Provider<SecurePreferencesManager> preferencesProvider;

  private final Provider<PlayIntegrityChecker> playIntegrityCheckerProvider;

  private final Provider<Context> contextProvider;

  public SecurityModule_ProvideLayer6Factory(
      Provider<CloudVerificationRepository> cloudRepoProvider,
      Provider<SecurePreferencesManager> preferencesProvider,
      Provider<PlayIntegrityChecker> playIntegrityCheckerProvider,
      Provider<Context> contextProvider) {
    this.cloudRepoProvider = cloudRepoProvider;
    this.preferencesProvider = preferencesProvider;
    this.playIntegrityCheckerProvider = playIntegrityCheckerProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public CloudVerifier get() {
    return provideLayer6(cloudRepoProvider.get(), preferencesProvider.get(), playIntegrityCheckerProvider.get(), contextProvider.get());
  }

  public static SecurityModule_ProvideLayer6Factory create(
      Provider<CloudVerificationRepository> cloudRepoProvider,
      Provider<SecurePreferencesManager> preferencesProvider,
      Provider<PlayIntegrityChecker> playIntegrityCheckerProvider,
      Provider<Context> contextProvider) {
    return new SecurityModule_ProvideLayer6Factory(cloudRepoProvider, preferencesProvider, playIntegrityCheckerProvider, contextProvider);
  }

  public static CloudVerifier provideLayer6(CloudVerificationRepository cloudRepo,
      SecurePreferencesManager preferences, PlayIntegrityChecker playIntegrityChecker,
      Context context) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideLayer6(cloudRepo, preferences, playIntegrityChecker, context));
  }
}
