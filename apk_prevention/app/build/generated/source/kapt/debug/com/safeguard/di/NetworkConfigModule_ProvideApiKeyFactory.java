package com.safeguard.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.Nullable;

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
public final class NetworkConfigModule_ProvideApiKeyFactory implements Factory<String> {
  @Override
  @Nullable
  public String get() {
    return provideApiKey();
  }

  public static NetworkConfigModule_ProvideApiKeyFactory create() {
    return InstanceHolder.INSTANCE;
  }

  @Nullable
  public static String provideApiKey() {
    return NetworkConfigModule.INSTANCE.provideApiKey();
  }

  private static final class InstanceHolder {
    private static final NetworkConfigModule_ProvideApiKeyFactory INSTANCE = new NetworkConfigModule_ProvideApiKeyFactory();
  }
}
