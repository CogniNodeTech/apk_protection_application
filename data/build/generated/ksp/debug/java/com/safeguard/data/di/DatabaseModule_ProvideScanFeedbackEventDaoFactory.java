package com.safeguard.data.di;

import com.safeguard.data.local.database.SafeGuardDatabase;
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao;
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
public final class DatabaseModule_ProvideScanFeedbackEventDaoFactory implements Factory<ScanFeedbackEventDao> {
  private final Provider<SafeGuardDatabase> dbProvider;

  public DatabaseModule_ProvideScanFeedbackEventDaoFactory(Provider<SafeGuardDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ScanFeedbackEventDao get() {
    return provideScanFeedbackEventDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideScanFeedbackEventDaoFactory create(
      Provider<SafeGuardDatabase> dbProvider) {
    return new DatabaseModule_ProvideScanFeedbackEventDaoFactory(dbProvider);
  }

  public static ScanFeedbackEventDao provideScanFeedbackEventDao(SafeGuardDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideScanFeedbackEventDao(db));
  }
}
