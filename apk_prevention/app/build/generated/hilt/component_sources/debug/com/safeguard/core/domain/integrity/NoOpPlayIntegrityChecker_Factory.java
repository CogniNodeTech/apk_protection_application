package com.safeguard.core.domain.integrity;

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
public final class NoOpPlayIntegrityChecker_Factory implements Factory<NoOpPlayIntegrityChecker> {
  @Override
  public NoOpPlayIntegrityChecker get() {
    return newInstance();
  }

  public static NoOpPlayIntegrityChecker_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NoOpPlayIntegrityChecker newInstance() {
    return new NoOpPlayIntegrityChecker();
  }

  private static final class InstanceHolder {
    private static final NoOpPlayIntegrityChecker_Factory INSTANCE = new NoOpPlayIntegrityChecker_Factory();
  }
}
