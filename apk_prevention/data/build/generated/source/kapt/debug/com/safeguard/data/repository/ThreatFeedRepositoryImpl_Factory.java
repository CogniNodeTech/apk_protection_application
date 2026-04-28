package com.safeguard.data.repository;

import com.safeguard.core.domain.repository.ThreatFeedCursorStore;
import com.safeguard.core.domain.repository.ThreatFeedStatusStore;
import com.safeguard.data.local.database.dao.MalwareSignatureDao;
import com.safeguard.data.remote.api.ThreatIntelligenceApi;
import com.safeguard.data.remote.signing.ThreatFeedSignatureVerifier;
import com.squareup.moshi.Moshi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ThreatFeedRepositoryImpl_Factory implements Factory<ThreatFeedRepositoryImpl> {
  private final Provider<ThreatIntelligenceApi> apiProvider;

  private final Provider<MalwareSignatureDao> malwareDaoProvider;

  private final Provider<ThreatFeedCursorStore> cursorStoreProvider;

  private final Provider<ThreatFeedStatusStore> statusStoreProvider;

  private final Provider<ThreatFeedSignatureVerifier> signatureVerifierProvider;

  private final Provider<Moshi> moshiProvider;

  public ThreatFeedRepositoryImpl_Factory(Provider<ThreatIntelligenceApi> apiProvider,
      Provider<MalwareSignatureDao> malwareDaoProvider,
      Provider<ThreatFeedCursorStore> cursorStoreProvider,
      Provider<ThreatFeedStatusStore> statusStoreProvider,
      Provider<ThreatFeedSignatureVerifier> signatureVerifierProvider,
      Provider<Moshi> moshiProvider) {
    this.apiProvider = apiProvider;
    this.malwareDaoProvider = malwareDaoProvider;
    this.cursorStoreProvider = cursorStoreProvider;
    this.statusStoreProvider = statusStoreProvider;
    this.signatureVerifierProvider = signatureVerifierProvider;
    this.moshiProvider = moshiProvider;
  }

  @Override
  public ThreatFeedRepositoryImpl get() {
    return newInstance(apiProvider.get(), malwareDaoProvider.get(), cursorStoreProvider.get(), statusStoreProvider.get(), signatureVerifierProvider.get(), moshiProvider.get());
  }

  public static ThreatFeedRepositoryImpl_Factory create(Provider<ThreatIntelligenceApi> apiProvider,
      Provider<MalwareSignatureDao> malwareDaoProvider,
      Provider<ThreatFeedCursorStore> cursorStoreProvider,
      Provider<ThreatFeedStatusStore> statusStoreProvider,
      Provider<ThreatFeedSignatureVerifier> signatureVerifierProvider,
      Provider<Moshi> moshiProvider) {
    return new ThreatFeedRepositoryImpl_Factory(apiProvider, malwareDaoProvider, cursorStoreProvider, statusStoreProvider, signatureVerifierProvider, moshiProvider);
  }

  public static ThreatFeedRepositoryImpl newInstance(ThreatIntelligenceApi api,
      MalwareSignatureDao malwareDao, ThreatFeedCursorStore cursorStore,
      ThreatFeedStatusStore statusStore, ThreatFeedSignatureVerifier signatureVerifier,
      Moshi moshi) {
    return new ThreatFeedRepositoryImpl(api, malwareDao, cursorStore, statusStore, signatureVerifier, moshi);
  }
}
