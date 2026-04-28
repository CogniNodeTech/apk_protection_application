package com.safeguard.telemetry;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class NoOpScanTelemetry_Factory implements Factory<NoOpScanTelemetry> {
  @Override
  public NoOpScanTelemetry get() {
    return newInstance();
  }

  public static NoOpScanTelemetry_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NoOpScanTelemetry newInstance() {
    return new NoOpScanTelemetry();
  }

  private static final class InstanceHolder {
    private static final NoOpScanTelemetry_Factory INSTANCE = new NoOpScanTelemetry_Factory();
  }
}
