package com.safeguard.di;

import android.content.Context;
import com.safeguard.core.domain.security.DeviceIntegrityProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SecurityModule_ProvideDeviceIntegrityProviderFactory implements Factory<DeviceIntegrityProvider> {
  private final Provider<Context> contextProvider;

  public SecurityModule_ProvideDeviceIntegrityProviderFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DeviceIntegrityProvider get() {
    return provideDeviceIntegrityProvider(contextProvider.get());
  }

  public static SecurityModule_ProvideDeviceIntegrityProviderFactory create(
      Provider<Context> contextProvider) {
    return new SecurityModule_ProvideDeviceIntegrityProviderFactory(contextProvider);
  }

  public static DeviceIntegrityProvider provideDeviceIntegrityProvider(Context context) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideDeviceIntegrityProvider(context));
  }
}
