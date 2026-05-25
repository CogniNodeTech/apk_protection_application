package com.safeguard.di;

import com.safeguard.mlmodel.FeatureExtractor;
import com.safeguard.mlmodel.TFLiteRunner;
import com.safeguard.security.layers.layer5.MLAnalyzer;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class SecurityModule_ProvideLayer5Factory implements Factory<MLAnalyzer> {
  private final Provider<FeatureExtractor> featureExtractorProvider;

  private final Provider<TFLiteRunner> tfliteRunnerProvider;

  public SecurityModule_ProvideLayer5Factory(Provider<FeatureExtractor> featureExtractorProvider,
      Provider<TFLiteRunner> tfliteRunnerProvider) {
    this.featureExtractorProvider = featureExtractorProvider;
    this.tfliteRunnerProvider = tfliteRunnerProvider;
  }

  @Override
  public MLAnalyzer get() {
    return provideLayer5(featureExtractorProvider.get(), tfliteRunnerProvider.get());
  }

  public static SecurityModule_ProvideLayer5Factory create(
      Provider<FeatureExtractor> featureExtractorProvider,
      Provider<TFLiteRunner> tfliteRunnerProvider) {
    return new SecurityModule_ProvideLayer5Factory(featureExtractorProvider, tfliteRunnerProvider);
  }

  public static MLAnalyzer provideLayer5(FeatureExtractor featureExtractor,
      TFLiteRunner tfliteRunner) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideLayer5(featureExtractor, tfliteRunner));
  }
}
