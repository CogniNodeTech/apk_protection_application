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
  public PeriodicDeepSweepWorker create(Context arg0, WorkerParameters arg1) {
    return delegateFactory.get(arg0, arg1);
  }

  public static Provider<PeriodicDeepSweepWorker_AssistedFactory> create(
      PeriodicDeepSweepWorker_Factory delegateFactory) {
    return InstanceFactory.create(new PeriodicDeepSweepWorker_AssistedFactory_Impl(delegateFactory));
  }
}
