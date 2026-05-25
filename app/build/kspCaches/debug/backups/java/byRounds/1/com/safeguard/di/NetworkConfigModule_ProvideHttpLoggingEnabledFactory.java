package com.safeguard.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class NetworkConfigModule_ProvideHttpLoggingEnabledFactory implements Factory<Boolean> {
  @Override
  public Boolean get() {
    return provideHttpLoggingEnabled();
  }

  public static NetworkConfigModule_ProvideHttpLoggingEnabledFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static boolean provideHttpLoggingEnabled() {
    return NetworkConfigModule.INSTANCE.provideHttpLoggingEnabled();
  }

  private static final class InstanceHolder {
    private static final NetworkConfigModule_ProvideHttpLoggingEnabledFactory INSTANCE = new NetworkConfigModule_ProvideHttpLoggingEnabledFactory();
  }
}
