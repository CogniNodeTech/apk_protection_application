package com.safeguard.di

import android.content.Context
import com.safeguard.BuildConfig
import com.safeguard.core.domain.integrity.PlayIntegrityChecker
import com.safeguard.core.domain.layer.ProtectionLayer
import com.safeguard.core.orchestration.RiskAssessmentEngine
import com.safeguard.core.orchestration.ScanOrchestrator
import com.safeguard.core.orchestration.ZeroTrustDecisionEngine
import com.safeguard.core.domain.security.DeviceIntegrityProvider
import com.safeguard.mlmodel.FeatureExtractor
import com.safeguard.mlmodel.TFLiteRunner
import com.safeguard.security.layers.layer1.Layer1FileMonitor
import com.safeguard.security.layers.layer2.HashValidator
import com.safeguard.security.layers.layer3.PermissionAnalyzer
import com.safeguard.security.layers.layer4.SignatureValidator
import com.safeguard.security.layers.layer5.MLAnalyzer
import com.safeguard.data.local.preferences.SecurePreferencesManager
import com.safeguard.security.layers.layer6.CloudVerifier
import com.safeguard.security.layers.layer7.YaraRuleSet
import com.safeguard.security.layers.layer7.YaraScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import com.safeguard.security.rasp.RASPEngine
import com.safeguard.mlmodel.ModelDecryptor
import javax.inject.Named
import javax.inject.Singleton
import kotlin.jvm.JvmSuppressWildcards

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideZeroTrustDecisionEngine(): ZeroTrustDecisionEngine = ZeroTrustDecisionEngine()

    @Provides
    @Singleton
    fun provideRiskAssessmentEngine(): RiskAssessmentEngine = RiskAssessmentEngine()

    @Provides
    @Singleton
    fun provideProtectionLayers(
        layer1: Layer1FileMonitor,
        layer2: HashValidator,
        layer3: PermissionAnalyzer,
        layer4: SignatureValidator,
        layer5: MLAnalyzer,
        layer7: YaraScanner,
        layer6: CloudVerifier
    ): List<@JvmSuppressWildcards ProtectionLayer> =
        // Layer 7 runs *before* Layer 6 so cloud verification can incorporate Layer 7's
        // verdict via `previousLayerResults` (and so a high-confidence local content hit
        // can short-circuit the cloud round-trip via the orchestrator's early-stop).
        listOf(layer1, layer2, layer3, layer4, layer5, layer7, layer6)

    @Provides
    @Singleton
    fun provideForensicReasoningEngine(): com.safeguard.core.orchestration.ForensicReasoningEngine = 
        com.safeguard.core.orchestration.ForensicReasoningEngine()

    @Provides
    @Singleton
    fun provideScanOrchestrator(
        layers: List<@JvmSuppressWildcards ProtectionLayer>,
        decisionEngine: ZeroTrustDecisionEngine,
        riskEngine: RiskAssessmentEngine,
        integrityProvider: DeviceIntegrityProvider,
        forensicEngine: com.safeguard.core.orchestration.ForensicReasoningEngine
    ): ScanOrchestrator = ScanOrchestrator(layers, decisionEngine, riskEngine, integrityProvider, forensicEngine)

    @Provides
    fun provideLayer1(): Layer1FileMonitor = Layer1FileMonitor()

    @Provides
    fun provideLayer2(threatDb: com.safeguard.core.domain.repository.ThreatDatabaseRepository): HashValidator =
        HashValidator(threatDb)

    @Provides
    fun provideLayer3(): PermissionAnalyzer = PermissionAnalyzer()

    @Provides
    @Named("signature_blacklist")
    fun provideSignatureBlacklist(@ApplicationContext context: Context): Set<String> {
        return try {
            context.assets.open("signature_blacklist.txt").bufferedReader().use { reader ->
                reader.readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .toSet()
            }
        } catch (_: Exception) {
            emptySet()
        }
    }

    @Provides
    fun provideLayer4(@Named("signature_blacklist") blacklist: Set<String>): SignatureValidator =
        SignatureValidator(blacklist)

    @Provides
    fun provideLayer5(
        featureExtractor: FeatureExtractor,
        tfliteRunner: TFLiteRunner
    ): MLAnalyzer = MLAnalyzer(featureExtractor, tfliteRunner)

    @Provides
    fun provideLayer6(
        cloudRepo: com.safeguard.core.domain.repository.CloudVerificationRepository,
        preferences: SecurePreferencesManager,
        playIntegrityChecker: PlayIntegrityChecker,
        @ApplicationContext context: Context
    ): CloudVerifier {
        val version = android.os.Build.VERSION.SDK_INT
        val locale = context.resources.configuration.locales[0]?.toString() ?: "en-US"
        return CloudVerifier(cloudRepo, version, locale, preferences, playIntegrityChecker)
    }

    @Provides
    @Singleton
    fun provideYaraRuleSet(@ApplicationContext context: Context): YaraRuleSet =
        // Loaded once at process startup; rules are immutable so we can reuse the same
        // [YaraScanner] instance across every scan without rebuilding the matcher index.
        YaraRuleSet.fromAssets(context)

    @Provides
    fun provideLayer7(ruleSet: YaraRuleSet): YaraScanner = YaraScanner(ruleSet)

    @Provides
    @Singleton
    fun provideFeatureExtractor(): FeatureExtractor = FeatureExtractor()

    @Provides
    @Singleton
    fun provideDeviceIntegrityProvider(@ApplicationContext context: Context): DeviceIntegrityProvider =
        RASPEngine(context, BuildConfig.APP_SIGNING_CERT_SHA256)

    @Provides
    @Singleton
    fun provideModelDecryptor(@ApplicationContext context: Context): ModelDecryptor = ModelDecryptor(context)

    @Provides
    @Singleton
    fun provideTFLiteRunner(
        @ApplicationContext context: Context,
        modelDecryptor: ModelDecryptor
    ): TFLiteRunner = TFLiteRunner(context, modelDecryptor)
}
