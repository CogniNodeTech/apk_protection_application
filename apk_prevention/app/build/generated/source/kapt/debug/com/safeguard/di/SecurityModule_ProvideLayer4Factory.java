package com.safeguard.di;

import com.safeguard.security.layers.layer4.SignatureValidator;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.util.Set;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SecurityModule_ProvideLayer4Factory implements Factory<SignatureValidator> {
  private final Provider<Set<String>> blacklistProvider;

  public SecurityModule_ProvideLayer4Factory(Provider<Set<String>> blacklistProvider) {
    this.blacklistProvider = blacklistProvider;
  }

  @Override
  public SignatureValidator get() {
    return provideLayer4(blacklistProvider.get());
  }

  public static SecurityModule_ProvideLayer4Factory create(
      Provider<Set<String>> blacklistProvider) {
    return new SecurityModule_ProvideLayer4Factory(blacklistProvider);
  }

  public static SignatureValidator provideLayer4(Set<String> blacklist) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideLayer4(blacklist));
  }
}
