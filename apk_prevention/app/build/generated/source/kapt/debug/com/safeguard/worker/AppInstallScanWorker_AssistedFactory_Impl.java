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
public final class AppInstallScanWorker_AssistedFactory_Impl implements AppInstallScanWorker_AssistedFactory {
  private final AppInstallScanWorker_Factory delegateFactory;

  AppInstallScanWorker_AssistedFactory_Impl(AppInstallScanWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public AppInstallScanWorker create(Context arg0, WorkerParameters arg1) {
    return delegateFactory.get(arg0, arg1);
  }

  public static Provider<AppInstallScanWorker_AssistedFactory> create(
      AppInstallScanWorker_Factory delegateFactory) {
    return InstanceFactory.create(new AppInstallScanWorker_AssistedFactory_Impl(delegateFactory));
  }
}
