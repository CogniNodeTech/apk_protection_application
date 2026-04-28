package com.safeguard.data.di;

import com.safeguard.data.local.database.SafeGuardDatabase;
import com.safeguard.data.local.database.dao.AuditLogDao;
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
public final class DatabaseModule_ProvideAuditLogDaoFactory implements Factory<AuditLogDao> {
  private final Provider<SafeGuardDatabase> dbProvider;

  public DatabaseModule_ProvideAuditLogDaoFactory(Provider<SafeGuardDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AuditLogDao get() {
    return provideAuditLogDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideAuditLogDaoFactory create(
      Provider<SafeGuardDatabase> dbProvider) {
    return new DatabaseModule_ProvideAuditLogDaoFactory(dbProvider);
  }

  public static AuditLogDao provideAuditLogDao(SafeGuardDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAuditLogDao(db));
  }
}
