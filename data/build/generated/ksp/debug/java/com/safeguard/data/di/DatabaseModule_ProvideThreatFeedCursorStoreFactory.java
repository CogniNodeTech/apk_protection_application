package com.safeguard.data.di;

import com.safeguard.core.domain.repository.ThreatFeedCursorStore;
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
public final class DatabaseModule_ProvideThreatFeedCursorStoreFactory implements Factory<ThreatFeedCursorStore> {
  private final Provider<SecurePreferencesManager> prefsProvider;

  public DatabaseModule_ProvideThreatFeedCursorStoreFactory(
      Provider<SecurePreferencesManager> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public ThreatFeedCursorStore get() {
    return provideThreatFeedCursorStore(prefsProvider.get());
  }

  public static DatabaseModule_ProvideThreatFeedCursorStoreFactory create(
      Provider<SecurePreferencesManager> prefsProvider) {
    return new DatabaseModule_ProvideThreatFeedCursorStoreFactory(prefsProvider);
  }

  public static ThreatFeedCursorStore provideThreatFeedCursorStore(SecurePreferencesManager prefs) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideThreatFeedCursorStore(prefs));
  }
}
