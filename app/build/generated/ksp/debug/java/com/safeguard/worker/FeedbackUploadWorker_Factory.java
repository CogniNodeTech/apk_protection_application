package com.safeguard.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.safeguard.core.domain.feedback.FeedbackPrivacyGate;
import com.safeguard.core.domain.repository.ScanFeedbackRepository;
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
public final class FeedbackUploadWorker_Factory {
  private final Provider<ScanFeedbackRepository> repositoryProvider;

  private final Provider<FeedbackPrivacyGate> privacyGateProvider;

  public FeedbackUploadWorker_Factory(Provider<ScanFeedbackRepository> repositoryProvider,
      Provider<FeedbackPrivacyGate> privacyGateProvider) {
    this.repositoryProvider = repositoryProvider;
    this.privacyGateProvider = privacyGateProvider;
  }

  public FeedbackUploadWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, repositoryProvider.get(), privacyGateProvider.get());
  }

  public static FeedbackUploadWorker_Factory create(
      Provider<ScanFeedbackRepository> repositoryProvider,
      Provider<FeedbackPrivacyGate> privacyGateProvider) {
    return new FeedbackUploadWorker_Factory(repositoryProvider, privacyGateProvider);
  }

  public static FeedbackUploadWorker newInstance(Context context, WorkerParameters params,
      ScanFeedbackRepository repository, FeedbackPrivacyGate privacyGate) {
    return new FeedbackUploadWorker(context, params, repository, privacyGate);
  }
}
