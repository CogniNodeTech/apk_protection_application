package com.safeguard.data.di;

import com.safeguard.data.remote.api.ThreatIntelligenceApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideThreatIntelligenceApiFactory implements Factory<ThreatIntelligenceApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideThreatIntelligenceApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public ThreatIntelligenceApi get() {
    return provideThreatIntelligenceApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideThreatIntelligenceApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideThreatIntelligenceApiFactory(retrofitProvider);
  }

  public static ThreatIntelligenceApi provideThreatIntelligenceApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideThreatIntelligenceApi(retrofit));
  }
}
