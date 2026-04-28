package com.safeguard.data.di;

import com.safeguard.core.domain.repository.ThreatFeedStatusStore;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideThreatFeedStatusStoreFactory implements Factory<ThreatFeedStatusStore> {
  private final Provider<SecurePreferencesManager> prefsProvider;

  public DatabaseModule_ProvideThreatFeedStatusStoreFactory(
      Provider<SecurePreferencesManager> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public ThreatFeedStatusStore get() {
    return provideThreatFeedStatusStore(prefsProvider.get());
  }

  public static DatabaseModule_ProvideThreatFeedStatusStoreFactory create(
      Provider<SecurePreferencesManager> prefsProvider) {
    return new DatabaseModule_ProvideThreatFeedStatusStoreFactory(prefsProvider);
  }

  public static ThreatFeedStatusStore provideThreatFeedStatusStore(SecurePreferencesManager prefs) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideThreatFeedStatusStore(prefs));
  }
}
