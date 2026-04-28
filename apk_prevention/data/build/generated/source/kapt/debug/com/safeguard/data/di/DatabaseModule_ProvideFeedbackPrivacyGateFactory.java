package com.safeguard.data.di;

import com.safeguard.core.domain.feedback.FeedbackPrivacyGate;
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
public final class DatabaseModule_ProvideFeedbackPrivacyGateFactory implements Factory<FeedbackPrivacyGate> {
  private final Provider<SecurePreferencesManager> prefsProvider;

  public DatabaseModule_ProvideFeedbackPrivacyGateFactory(
      Provider<SecurePreferencesManager> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public FeedbackPrivacyGate get() {
    return provideFeedbackPrivacyGate(prefsProvider.get());
  }

  public static DatabaseModule_ProvideFeedbackPrivacyGateFactory create(
      Provider<SecurePreferencesManager> prefsProvider) {
    return new DatabaseModule_ProvideFeedbackPrivacyGateFactory(prefsProvider);
  }

  public static FeedbackPrivacyGate provideFeedbackPrivacyGate(SecurePreferencesManager prefs) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideFeedbackPrivacyGate(prefs));
  }
}
