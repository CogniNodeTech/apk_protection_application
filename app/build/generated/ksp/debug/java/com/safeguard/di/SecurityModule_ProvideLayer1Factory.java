package com.safeguard.di;

import com.safeguard.security.layers.layer1.Layer1FileMonitor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class SecurityModule_ProvideLayer1Factory implements Factory<Layer1FileMonitor> {
  @Override
  public Layer1FileMonitor get() {
    return provideLayer1();
  }

  public static SecurityModule_ProvideLayer1Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Layer1FileMonitor provideLayer1() {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideLayer1());
  }

  private static final class InstanceHolder {
    private static final SecurityModule_ProvideLayer1Factory INSTANCE = new SecurityModule_ProvideLayer1Factory();
  }
}
