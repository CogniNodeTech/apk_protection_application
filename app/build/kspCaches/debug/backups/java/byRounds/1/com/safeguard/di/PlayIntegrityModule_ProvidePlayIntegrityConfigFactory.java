package com.safeguard.di;

import com.safeguard.core.domain.integrity.PlayIntegrityConfig;
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
public final class PlayIntegrityModule_ProvidePlayIntegrityConfigFactory implements Factory<PlayIntegrityConfig> {
  @Override
  public PlayIntegrityConfig get() {
    return providePlayIntegrityConfig();
  }

  public static PlayIntegrityModule_ProvidePlayIntegrityConfigFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PlayIntegrityConfig providePlayIntegrityConfig() {
    return Preconditions.checkNotNullFromProvides(PlayIntegrityModule.INSTANCE.providePlayIntegrityConfig());
  }

  private static final class InstanceHolder {
    private static final PlayIntegrityModule_ProvidePlayIntegrityConfigFactory INSTANCE = new PlayIntegrityModule_ProvidePlayIntegrityConfigFactory();
  }
}
