package com.safeguard.di;

import com.safeguard.core.domain.repository.ThreatFeedSigningConfig;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class NetworkConfigModule_ProvideThreatFeedSigningConfigFactory implements Factory<ThreatFeedSigningConfig> {
  @Override
  public ThreatFeedSigningConfig get() {
    return provideThreatFeedSigningConfig();
  }

  public static NetworkConfigModule_ProvideThreatFeedSigningConfigFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ThreatFeedSigningConfig provideThreatFeedSigningConfig() {
    return Preconditions.checkNotNullFromProvides(NetworkConfigModule.INSTANCE.provideThreatFeedSigningConfig());
  }

  private static final class InstanceHolder {
    private static final NetworkConfigModule_ProvideThreatFeedSigningConfigFactory INSTANCE = new NetworkConfigModule_ProvideThreatFeedSigningConfigFactory();
  }
}
