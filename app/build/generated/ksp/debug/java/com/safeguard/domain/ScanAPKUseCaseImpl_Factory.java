package com.safeguard.domain;

import android.content.Context;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.core.domain.telemetry.ScanTelemetry;
import com.safeguard.core.orchestration.ScanOrchestrator;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ScanAPKUseCaseImpl_Factory implements Factory<ScanAPKUseCaseImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<ScanOrchestrator> orchestratorProvider;

  private final Provider<ScanRepository> scanRepositoryProvider;

  private final Provider<ScanTelemetry> telemetryProvider;

  public ScanAPKUseCaseImpl_Factory(Provider<Context> contextProvider,
      Provider<ScanOrchestrator> orchestratorProvider,
      Provider<ScanRepository> scanRepositoryProvider, Provider<ScanTelemetry> telemetryProvider) {
    this.contextProvider = contextProvider;
    this.orchestratorProvider = orchestratorProvider;
    this.scanRepositoryProvider = scanRepositoryProvider;
    this.telemetryProvider = telemetryProvider;
  }

  @Override
  public ScanAPKUseCaseImpl get() {
    return newInstance(contextProvider.get(), orchestratorProvider.get(), scanRepositoryProvider.get(), telemetryProvider.get());
  }

  public static ScanAPKUseCaseImpl_Factory create(Provider<Context> contextProvider,
      Provider<ScanOrchestrator> orchestratorProvider,
      Provider<ScanRepository> scanRepositoryProvider, Provider<ScanTelemetry> telemetryProvider) {
    return new ScanAPKUseCaseImpl_Factory(contextProvider, orchestratorProvider, scanRepositoryProvider, telemetryProvider);
  }

  public static ScanAPKUseCaseImpl newInstance(Context context, ScanOrchestrator orchestrator,
      ScanRepository scanRepository, ScanTelemetry telemetry) {
    return new ScanAPKUseCaseImpl(context, orchestrator, scanRepository, telemetry);
  }
}
