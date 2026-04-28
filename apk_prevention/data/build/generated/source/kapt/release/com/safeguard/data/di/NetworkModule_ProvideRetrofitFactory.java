package com.safeguard.data.di;

import com.squareup.moshi.Moshi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideRetrofitFactory implements Factory<Retrofit> {
  private final Provider<OkHttpClient> clientProvider;

  private final Provider<Moshi> moshiProvider;

  private final Provider<String> baseUrlProvider;

  public NetworkModule_ProvideRetrofitFactory(Provider<OkHttpClient> clientProvider,
      Provider<Moshi> moshiProvider, Provider<String> baseUrlProvider) {
    this.clientProvider = clientProvider;
    this.moshiProvider = moshiProvider;
    this.baseUrlProvider = baseUrlProvider;
  }

  @Override
  public Retrofit get() {
    return provideRetrofit(clientProvider.get(), moshiProvider.get(), baseUrlProvider.get());
  }

  public static NetworkModule_ProvideRetrofitFactory create(Provider<OkHttpClient> clientProvider,
      Provider<Moshi> moshiProvider, Provider<String> baseUrlProvider) {
    return new NetworkModule_ProvideRetrofitFactory(clientProvider, moshiProvider, baseUrlProvider);
  }

  public static Retrofit provideRetrofit(OkHttpClient client, Moshi moshi, String baseUrl) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideRetrofit(client, moshi, baseUrl));
  }
}
