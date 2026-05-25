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
public final class PeriodicDeepSweepWorker_AssistedFactory_Impl implements PeriodicDeepSweepWorker_AssistedFactory {
  private final PeriodicDeepSweepWorker_Factory delegateFactory;

  PeriodicDeepSweepWorker_AssistedFactory_Impl(PeriodicDeepSweepWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public PeriodicDeepSweepWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<PeriodicDeepSweepWorker_AssistedFactory> create(
      PeriodicDeepSweepWorker_Factory delegateFactory) {
    return InstanceFactory.create(new PeriodicDeepSweepWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<PeriodicDeepSweepWorker_AssistedFactory> createFactoryProvider(
      PeriodicDeepSweepWorker_Factory delegateFactory) {
    return InstanceFactory.create(new PeriodicDeepSweepWorker_AssistedFactory_Impl(delegateFactory));
  }
}
