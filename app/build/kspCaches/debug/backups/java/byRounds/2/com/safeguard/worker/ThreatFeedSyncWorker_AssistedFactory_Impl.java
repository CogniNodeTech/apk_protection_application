package com.safeguard.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ThreatFeedSyncWorker_AssistedFactory_Impl implements ThreatFeedSyncWorker_AssistedFactory {
  private final ThreatFeedSyncWorker_Factory delegateFactory;

  ThreatFeedSyncWorker_AssistedFactory_Impl(ThreatFeedSyncWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public ThreatFeedSyncWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<ThreatFeedSyncWorker_AssistedFactory> create(
      ThreatFeedSyncWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ThreatFeedSyncWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<ThreatFeedSyncWorker_AssistedFactory> createFactoryProvider(
      ThreatFeedSyncWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ThreatFeedSyncWorker_AssistedFactory_Impl(delegateFactory));
  }
}
