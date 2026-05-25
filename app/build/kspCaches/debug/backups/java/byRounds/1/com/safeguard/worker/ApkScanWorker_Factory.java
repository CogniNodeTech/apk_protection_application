package com.safeguard.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
import com.safeguard.core.domain.usecase.ScanAPKUseCase;
import dagger.internal.DaggerGenerated;
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
public final class ApkScanWorker_Factory {
  private final Provider<ScanAPKUseCase> scanAPKUseCaseProvider;

  private final Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider;

  private final Provider<QuarantineRepository> quarantineRepositoryProvider;

  public ApkScanWorker_Factory(Provider<ScanAPKUseCase> scanAPKUseCaseProvider,
      Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider) {
    this.scanAPKUseCaseProvider = scanAPKUseCaseProvider;
    this.quarantineAPKUseCaseProvider = quarantineAPKUseCaseProvider;
    this.quarantineRepositoryProvider = quarantineRepositoryProvider;
  }

  public ApkScanWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, scanAPKUseCaseProvider.get(), quarantineAPKUseCaseProvider.get(), quarantineRepositoryProvider.get());
  }

  public static ApkScanWorker_Factory create(Provider<ScanAPKUseCase> scanAPKUseCaseProvider,
      Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider) {
    return new ApkScanWorker_Factory(scanAPKUseCaseProvider, quarantineAPKUseCaseProvider, quarantineRepositoryProvider);
  }

  public static ApkScanWorker newInstance(Context context, WorkerParameters params,
      ScanAPKUseCase scanAPKUseCase, QuarantineAPKUseCase quarantineAPKUseCase,
      QuarantineRepository quarantineRepository) {
    return new ApkScanWorker(context, params, scanAPKUseCase, quarantineAPKUseCase, quarantineRepository);
  }
}
