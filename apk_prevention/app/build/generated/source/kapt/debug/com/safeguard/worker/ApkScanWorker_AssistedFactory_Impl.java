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
public final class ApkScanWorker_AssistedFactory_Impl implements ApkScanWorker_AssistedFactory {
  private final ApkScanWorker_Factory delegateFactory;

  ApkScanWorker_AssistedFactory_Impl(ApkScanWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public ApkScanWorker create(Context arg0, WorkerParameters arg1) {
    return delegateFactory.get(arg0, arg1);
  }

  public static Provider<ApkScanWorker_AssistedFactory> create(
      ApkScanWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ApkScanWorker_AssistedFactory_Impl(delegateFactory));
  }
}
