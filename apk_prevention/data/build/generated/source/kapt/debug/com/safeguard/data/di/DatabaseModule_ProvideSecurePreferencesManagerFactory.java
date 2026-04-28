package com.safeguard.data.di;

import android.content.Context;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
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
public final class DatabaseModule_ProvideSecurePreferencesManagerFactory implements Factory<SecurePreferencesManager> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideSecurePreferencesManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SecurePreferencesManager get() {
    return provideSecurePreferencesManager(contextProvider.get());
  }

  public static DatabaseModule_ProvideSecurePreferencesManagerFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideSecurePreferencesManagerFactory(contextProvider);
  }

  public static SecurePreferencesManager provideSecurePreferencesManager(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSecurePreferencesManager(context));
  }
}
