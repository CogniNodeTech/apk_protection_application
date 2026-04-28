package com.safeguard.data.repository;

import com.safeguard.data.local.database.dao.AuditLogDao;
import com.safeguard.data.local.database.dao.ScanHistoryDao;
import com.squareup.moshi.Moshi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ScanRepositoryImpl_Factory implements Factory<ScanRepositoryImpl> {
  private final Provider<ScanHistoryDao> scanHistoryDaoProvider;

  private final Provider<AuditLogDao> auditLogDaoProvider;

  private final Provider<Moshi> moshiProvider;

  public ScanRepositoryImpl_Factory(Provider<ScanHistoryDao> scanHistoryDaoProvider,
      Provider<AuditLogDao> auditLogDaoProvider, Provider<Moshi> moshiProvider) {
    this.scanHistoryDaoProvider = scanHistoryDaoProvider;
    this.auditLogDaoProvider = auditLogDaoProvider;
    this.moshiProvider = moshiProvider;
  }

  @Override
  public ScanRepositoryImpl get() {
    return newInstance(scanHistoryDaoProvider.get(), auditLogDaoProvider.get(), moshiProvider.get());
  }

  public static ScanRepositoryImpl_Factory create(Provider<ScanHistoryDao> scanHistoryDaoProvider,
      Provider<AuditLogDao> auditLogDaoProvider, Provider<Moshi> moshiProvider) {
    return new ScanRepositoryImpl_Factory(scanHistoryDaoProvider, auditLogDaoProvider, moshiProvider);
  }

  public static ScanRepositoryImpl newInstance(ScanHistoryDao scanHistoryDao,
      AuditLogDao auditLogDao, Moshi moshi) {
    return new ScanRepositoryImpl(scanHistoryDao, auditLogDao, moshi);
  }
}
