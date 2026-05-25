package com.safeguard.di;

import com.safeguard.core.orchestration.ZeroTrustDecisionEngine;
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
public final class SecurityModule_ProvideZeroTrustDecisionEngineFactory implements Factory<ZeroTrustDecisionEngine> {
  @Override
  public ZeroTrustDecisionEngine get() {
    return provideZeroTrustDecisionEngine();
  }

  public static SecurityModule_ProvideZeroTrustDecisionEngineFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ZeroTrustDecisionEngine provideZeroTrustDecisionEngine() {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideZeroTrustDecisionEngine());
  }

  private static final class InstanceHolder {
    private static final SecurityModule_ProvideZeroTrustDecisionEngineFactory INSTANCE = new SecurityModule_ProvideZeroTrustDecisionEngineFactory();
  }
}
