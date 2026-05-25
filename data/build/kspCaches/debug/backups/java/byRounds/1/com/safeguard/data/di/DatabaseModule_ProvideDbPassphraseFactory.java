package com.safeguard.data.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class DatabaseModule_ProvideDbPassphraseFactory implements Factory<byte[]> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideDbPassphraseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public byte[] get() {
    return provideDbPassphrase(contextProvider.get());
  }

  public static DatabaseModule_ProvideDbPassphraseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideDbPassphraseFactory(contextProvider);
  }

  public static byte[] provideDbPassphrase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDbPassphrase(context));
  }
}
