package com.safeguard.di;

import android.content.Context;
import com.safeguard.BuildConfig;
import com.safeguard.core.domain.integrity.PlayIntegrityChecker;
import com.safeguard.core.domain.layer.ProtectionLayer;
import com.safeguard.core.orchestration.RiskAssessmentEngine;
import com.safeguard.core.orchestration.ScanOrchestrator;
import com.safeguard.core.orchestration.ZeroTrustDecisionEngine;
import com.safeguard.core.domain.security.DeviceIntegrityProvider;
import com.safeguard.mlmodel.FeatureExtractor;
import com.safeguard.mlmodel.TFLiteRunner;
import com.safeguard.security.layers.layer1.Layer1FileMonitor;
import com.safeguard.security.layers.layer2.HashValidator;
import com.safeguard.security.layers.layer3.PermissionAnalyzer;
import com.safeguard.security.layers.layer4.SignatureValidator;
import com.safeguard.security.layers.layer5.MLAnalyzer;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.security.layers.layer6.CloudVerifier;
import com.safeguard.security.layers.layer7.YaraRuleSet;
import com.safeguard.security.layers.layer7.YaraScanner;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import com.safeguard.security.rasp.RASPEngine;
import com.safeguard.mlmodel.ModelDecryptor;
import javax.inject.Named;
import javax.inject.Singleton;
import kotlin.jvm.JvmSuppressWildcards;

