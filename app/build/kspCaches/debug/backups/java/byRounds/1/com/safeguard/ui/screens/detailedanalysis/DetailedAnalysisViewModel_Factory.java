package com.safeguard.ui.screens.detailedanalysis;

import com.safeguard.core.domain.repository.ScanRepository;
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
public final class DetailedAnalysisViewModel_Factory implements Factory<DetailedAnalysisViewModel> {
  private final Provider<ScanRepository> scanRepositoryProvider;

  public DetailedAnalysisViewModel_Factory(Provider<ScanRepository> scanRepositoryProvider) {
    this.scanRepositoryProvider = scanRepositoryProvider;
  }

  @Override
  public DetailedAnalysisViewModel get() {
    return newInstance(scanRepositoryProvider.get());
  }

  public static DetailedAnalysisViewModel_Factory create(
      Provider<ScanRepository> scanRepositoryProvider) {
    return new DetailedAnalysisViewModel_Factory(scanRepositoryProvider);
  }

  public static DetailedAnalysisViewModel newInstance(ScanRepository scanRepository) {
    return new DetailedAnalysisViewModel(scanRepository);
  }
}
