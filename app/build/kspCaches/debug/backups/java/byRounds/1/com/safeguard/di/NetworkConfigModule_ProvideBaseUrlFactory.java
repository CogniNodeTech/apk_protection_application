package com.safeguard.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class NetworkConfigModule_ProvideBaseUrlFactory implements Factory<String> {
  @Override
  public String get() {
    return provideBaseUrl();
  }

  public static NetworkConfigModule_ProvideBaseUrlFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static String provideBaseUrl() {
    return Preconditions.checkNotNullFromProvides(NetworkConfigModule.INSTANCE.provideBaseUrl());
  }

  private static final class InstanceHolder {
    private static final NetworkConfigModule_ProvideBaseUrlFactory INSTANCE = new NetworkConfigModule_ProvideBaseUrlFactory();
  }
}
