package com.safeguard.di;

import com.safeguard.core.orchestration.RiskAssessmentEngine;
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
public final class SecurityModule_ProvideRiskAssessmentEngineFactory implements Factory<RiskAssessmentEngine> {
  @Override
  public RiskAssessmentEngine get() {
    return provideRiskAssessmentEngine();
  }

  public static SecurityModule_ProvideRiskAssessmentEngineFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RiskAssessmentEngine provideRiskAssessmentEngine() {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideRiskAssessmentEngine());
  }

  private static final class InstanceHolder {
    private static final SecurityModule_ProvideRiskAssessmentEngineFactory INSTANCE = new SecurityModule_ProvideRiskAssessmentEngineFactory();
  }
}
