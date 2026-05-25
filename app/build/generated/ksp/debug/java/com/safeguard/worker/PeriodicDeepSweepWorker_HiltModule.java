package com.safeguard.worker;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = PeriodicDeepSweepWorker.class
)
public interface PeriodicDeepSweepWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.safeguard.worker.PeriodicDeepSweepWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(
      PeriodicDeepSweepWorker_AssistedFactory factory);
}
