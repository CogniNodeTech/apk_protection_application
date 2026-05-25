package com.safeguard.data.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class NetworkModule_ProvideOkHttpClientFactory implements Factory<OkHttpClient> {
  private final Provider<String> baseUrlProvider;

  private final Provider<String> certPinProvider;

  private final Provider<Boolean> httpLoggingEnabledProvider;

  private final Provider<String> apiKeyProvider;

  public NetworkModule_ProvideOkHttpClientFactory(Provider<String> baseUrlProvider,
      Provider<String> certPinProvider, Provider<Boolean> httpLoggingEnabledProvider,
      Provider<String> apiKeyProvider) {
    this.baseUrlProvider = baseUrlProvider;
    this.certPinProvider = certPinProvider;
    this.httpLoggingEnabledProvider = httpLoggingEnabledProvider;
    this.apiKeyProvider = apiKeyProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideOkHttpClient(baseUrlProvider.get(), certPinProvider.get(), httpLoggingEnabledProvider.get(), apiKeyProvider.get());
  }

  public static NetworkModule_ProvideOkHttpClientFactory create(Provider<String> baseUrlProvider,
      Provider<String> certPinProvider, Provider<Boolean> httpLoggingEnabledProvider,
      Provider<String> apiKeyProvider) {
    return new NetworkModule_ProvideOkHttpClientFactory(baseUrlProvider, certPinProvider, httpLoggingEnabledProvider, apiKeyProvider);
  }

  public static OkHttpClient provideOkHttpClient(String baseUrl, String certPin,
      boolean httpLoggingEnabled, String apiKey) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideOkHttpClient(baseUrl, certPin, httpLoggingEnabled, apiKey));
  }
}
