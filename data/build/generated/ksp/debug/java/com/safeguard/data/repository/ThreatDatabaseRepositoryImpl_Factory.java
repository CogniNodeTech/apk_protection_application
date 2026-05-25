package com.safeguard.data.repository;

import com.safeguard.data.local.database.dao.MalwareSignatureDao;
import com.safeguard.data.local.database.dao.TrustedAppDao;
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
public final class ThreatDatabaseRepositoryImpl_Factory implements Factory<ThreatDatabaseRepositoryImpl> {
  private final Provider<MalwareSignatureDao> malwareDaoProvider;

  private final Provider<TrustedAppDao> trustedAppDaoProvider;

  public ThreatDatabaseRepositoryImpl_Factory(Provider<MalwareSignatureDao> malwareDaoProvider,
      Provider<TrustedAppDao> trustedAppDaoProvider) {
    this.malwareDaoProvider = malwareDaoProvider;
    this.trustedAppDaoProvider = trustedAppDaoProvider;
  }

  @Override
  public ThreatDatabaseRepositoryImpl get() {
    return newInstance(malwareDaoProvider.get(), trustedAppDaoProvider.get());
  }

  public static ThreatDatabaseRepositoryImpl_Factory create(
      Provider<MalwareSignatureDao> malwareDaoProvider,
      Provider<TrustedAppDao> trustedAppDaoProvider) {
    return new ThreatDatabaseRepositoryImpl_Factory(malwareDaoProvider, trustedAppDaoProvider);
  }

  public static ThreatDatabaseRepositoryImpl newInstance(MalwareSignatureDao malwareDao,
      TrustedAppDao trustedAppDao) {
    return new ThreatDatabaseRepositoryImpl(malwareDao, trustedAppDao);
  }
}
