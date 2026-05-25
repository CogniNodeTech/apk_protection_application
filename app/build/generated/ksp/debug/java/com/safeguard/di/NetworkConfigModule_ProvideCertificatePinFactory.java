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
public final class NetworkConfigModule_ProvideCertificatePinFactory implements Factory<String> {
  @Override
  public String get() {
    return provideCertificatePin();
  }

  public static NetworkConfigModule_ProvideCertificatePinFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static String provideCertificatePin() {
    return NetworkConfigModule.INSTANCE.provideCertificatePin();
  }

  private static final class InstanceHolder {
    private static final NetworkConfigModule_ProvideCertificatePinFactory INSTANCE = new NetworkConfigModule_ProvideCertificatePinFactory();
  }
}
