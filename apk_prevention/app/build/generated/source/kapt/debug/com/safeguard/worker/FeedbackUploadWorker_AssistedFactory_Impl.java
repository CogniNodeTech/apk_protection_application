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
public final class FeedbackUploadWorker_AssistedFactory_Impl implements FeedbackUploadWorker_AssistedFactory {
  private final FeedbackUploadWorker_Factory delegateFactory;

  FeedbackUploadWorker_AssistedFactory_Impl(FeedbackUploadWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public FeedbackUploadWorker create(Context arg0, WorkerParameters arg1) {
    return delegateFactory.get(arg0, arg1);
  }

  public static Provider<FeedbackUploadWorker_AssistedFactory> create(
      FeedbackUploadWorker_Factory delegateFactory) {
    return InstanceFactory.create(new FeedbackUploadWorker_AssistedFactory_Impl(delegateFactory));
  }
}
