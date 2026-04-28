package com.safeguard.ui.screens.quarantine;

import com.safeguard.core.domain.repository.QuarantineRepository;
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
public final class QuarantineViewModel_Factory implements Factory<QuarantineViewModel> {
  private final Provider<QuarantineRepository> quarantineRepositoryProvider;

  public QuarantineViewModel_Factory(Provider<QuarantineRepository> quarantineRepositoryProvider) {
    this.quarantineRepositoryProvider = quarantineRepositoryProvider;
  }

  @Override
  public QuarantineViewModel get() {
    return newInstance(quarantineRepositoryProvider.get());
  }

  public static QuarantineViewModel_Factory create(
      Provider<QuarantineRepository> quarantineRepositoryProvider) {
    return new QuarantineViewModel_Factory(quarantineRepositoryProvider);
  }

  public static QuarantineViewModel newInstance(QuarantineRepository quarantineRepository) {
    return new QuarantineViewModel(quarantineRepository);
  }
}
