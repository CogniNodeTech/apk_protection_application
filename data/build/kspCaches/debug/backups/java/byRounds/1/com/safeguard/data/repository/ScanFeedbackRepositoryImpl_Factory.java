package com.safeguard.data.repository;

import com.safeguard.core.domain.feedback.FeedbackPrivacyGate;
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao;
import com.safeguard.data.remote.api.ThreatIntelligenceApi;
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
public final class ScanFeedbackRepositoryImpl_Factory implements Factory<ScanFeedbackRepositoryImpl> {
  private final Provider<ScanFeedbackEventDao> daoProvider;

  private final Provider<ThreatIntelligenceApi> apiProvider;

  private final Provider<FeedbackPrivacyGate> privacyGateProvider;

  private final Provider<Moshi> moshiProvider;

  public ScanFeedbackRepositoryImpl_Factory(Provider<ScanFeedbackEventDao> daoProvider,
      Provider<ThreatIntelligenceApi> apiProvider,
      Provider<FeedbackPrivacyGate> privacyGateProvider, Provider<Moshi> moshiProvider) {
    this.daoProvider = daoProvider;
    this.apiProvider = apiProvider;
    this.privacyGateProvider = privacyGateProvider;
    this.moshiProvider = moshiProvider;
  }

  @Override
  public ScanFeedbackRepositoryImpl get() {
    return newInstance(daoProvider.get(), apiProvider.get(), privacyGateProvider.get(), moshiProvider.get());
  }

  public static ScanFeedbackRepositoryImpl_Factory create(
      Provider<ScanFeedbackEventDao> daoProvider, Provider<ThreatIntelligenceApi> apiProvider,
      Provider<FeedbackPrivacyGate> privacyGateProvider, Provider<Moshi> moshiProvider) {
    return new ScanFeedbackRepositoryImpl_Factory(daoProvider, apiProvider, privacyGateProvider, moshiProvider);
  }

  public static ScanFeedbackRepositoryImpl newInstance(ScanFeedbackEventDao dao,
      ThreatIntelligenceApi api, FeedbackPrivacyGate privacyGate, Moshi moshi) {
    return new ScanFeedbackRepositoryImpl(dao, api, privacyGate, moshi);
  }
}
