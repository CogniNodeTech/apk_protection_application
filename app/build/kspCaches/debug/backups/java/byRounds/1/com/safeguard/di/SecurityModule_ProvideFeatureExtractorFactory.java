package com.safeguard.di;

import com.safeguard.mlmodel.FeatureExtractor;
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
public final class SecurityModule_ProvideFeatureExtractorFactory implements Factory<FeatureExtractor> {
  @Override
  public FeatureExtractor get() {
    return provideFeatureExtractor();
  }

  public static SecurityModule_ProvideFeatureExtractorFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FeatureExtractor provideFeatureExtractor() {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideFeatureExtractor());
  }

  private static final class InstanceHolder {
    private static final SecurityModule_ProvideFeatureExtractorFactory INSTANCE = new SecurityModule_ProvideFeatureExtractorFactory();
  }
}
