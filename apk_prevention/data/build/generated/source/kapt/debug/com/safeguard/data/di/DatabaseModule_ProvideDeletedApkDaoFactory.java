package com.safeguard.data.di;

import com.safeguard.data.local.database.SafeGuardDatabase;
import com.safeguard.data.local.database.dao.DeletedApkDao;
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
public final class DatabaseModule_ProvideDeletedApkDaoFactory implements Factory<DeletedApkDao> {
  private final Provider<SafeGuardDatabase> dbProvider;

  public DatabaseModule_ProvideDeletedApkDaoFactory(Provider<SafeGuardDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public DeletedApkDao get() {
    return provideDeletedApkDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideDeletedApkDaoFactory create(
      Provider<SafeGuardDatabase> dbProvider) {
    return new DatabaseModule_ProvideDeletedApkDaoFactory(dbProvider);
  }

  public static DeletedApkDao provideDeletedApkDao(SafeGuardDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDeletedApkDao(db));
  }
}
