package com.safeguard.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
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
public final class ScheduledScanWorker_Factory {
  private final Provider<SecurePreferencesManager> preferencesProvider;

  public ScheduledScanWorker_Factory(Provider<SecurePreferencesManager> preferencesProvider) {
    this.preferencesProvider = preferencesProvider;
  }

  public ScheduledScanWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, preferencesProvider.get());
  }

  public static ScheduledScanWorker_Factory create(
      Provider<SecurePreferencesManager> preferencesProvider) {
    return new ScheduledScanWorker_Factory(preferencesProvider);
  }

  public static ScheduledScanWorker newInstance(Context context, WorkerParameters params,
      SecurePreferencesManager preferences) {
    return new ScheduledScanWorker(context, params, preferences);
  }
}
