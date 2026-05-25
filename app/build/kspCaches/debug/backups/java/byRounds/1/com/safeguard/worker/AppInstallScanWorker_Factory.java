package com.safeguard.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
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
public final class AppInstallScanWorker_Factory {
  private final Provider<ScanAPKUseCase> scanAPKUseCaseProvider;

  private final Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider;

  public AppInstallScanWorker_Factory(Provider<ScanAPKUseCase> scanAPKUseCaseProvider,
      Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider) {
    this.scanAPKUseCaseProvider = scanAPKUseCaseProvider;
    this.quarantineAPKUseCaseProvider = quarantineAPKUseCaseProvider;
  }

  public AppInstallScanWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, scanAPKUseCaseProvider.get(), quarantineAPKUseCaseProvider.get());
  }

  public static AppInstallScanWorker_Factory create(Provider<ScanAPKUseCase> scanAPKUseCaseProvider,
      Provider<QuarantineAPKUseCase> quarantineAPKUseCaseProvider) {
    return new AppInstallScanWorker_Factory(scanAPKUseCaseProvider, quarantineAPKUseCaseProvider);
  }

  public static AppInstallScanWorker newInstance(Context context, WorkerParameters params,
      ScanAPKUseCase scanAPKUseCase, QuarantineAPKUseCase quarantineAPKUseCase) {
    return new AppInstallScanWorker(context, params, scanAPKUseCase, quarantineAPKUseCase);
  }
}
