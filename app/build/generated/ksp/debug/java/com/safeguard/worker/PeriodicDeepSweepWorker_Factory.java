package com.safeguard.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class PeriodicDeepSweepWorker_Factory {
  public PeriodicDeepSweepWorker_Factory() {
  }

  public PeriodicDeepSweepWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params);
  }

  public static PeriodicDeepSweepWorker_Factory create() {
    return new PeriodicDeepSweepWorker_Factory();
  }

  public static PeriodicDeepSweepWorker newInstance(Context context, WorkerParameters params) {
    return new PeriodicDeepSweepWorker(context, params);
  }
}
