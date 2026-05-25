package com.safeguard.telemetry;

import com.safeguard.data.local.preferences.SecurePreferencesManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class PrivacyAwareScanTelemetry_Factory implements Factory<PrivacyAwareScanTelemetry> {
  private final Provider<SecurePreferencesManager> preferencesProvider;

  private final Provider<NoOpScanTelemetry> delegateProvider;

  public PrivacyAwareScanTelemetry_Factory(Provider<SecurePreferencesManager> preferencesProvider,
      Provider<NoOpScanTelemetry> delegateProvider) {
    this.preferencesProvider = preferencesProvider;
    this.delegateProvider = delegateProvider;
  }

  @Override
  public PrivacyAwareScanTelemetry get() {
    return newInstance(preferencesProvider.get(), delegateProvider.get());
  }

  public static PrivacyAwareScanTelemetry_Factory create(
      Provider<SecurePreferencesManager> preferencesProvider,
      Provider<NoOpScanTelemetry> delegateProvider) {
    return new PrivacyAwareScanTelemetry_Factory(preferencesProvider, delegateProvider);
  }

  public static PrivacyAwareScanTelemetry newInstance(SecurePreferencesManager preferences,
      NoOpScanTelemetry delegate) {
    return new PrivacyAwareScanTelemetry(preferences, delegate);
  }
}
