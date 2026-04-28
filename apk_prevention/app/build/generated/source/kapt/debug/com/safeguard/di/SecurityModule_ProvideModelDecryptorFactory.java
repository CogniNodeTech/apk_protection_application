package com.safeguard.di;

import android.content.Context;
import com.safeguard.mlmodel.ModelDecryptor;
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
public final class SecurityModule_ProvideModelDecryptorFactory implements Factory<ModelDecryptor> {
  private final Provider<Context> contextProvider;

  public SecurityModule_ProvideModelDecryptorFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ModelDecryptor get() {
    return provideModelDecryptor(contextProvider.get());
  }

  public static SecurityModule_ProvideModelDecryptorFactory create(
      Provider<Context> contextProvider) {
    return new SecurityModule_ProvideModelDecryptorFactory(contextProvider);
  }

  public static ModelDecryptor provideModelDecryptor(Context context) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideModelDecryptor(context));
  }
}
