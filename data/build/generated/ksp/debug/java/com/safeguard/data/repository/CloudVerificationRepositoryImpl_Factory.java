package com.safeguard.data.repository;

import com.safeguard.data.remote.api.ThreatIntelligenceApi;
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
public final class CloudVerificationRepositoryImpl_Factory implements Factory<CloudVerificationRepositoryImpl> {
  private final Provider<ThreatIntelligenceApi> apiProvider;

  public CloudVerificationRepositoryImpl_Factory(Provider<ThreatIntelligenceApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public CloudVerificationRepositoryImpl get() {
    return newInstance(apiProvider.get());
  }

  public static CloudVerificationRepositoryImpl_Factory create(
      Provider<ThreatIntelligenceApi> apiProvider) {
    return new CloudVerificationRepositoryImpl_Factory(apiProvider);
  }

  public static CloudVerificationRepositoryImpl newInstance(ThreatIntelligenceApi api) {
    return new CloudVerificationRepositoryImpl(api);
  }
}