@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00aa\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007J\b\u0010\u0007\u001a\u00020\bH\u0007J\b\u0010\t\u001a\u00020\nH\u0007J\b\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u0007J\b\u0010\u0011\u001a\u00020\u0012H\u0007J\u0018\u0010\u0013\u001a\u00020\u00142\u000e\b\u0001\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00170\u0016H\u0007J\u0018\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\b2\u0006\u0010\u001b\u001a\u00020\u001cH\u0007J*\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020$2\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(H\u0007J\u0012\u0010)\u001a\u00020*2\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007JK\u0010+\u001a\r\u0012\t\u0012\u00070-\u00a2\u0006\u0002\b.0,2\u0006\u0010/\u001a\u00020\f2\u0006\u00100\u001a\u00020\u000e2\u0006\u00101\u001a\u00020\u00122\u0006\u00102\u001a\u00020\u00142\u0006\u00103\u001a\u00020\u00192\u0006\u00104\u001a\u00020&2\u0006\u00105\u001a\u00020\u001eH\u0007J\b\u00106\u001a\u000207H\u0007J;\u00108\u001a\u0002092\u0011\u0010:\u001a\r\u0012\t\u0012\u00070-\u00a2\u0006\u0002\b.0,2\u0006\u0010;\u001a\u00020<2\u0006\u0010=\u001a\u0002072\u0006\u0010>\u001a\u00020\u00042\u0006\u0010?\u001a\u00020\nH\u0007J\u0018\u0010@\u001a\b\u0012\u0004\u0012\u00020\u00170\u00162\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007J\u001a\u0010A\u001a\u00020\u001c2\b\b\u0001\u0010\u0005\u001a\u00020\u00062\u0006\u0010B\u001a\u00020*H\u0007J\u0012\u0010C\u001a\u00020(2\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007J\b\u0010D\u001a\u00020<H\u0007\u00a8\u0006E"}, d2 = {"Lcom/safeguard/di/SecurityModule;", "", "()V", "provideDeviceIntegrityProvider", "Lcom/safeguard/core/domain/security/DeviceIntegrityProvider;", "context", "Landroid/content/Context;", "provideFeatureExtractor", "Lcom/safeguard/mlmodel/FeatureExtractor;", "provideForensicReasoningEngine", "Lcom/safeguard/core/orchestration/ForensicReasoningEngine;", "provideLayer1", "Lcom/safeguard/security/layers/layer1/Layer1FileMonitor;", "provideLayer2", "Lcom/safeguard/security/layers/layer2/HashValidator;", "threatDb", "Lcom/safeguard/core/domain/repository/ThreatDatabaseRepository;", "provideLayer3", "Lcom/safeguard/security/layers/layer3/PermissionAnalyzer;", "provideLayer4", "Lcom/safeguard/security/layers/layer4/SignatureValidator;", "blacklist", "", "", "provideLayer5", "Lcom/safeguard/security/layers/layer5/MLAnalyzer;", "featureExtractor", "tfliteRunner", "Lcom/safeguard/mlmodel/TFLiteRunner;", "provideLayer6", "Lcom/safeguard/security/layers/layer6/CloudVerifier;", "cloudRepo", "Lcom/safeguard/core/domain/repository/CloudVerificationRepository;", "preferences", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "playIntegrityChecker", "Lcom/safeguard/core/domain/integrity/PlayIntegrityChecker;", "provideLayer7", "Lcom/safeguard/security/layers/layer7/YaraScanner;", "ruleSet", "Lcom/safeguard/security/layers/layer7/YaraRuleSet;", "provideModelDecryptor", "Lcom/safeguard/mlmodel/ModelDecryptor;", "provideProtectionLayers", "", "Lcom/safeguard/core/domain/layer/ProtectionLayer;", "Lkotlin/jvm/JvmSuppressWildcards;", "layer1", "layer2", "layer3", "layer4", "layer5", "layer7", "layer6", "provideRiskAssessmentEngine", "Lcom/safeguard/core/orchestration/RiskAssessmentEngine;", "provideScanOrchestrator", "Lcom/safeguard/core/orchestration/ScanOrchestrator;", "layers", "decisionEngine", "Lcom/safeguard/core/orchestration/ZeroTrustDecisionEngine;", "riskEngine", "integrityProvider", "forensicEngine", "provideSignatureBlacklist", "provideTFLiteRunner", "modelDecryptor", "provideYaraRuleSet", "provideZeroTrustDecisionEngine", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class SecurityModule {
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.di.SecurityModule INSTANCE = null;
    
    private SecurityModule() {
        super();
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.orchestration.ZeroTrustDecisionEngine provideZeroTrustDecisionEngine() {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.orchestration.RiskAssessmentEngine provideRiskAssessmentEngine() {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.safeguard.core.domain.layer.ProtectionLayer> provideProtectionLayers(@org.jetbrains.annotations.NotNull
    com.safeguard.security.layers.layer1.Layer1FileMonitor layer1, @org.jetbrains.annotations.NotNull
    com.safeguard.security.layers.layer2.HashValidator layer2, @org.jetbrains.annotations.NotNull
    com.safeguard.security.layers.layer3.PermissionAnalyzer layer3, @org.jetbrains.annotations.NotNull
    com.safeguard.security.layers.layer4.SignatureValidator layer4, @org.jetbrains.annotations.NotNull
    com.safeguard.security.layers.layer5.MLAnalyzer layer5, @org.jetbrains.annotations.NotNull
    com.safeguard.security.layers.layer7.YaraScanner layer7, @org.jetbrains.annotations.NotNull
    com.safeguard.security.layers.layer6.CloudVerifier layer6) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.orchestration.ForensicReasoningEngine provideForensicReasoningEngine() {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.orchestration.ScanOrchestrator provideScanOrchestrator(@org.jetbrains.annotations.NotNull
    java.util.List<com.safeguard.core.domain.layer.ProtectionLayer> layers, @org.jetbrains.annotations.NotNull
    com.safeguard.core.orchestration.ZeroTrustDecisionEngine decisionEngine, @org.jetbrains.annotations.NotNull
    com.safeguard.core.orchestration.RiskAssessmentEngine riskEngine, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.security.DeviceIntegrityProvider integrityProvider, @org.jetbrains.annotations.NotNull
    com.safeguard.core.orchestration.ForensicReasoningEngine forensicEngine) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.security.layers.layer1.Layer1FileMonitor provideLayer1() {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.security.layers.layer2.HashValidator provideLayer2(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ThreatDatabaseRepository threatDb) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.security.layers.layer3.PermissionAnalyzer provideLayer3() {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Named(value = "signature_blacklist")
    @org.jetbrains.annotations.NotNull
    public final java.util.Set<java.lang.String> provideSignatureBlacklist(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.security.layers.layer4.SignatureValidator provideLayer4(@javax.inject.Named(value = "signature_blacklist")
    @org.jetbrains.annotations.NotNull
    java.util.Set<java.lang.String> blacklist) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.security.layers.layer5.MLAnalyzer provideLayer5(@org.jetbrains.annotations.NotNull
    com.safeguard.mlmodel.FeatureExtractor featureExtractor, @org.jetbrains.annotations.NotNull
    com.safeguard.mlmodel.TFLiteRunner tfliteRunner) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.security.layers.layer6.CloudVerifier provideLayer6(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.CloudVerificationRepository cloudRepo, @org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager preferences, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.integrity.PlayIntegrityChecker playIntegrityChecker, @dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.security.layers.layer7.YaraRuleSet provideYaraRuleSet(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.security.layers.layer7.YaraScanner provideLayer7(@org.jetbrains.annotations.NotNull
    com.safeguard.security.layers.layer7.YaraRuleSet ruleSet) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.mlmodel.FeatureExtractor provideFeatureExtractor() {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.security.DeviceIntegrityProvider provideDeviceIntegrityProvider(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.mlmodel.ModelDecryptor provideModelDecryptor(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.mlmodel.TFLiteRunner provideTFLiteRunner(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.safeguard.mlmodel.ModelDecryptor modelDecryptor) {
        return null;
    }
}