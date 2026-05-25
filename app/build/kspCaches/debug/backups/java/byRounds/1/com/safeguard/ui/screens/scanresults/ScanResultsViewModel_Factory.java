package com.safeguard.ui.screens.scanresults;

import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
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
public final class ScanResultsViewModel_Factory implements Factory<ScanResultsViewModel> {
  private final Provider<ScanRepository> scanRepositoryProvider;

  private final Provider<QuarantineAPKUseCase> quarantineUseCaseProvider;

  private final Provider<QuarantineRepository> quarantineRepositoryProvider;

  public ScanResultsViewModel_Factory(Provider<ScanRepository> scanRepositoryProvider,
      Provider<QuarantineAPKUseCase> quarantineUseCaseProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider) {
    this.scanRepositoryProvider = scanRepositoryProvider;
    this.quarantineUseCaseProvider = quarantineUseCaseProvider;
    this.quarantineRepositoryProvider = quarantineRepositoryProvider;
  }

  @Override
  public ScanResultsViewModel get() {
    return newInstance(scanRepositoryProvider.get(), quarantineUseCaseProvider.get(), quarantineRepositoryProvider.get());
  }

  public static ScanResultsViewModel_Factory create(Provider<ScanRepository> scanRepositoryProvider,
      Provider<QuarantineAPKUseCase> quarantineUseCaseProvider,
      Provider<QuarantineRepository> quarantineRepositoryProvider) {
    return new ScanResultsViewModel_Factory(scanRepositoryProvider, quarantineUseCaseProvider, quarantineRepositoryProvider);
  }

  public static ScanResultsViewModel newInstance(ScanRepository scanRepository,
      QuarantineAPKUseCase quarantineUseCase, QuarantineRepository quarantineRepository) {
    return new ScanResultsViewModel(scanRepository, quarantineUseCase, quarantineRepository);
  }
}
