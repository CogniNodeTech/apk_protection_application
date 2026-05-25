package com.safeguard.data.di;

import com.safeguard.data.local.database.SafeGuardDatabase;
import com.safeguard.data.local.database.dao.ScanHistoryDao;
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
public final class DatabaseModule_ProvideScanHistoryDaoFactory implements Factory<ScanHistoryDao> {
  private final Provider<SafeGuardDatabase> dbProvider;

  public DatabaseModule_ProvideScanHistoryDaoFactory(Provider<SafeGuardDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ScanHistoryDao get() {
    return provideScanHistoryDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideScanHistoryDaoFactory create(
      Provider<SafeGuardDatabase> dbProvider) {
    return new DatabaseModule_ProvideScanHistoryDaoFactory(dbProvider);
  }

  public static ScanHistoryDao provideScanHistoryDao(SafeGuardDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideScanHistoryDao(db));
  }
}
