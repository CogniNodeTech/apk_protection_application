package com.safeguard.di;

import android.content.Context;
import com.safeguard.mlmodel.ModelDecryptor;
import com.safeguard.mlmodel.TFLiteRunner;
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
public final class SecurityModule_ProvideTFLiteRunnerFactory implements Factory<TFLiteRunner> {
  private final Provider<Context> contextProvider;

  private final Provider<ModelDecryptor> modelDecryptorProvider;

  public SecurityModule_ProvideTFLiteRunnerFactory(Provider<Context> contextProvider,
      Provider<ModelDecryptor> modelDecryptorProvider) {
    this.contextProvider = contextProvider;
    this.modelDecryptorProvider = modelDecryptorProvider;
  }

  @Override
  public TFLiteRunner get() {
    return provideTFLiteRunner(contextProvider.get(), modelDecryptorProvider.get());
  }

  public static SecurityModule_ProvideTFLiteRunnerFactory create(Provider<Context> contextProvider,
      Provider<ModelDecryptor> modelDecryptorProvider) {
    return new SecurityModule_ProvideTFLiteRunnerFactory(contextProvider, modelDecryptorProvider);
  }

  public static TFLiteRunner provideTFLiteRunner(Context context, ModelDecryptor modelDecryptor) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideTFLiteRunner(context, modelDecryptor));
  }
}
