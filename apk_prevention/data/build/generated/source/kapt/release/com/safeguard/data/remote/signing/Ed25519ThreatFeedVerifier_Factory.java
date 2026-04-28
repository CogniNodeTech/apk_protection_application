package com.safeguard.data.remote.signing;

import com.safeguard.core.domain.repository.ThreatFeedSigningConfig;
import com.squareup.moshi.Moshi;
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
public final class Ed25519ThreatFeedVerifier_Factory implements Factory<Ed25519ThreatFeedVerifier> {
  private final Provider<ThreatFeedSigningConfig> signingConfigProvider;

  private final Provider<Moshi> moshiProvider;

  public Ed25519ThreatFeedVerifier_Factory(Provider<ThreatFeedSigningConfig> signingConfigProvider,
      Provider<Moshi> moshiProvider) {
    this.signingConfigProvider = signingConfigProvider;
    this.moshiProvider = moshiProvider;
  }

  @Override
  public Ed25519ThreatFeedVerifier get() {
    return newInstance(signingConfigProvider.get(), moshiProvider.get());
  }

  public static Ed25519ThreatFeedVerifier_Factory create(
      Provider<ThreatFeedSigningConfig> signingConfigProvider, Provider<Moshi> moshiProvider) {
    return new Ed25519ThreatFeedVerifier_Factory(signingConfigProvider, moshiProvider);
  }

  public static Ed25519ThreatFeedVerifier newInstance(ThreatFeedSigningConfig signingConfig,
      Moshi moshi) {
    return new Ed25519ThreatFeedVerifier(signingConfig, moshi);
  }
}
