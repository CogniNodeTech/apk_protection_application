package com.safeguard.di;

import android.content.Context;
import com.safeguard.core.domain.integrity.NoOpPlayIntegrityChecker;
import com.safeguard.core.domain.integrity.PlayIntegrityChecker;
import com.safeguard.core.domain.integrity.PlayIntegrityConfig;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class PlayIntegrityModule_ProvidePlayIntegrityCheckerFactory implements Factory<PlayIntegrityChecker> {
  private final Provider<PlayIntegrityConfig> configProvider;

  private final Provider<Context> contextProvider;

  private final Provider<NoOpPlayIntegrityChecker> noOpProvider;

  public PlayIntegrityModule_ProvidePlayIntegrityCheckerFactory(
      Provider<PlayIntegrityConfig> configProvider, Provider<Context> contextProvider,
      Provider<NoOpPlayIntegrityChecker> noOpProvider) {
    this.configProvider = configProvider;
    this.contextProvider = contextProvider;
    this.noOpProvider = noOpProvider;
  }

  @Override
  public PlayIntegrityChecker get() {
    return providePlayIntegrityChecker(configProvider.get(), contextProvider.get(), noOpProvider.get());
  }

  public static PlayIntegrityModule_ProvidePlayIntegrityCheckerFactory create(
      Provider<PlayIntegrityConfig> configProvider, Provider<Context> contextProvider,
      Provider<NoOpPlayIntegrityChecker> noOpProvider) {
    return new PlayIntegrityModule_ProvidePlayIntegrityCheckerFactory(configProvider, contextProvider, noOpProvider);
  }

  public static PlayIntegrityChecker providePlayIntegrityChecker(PlayIntegrityConfig config,
      Context context, NoOpPlayIntegrityChecker noOp) {
    return Preconditions.checkNotNullFromProvides(PlayIntegrityModule.INSTANCE.providePlayIntegrityChecker(config, context, noOp));
  }
}
