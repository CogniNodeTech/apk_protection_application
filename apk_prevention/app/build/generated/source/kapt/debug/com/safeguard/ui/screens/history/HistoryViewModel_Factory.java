package com.safeguard.ui.screens.history;

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
public final class HistoryViewModel_Factory implements Factory<HistoryViewModel> {
  private final Provider<ScanRepository> scanRepositoryProvider;

  public HistoryViewModel_Factory(Provider<ScanRepository> scanRepositoryProvider) {
    this.scanRepositoryProvider = scanRepositoryProvider;
  }

  @Override
  public HistoryViewModel get() {
    return newInstance(scanRepositoryProvider.get());
  }

  public static HistoryViewModel_Factory create(Provider<ScanRepository> scanRepositoryProvider) {
    return new HistoryViewModel_Factory(scanRepositoryProvider);
  }

  public static HistoryViewModel newInstance(ScanRepository scanRepository) {
    return new HistoryViewModel(scanRepository);
  }
}
