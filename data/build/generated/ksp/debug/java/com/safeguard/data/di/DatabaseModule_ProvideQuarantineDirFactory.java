package com.safeguard.data.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.io.File;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DatabaseModule_ProvideQuarantineDirFactory implements Factory<File> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideQuarantineDirFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public File get() {
    return provideQuarantineDir(contextProvider.get());
  }

  public static DatabaseModule_ProvideQuarantineDirFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideQuarantineDirFactory(contextProvider);
  }

  public static File provideQuarantineDir(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideQuarantineDir(context));
  }
}
