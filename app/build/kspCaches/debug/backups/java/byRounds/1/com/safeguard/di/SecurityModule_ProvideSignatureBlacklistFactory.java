package com.safeguard.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.util.Set;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata({
    "javax.inject.Named",
    "dagger.hilt.android.qualifiers.ApplicationContext"
})
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
public final class SecurityModule_ProvideSignatureBlacklistFactory implements Factory<Set<String>> {
  private final Provider<Context> contextProvider;

  public SecurityModule_ProvideSignatureBlacklistFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public Set<String> get() {
    return provideSignatureBlacklist(contextProvider.get());
  }

  public static SecurityModule_ProvideSignatureBlacklistFactory create(
      Provider<Context> contextProvider) {
    return new SecurityModule_ProvideSignatureBlacklistFactory(contextProvider);
  }

  public static Set<String> provideSignatureBlacklist(Context context) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideSignatureBlacklist(context));
  }
}
