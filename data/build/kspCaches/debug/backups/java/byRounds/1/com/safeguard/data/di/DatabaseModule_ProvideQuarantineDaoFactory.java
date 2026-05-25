package com.safeguard.data.di;

import com.safeguard.data.local.database.SafeGuardDatabase;
import com.safeguard.data.local.database.dao.QuarantineDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class DatabaseModule_ProvideQuarantineDaoFactory implements Factory<QuarantineDao> {
  private final Provider<SafeGuardDatabase> dbProvider;

  public DatabaseModule_ProvideQuarantineDaoFactory(Provider<SafeGuardDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public QuarantineDao get() {
    return provideQuarantineDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideQuarantineDaoFactory create(
      Provider<SafeGuardDatabase> dbProvider) {
    return new DatabaseModule_ProvideQuarantineDaoFactory(dbProvider);
  }

  public static QuarantineDao provideQuarantineDao(SafeGuardDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideQuarantineDao(db));
  }
}
