package com.safeguard.di;

import com.safeguard.core.domain.layer.ProtectionLayer;
import com.safeguard.core.domain.security.DeviceIntegrityProvider;
import com.safeguard.core.orchestration.ForensicReasoningEngine;
import com.safeguard.core.orchestration.RiskAssessmentEngine;
import com.safeguard.core.orchestration.ScanOrchestrator;
import com.safeguard.core.orchestration.ZeroTrustDecisionEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.util.List;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SecurityModule_ProvideScanOrchestratorFactory implements Factory<ScanOrchestrator> {
  private final Provider<List<ProtectionLayer>> layersProvider;

  private final Provider<ZeroTrustDecisionEngine> decisionEngineProvider;

  private final Provider<RiskAssessmentEngine> riskEngineProvider;

  private final Provider<DeviceIntegrityProvider> integrityProvider;

  private final Provider<ForensicReasoningEngine> forensicEngineProvider;

  public SecurityModule_ProvideScanOrchestratorFactory(
      Provider<List<ProtectionLayer>> layersProvider,
      Provider<ZeroTrustDecisionEngine> decisionEngineProvider,
      Provider<RiskAssessmentEngine> riskEngineProvider,
      Provider<DeviceIntegrityProvider> integrityProvider,
      Provider<ForensicReasoningEngine> forensicEngineProvider) {
    this.layersProvider = layersProvider;
    this.decisionEngineProvider = decisionEngineProvider;
    this.riskEngineProvider = riskEngineProvider;
    this.integrityProvider = integrityProvider;
    this.forensicEngineProvider = forensicEngineProvider;
  }

  @Override
  public ScanOrchestrator get() {
    return provideScanOrchestrator(layersProvider.get(), decisionEngineProvider.get(), riskEngineProvider.get(), integrityProvider.get(), forensicEngineProvider.get());
  }

  public static SecurityModule_ProvideScanOrchestratorFactory create(
      Provider<List<ProtectionLayer>> layersProvider,
      Provider<ZeroTrustDecisionEngine> decisionEngineProvider,
      Provider<RiskAssessmentEngine> riskEngineProvider,
      Provider<DeviceIntegrityProvider> integrityProvider,
      Provider<ForensicReasoningEngine> forensicEngineProvider) {
    return new SecurityModule_ProvideScanOrchestratorFactory(layersProvider, decisionEngineProvider, riskEngineProvider, integrityProvider, forensicEngineProvider);
  }

  public static ScanOrchestrator provideScanOrchestrator(List<ProtectionLayer> layers,
      ZeroTrustDecisionEngine decisionEngine, RiskAssessmentEngine riskEngine,
      DeviceIntegrityProvider integrityProvider, ForensicReasoningEngine forensicEngine) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideScanOrchestrator(layers, decisionEngine, riskEngine, integrityProvider, forensicEngine));
  }
}
