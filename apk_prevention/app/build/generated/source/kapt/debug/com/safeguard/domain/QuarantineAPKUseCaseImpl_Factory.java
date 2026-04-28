package com.safeguard.domain;

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
public final class QuarantineAPKUseCaseImpl_Factory implements Factory<QuarantineAPKUseCaseImpl> {
  private final Provider<QuarantineRepository> repositoryProvider;

  public QuarantineAPKUseCaseImpl_Factory(Provider<QuarantineRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public QuarantineAPKUseCaseImpl get() {
    return newInstance(repositoryProvider.get());
  }

  public static QuarantineAPKUseCaseImpl_Factory create(
      Provider<QuarantineRepository> repositoryProvider) {
    return new QuarantineAPKUseCaseImpl_Factory(repositoryProvider);
  }

  public static QuarantineAPKUseCaseImpl newInstance(QuarantineRepository repository) {
    return new QuarantineAPKUseCaseImpl(repository);
  }
}
