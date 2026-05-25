package com.safeguard.di;

import com.safeguard.core.domain.repository.ThreatDatabaseRepository;
import com.safeguard.security.layers.layer2.HashValidator;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class SecurityModule_ProvideLayer2Factory implements Factory<HashValidator> {
  private final Provider<ThreatDatabaseRepository> threatDbProvider;

  public SecurityModule_ProvideLayer2Factory(Provider<ThreatDatabaseRepository> threatDbProvider) {
    this.threatDbProvider = threatDbProvider;
  }

  @Override
  public HashValidator get() {
    return provideLayer2(threatDbProvider.get());
  }

  public static SecurityModule_ProvideLayer2Factory create(
      Provider<ThreatDatabaseRepository> threatDbProvider) {
    return new SecurityModule_ProvideLayer2Factory(threatDbProvider);
  }

  public static HashValidator provideLayer2(ThreatDatabaseRepository threatDb) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideLayer2(threatDb));
  }
}
