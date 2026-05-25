package com.safeguard.data.repository;

import android.content.Context;
import com.safeguard.data.local.database.dao.DeletedApkDao;
import com.safeguard.data.local.database.dao.QuarantineDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.io.File;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class QuarantineRepositoryImpl_Factory implements Factory<QuarantineRepositoryImpl> {
  private final Provider<QuarantineDao> quarantineDaoProvider;

  private final Provider<DeletedApkDao> deletedApkDaoProvider;

  private final Provider<File> quarantineDirProvider;

  private final Provider<Context> appContextProvider;

  public QuarantineRepositoryImpl_Factory(Provider<QuarantineDao> quarantineDaoProvider,
      Provider<DeletedApkDao> deletedApkDaoProvider, Provider<File> quarantineDirProvider,
      Provider<Context> appContextProvider) {
    this.quarantineDaoProvider = quarantineDaoProvider;
    this.deletedApkDaoProvider = deletedApkDaoProvider;
    this.quarantineDirProvider = quarantineDirProvider;
    this.appContextProvider = appContextProvider;
  }

  @Override
  public QuarantineRepositoryImpl get() {
    return newInstance(quarantineDaoProvider.get(), deletedApkDaoProvider.get(), quarantineDirProvider.get(), appContextProvider.get());
  }

  public static QuarantineRepositoryImpl_Factory create(
      Provider<QuarantineDao> quarantineDaoProvider, Provider<DeletedApkDao> deletedApkDaoProvider,
      Provider<File> quarantineDirProvider, Provider<Context> appContextProvider) {
    return new QuarantineRepositoryImpl_Factory(quarantineDaoProvider, deletedApkDaoProvider, quarantineDirProvider, appContextProvider);
  }

  public static QuarantineRepositoryImpl newInstance(QuarantineDao quarantineDao,
      DeletedApkDao deletedApkDao, File quarantineDir, Context appContext) {
    return new QuarantineRepositoryImpl(quarantineDao, deletedApkDao, quarantineDir, appContext);
  }
}
