package com.safeguard.di;

import com.safeguard.security.layers.layer3.PermissionAnalyzer;
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
public final class SecurityModule_ProvideLayer3Factory implements Factory<PermissionAnalyzer> {
  @Override
  public PermissionAnalyzer get() {
    return provideLayer3();
  }

  public static SecurityModule_ProvideLayer3Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PermissionAnalyzer provideLayer3() {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideLayer3());
  }

  private static final class InstanceHolder {
    private static final SecurityModule_ProvideLayer3Factory INSTANCE = new SecurityModule_ProvideLayer3Factory();
  }
}
