package com.safeguard.data.repository;

import com.safeguard.data.remote.api.AuthApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<AuthApiService> apiProvider;

  public AuthRepository_Factory(Provider<AuthApiService> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(apiProvider.get());
  }

  public static AuthRepository_Factory create(Provider<AuthApiService> apiProvider) {
    return new AuthRepository_Factory(apiProvider);
  }

  public static AuthRepository newInstance(AuthApiService api) {
    return new AuthRepository(api);
  }
}
