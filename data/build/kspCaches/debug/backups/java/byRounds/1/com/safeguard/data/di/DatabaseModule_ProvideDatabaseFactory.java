package com.safeguard.data.di;

import android.content.Context;
import com.safeguard.data.local.database.SafeGuardDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "dagger.hilt.android.qualifiers.ApplicationContext",
    "javax.inject.Named"
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
public final class DatabaseModule_ProvideDatabaseFactory implements Factory<SafeGuardDatabase> {
  private final Provider<Context> contextProvider;

  private final Provider<byte[]> passphraseProvider;

  public DatabaseModule_ProvideDatabaseFactory(Provider<Context> contextProvider,
      Provider<byte[]> passphraseProvider) {
    this.contextProvider = contextProvider;
    this.passphraseProvider = passphraseProvider;
  }

  @Override
  public SafeGuardDatabase get() {
    return provideDatabase(contextProvider.get(), passphraseProvider.get());
  }

  public static DatabaseModule_ProvideDatabaseFactory create(Provider<Context> contextProvider,
      Provider<byte[]> passphraseProvider) {
    return new DatabaseModule_ProvideDatabaseFactory(contextProvider, passphraseProvider);
  }

  public static SafeGuardDatabase provideDatabase(Context context, byte[] passphrase) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDatabase(context, passphrase));
  }
}
