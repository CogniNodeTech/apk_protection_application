package com.safeguard.di;

import com.safeguard.core.orchestration.ForensicReasoningEngine;
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
public final class SecurityModule_ProvideForensicReasoningEngineFactory implements Factory<ForensicReasoningEngine> {
  @Override
  public ForensicReasoningEngine get() {
    return provideForensicReasoningEngine();
  }

  public static SecurityModule_ProvideForensicReasoningEngineFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ForensicReasoningEngine provideForensicReasoningEngine() {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideForensicReasoningEngine());
  }

  private static final class InstanceHolder {
    private static final SecurityModule_ProvideForensicReasoningEngineFactory INSTANCE = new SecurityModule_ProvideForensicReasoningEngineFactory();
  }
}
