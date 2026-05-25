package com.safeguard.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.safeguard.core.domain.repository.ThreatFeedRepository;
import com.safeguard.core.domain.repository.ThreatFeedStatusStore;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
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
public final class ThreatFeedSyncWorker_Factory {
  private final Provider<ThreatFeedRepository> repositoryProvider;

  private final Provider<SecurePreferencesManager> preferencesProvider;

  private final Provider<ThreatFeedStatusStore> statusStoreProvider;

  public ThreatFeedSyncWorker_Factory(Provider<ThreatFeedRepository> repositoryProvider,
      Provider<SecurePreferencesManager> preferencesProvider,
      Provider<ThreatFeedStatusStore> statusStoreProvider) {
    this.repositoryProvider = repositoryProvider;
    this.preferencesProvider = preferencesProvider;
    this.statusStoreProvider = statusStoreProvider;
  }

  public ThreatFeedSyncWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, repositoryProvider.get(), preferencesProvider.get(), statusStoreProvider.get());
  }

  public static ThreatFeedSyncWorker_Factory create(
      Provider<ThreatFeedRepository> repositoryProvider,
      Provider<SecurePreferencesManager> preferencesProvider,
      Provider<ThreatFeedStatusStore> statusStoreProvider) {
    return new ThreatFeedSyncWorker_Factory(repositoryProvider, preferencesProvider, statusStoreProvider);
  }

  public static ThreatFeedSyncWorker newInstance(Context context, WorkerParameters params,
      ThreatFeedRepository repository, SecurePreferencesManager preferences,
      ThreatFeedStatusStore statusStore) {
    return new ThreatFeedSyncWorker(context, params, repository, preferences, statusStore);
  }
}
