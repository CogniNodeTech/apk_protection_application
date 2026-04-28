package com.safeguard;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.HiltWrapper_WorkerFactoryModule;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.safeguard.core.domain.crash.CrashReporter;
import com.safeguard.core.domain.feedback.FeedbackPrivacyGate;
import com.safeguard.core.domain.integrity.NoOpPlayIntegrityChecker;
import com.safeguard.core.domain.integrity.PlayIntegrityChecker;
import com.safeguard.core.domain.integrity.PlayIntegrityConfig;
import com.safeguard.core.domain.layer.ProtectionLayer;
import com.safeguard.core.domain.repository.ThreatFeedCursorStore;
import com.safeguard.core.domain.repository.ThreatFeedSigningConfig;
import com.safeguard.core.domain.repository.ThreatFeedStatusStore;
import com.safeguard.core.domain.security.DeviceIntegrityProvider;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
import com.safeguard.core.domain.usecase.ScanAPKUseCase;
import com.safeguard.core.orchestration.ForensicReasoningEngine;
import com.safeguard.core.orchestration.RiskAssessmentEngine;
import com.safeguard.core.orchestration.ScanOrchestrator;
import com.safeguard.core.orchestration.ZeroTrustDecisionEngine;
import com.safeguard.crash.FileCrashReporter;
import com.safeguard.data.di.DatabaseModule;
import com.safeguard.data.di.DatabaseModule_ProvideAuditLogDaoFactory;
import com.safeguard.data.di.DatabaseModule_ProvideDatabaseFactory;
import com.safeguard.data.di.DatabaseModule_ProvideDbPassphraseFactory;
import com.safeguard.data.di.DatabaseModule_ProvideDeletedApkDaoFactory;
import com.safeguard.data.di.DatabaseModule_ProvideFeedbackPrivacyGateFactory;
import com.safeguard.data.di.DatabaseModule_ProvideMalwareSignatureDaoFactory;
import com.safeguard.data.di.DatabaseModule_ProvideQuarantineDaoFactory;
import com.safeguard.data.di.DatabaseModule_ProvideQuarantineDirFactory;
import com.safeguard.data.di.DatabaseModule_ProvideScanFeedbackEventDaoFactory;
import com.safeguard.data.di.DatabaseModule_ProvideScanHistoryDaoFactory;
import com.safeguard.data.di.DatabaseModule_ProvideSecurePreferencesManagerFactory;
import com.safeguard.data.di.DatabaseModule_ProvideThreatFeedCursorStoreFactory;
import com.safeguard.data.di.DatabaseModule_ProvideThreatFeedStatusStoreFactory;
import com.safeguard.data.di.DatabaseModule_ProvideTrustedAppDaoFactory;
import com.safeguard.data.di.NetworkModule;
import com.safeguard.data.di.NetworkModule_ProvideAuthApiServiceFactory;
import com.safeguard.data.di.NetworkModule_ProvideMoshiFactory;
import com.safeguard.data.di.NetworkModule_ProvideOkHttpClientFactory;
import com.safeguard.data.di.NetworkModule_ProvideRetrofitFactory;
import com.safeguard.data.di.NetworkModule_ProvideThreatIntelligenceApiFactory;
import com.safeguard.data.local.database.SafeGuardDatabase;
import com.safeguard.data.local.database.dao.AuditLogDao;
import com.safeguard.data.local.database.dao.DeletedApkDao;
import com.safeguard.data.local.database.dao.MalwareSignatureDao;
import com.safeguard.data.local.database.dao.QuarantineDao;
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao;
import com.safeguard.data.local.database.dao.ScanHistoryDao;
import com.safeguard.data.local.database.dao.TrustedAppDao;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.data.remote.api.AuthApiService;
import com.safeguard.data.remote.api.ThreatIntelligenceApi;
import com.safeguard.data.remote.signing.Ed25519ThreatFeedVerifier;
import com.safeguard.data.repository.AuthRepository;
import com.safeguard.data.repository.CloudVerificationRepositoryImpl;
import com.safeguard.data.repository.InstalledAppsRepositoryImpl;
import com.safeguard.data.repository.QuarantineRepositoryImpl;
import com.safeguard.data.repository.ScanFeedbackRepositoryImpl;
import com.safeguard.data.repository.ScanRepositoryImpl;
import com.safeguard.data.repository.ThreatDatabaseRepositoryImpl;
import com.safeguard.data.repository.ThreatFeedRepositoryImpl;
import com.safeguard.di.NetworkConfigModule;
import com.safeguard.di.NetworkConfigModule_ProvideBaseUrlFactory;
import com.safeguard.di.NetworkConfigModule_ProvideThreatFeedSigningConfigFactory;
import com.safeguard.di.PlayIntegrityModule;
import com.safeguard.di.PlayIntegrityModule_ProvidePlayIntegrityCheckerFactory;
import com.safeguard.di.PlayIntegrityModule_ProvidePlayIntegrityConfigFactory;
import com.safeguard.di.SecurityModule;
import com.safeguard.di.SecurityModule_ProvideDeviceIntegrityProviderFactory;
import com.safeguard.di.SecurityModule_ProvideFeatureExtractorFactory;
import com.safeguard.di.SecurityModule_ProvideForensicReasoningEngineFactory;
import com.safeguard.di.SecurityModule_ProvideLayer1Factory;
import com.safeguard.di.SecurityModule_ProvideLayer2Factory;
import com.safeguard.di.SecurityModule_ProvideLayer3Factory;
import com.safeguard.di.SecurityModule_ProvideLayer4Factory;
import com.safeguard.di.SecurityModule_ProvideLayer5Factory;
import com.safeguard.di.SecurityModule_ProvideLayer6Factory;
import com.safeguard.di.SecurityModule_ProvideLayer7Factory;
import com.safeguard.di.SecurityModule_ProvideModelDecryptorFactory;
import com.safeguard.di.SecurityModule_ProvideProtectionLayersFactory;
import com.safeguard.di.SecurityModule_ProvideRiskAssessmentEngineFactory;
import com.safeguard.di.SecurityModule_ProvideScanOrchestratorFactory;
import com.safeguard.di.SecurityModule_ProvideSignatureBlacklistFactory;
import com.safeguard.di.SecurityModule_ProvideTFLiteRunnerFactory;
import com.safeguard.di.SecurityModule_ProvideYaraRuleSetFactory;
import com.safeguard.di.SecurityModule_ProvideZeroTrustDecisionEngineFactory;
import com.safeguard.domain.QuarantineAPKUseCaseImpl;
import com.safeguard.domain.ScanAPKUseCaseImpl;
import com.safeguard.manager.DeviceScanManager;
import com.safeguard.mlmodel.FeatureExtractor;
import com.safeguard.mlmodel.ModelDecryptor;
import com.safeguard.mlmodel.TFLiteRunner;
import com.safeguard.security.layers.layer2.HashValidator;
import com.safeguard.security.layers.layer4.SignatureValidator;
import com.safeguard.security.layers.layer5.MLAnalyzer;
import com.safeguard.security.layers.layer6.CloudVerifier;
import com.safeguard.security.layers.layer7.YaraRuleSet;
import com.safeguard.security.layers.layer7.YaraScanner;
import com.safeguard.telemetry.NoOpScanTelemetry;
import com.safeguard.telemetry.PrivacyAwareScanTelemetry;
import com.safeguard.ui.MainViewModel;
import com.safeguard.ui.MainViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.ui.screens.auth.AuthViewModel;
import com.safeguard.ui.screens.auth.AuthViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.ui.screens.dashboard.DashboardViewModel;
import com.safeguard.ui.screens.dashboard.DashboardViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.ui.screens.detailedanalysis.DetailedAnalysisViewModel;
import com.safeguard.ui.screens.detailedanalysis.DetailedAnalysisViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.ui.screens.history.HistoryViewModel;
import com.safeguard.ui.screens.history.HistoryViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.ui.screens.protectionstatus.ProtectionStatusDetailViewModel;
import com.safeguard.ui.screens.protectionstatus.ProtectionStatusDetailViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.ui.screens.protectionstatus.ProtectionStatusViewModel;
import com.safeguard.ui.screens.protectionstatus.ProtectionStatusViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.ui.screens.quarantine.QuarantineViewModel;
import com.safeguard.ui.screens.quarantine.QuarantineViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.ui.screens.scanprogress.DeviceScanProgressViewModel;
import com.safeguard.ui.screens.scanprogress.DeviceScanProgressViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.ui.screens.scanresults.ScanResultsViewModel;
import com.safeguard.ui.screens.scanresults.ScanResultsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.ui.screens.settings.SettingsViewModel;
import com.safeguard.ui.screens.settings.SettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.ui.theme.ThemeViewModel;
import com.safeguard.ui.theme.ThemeViewModel_HiltModules_KeyModule_ProvideFactory;
import com.safeguard.worker.ApkScanWorker;
import com.safeguard.worker.ApkScanWorker_AssistedFactory;
import com.safeguard.worker.AppInstallScanWorker;
import com.safeguard.worker.AppInstallScanWorker_AssistedFactory;
import com.safeguard.worker.FeedbackUploadWorker;
import com.safeguard.worker.FeedbackUploadWorker_AssistedFactory;
import com.safeguard.worker.PeriodicDeepSweepWorker;
import com.safeguard.worker.PeriodicDeepSweepWorker_AssistedFactory;
import com.safeguard.worker.ScheduledScanWorker;
import com.safeguard.worker.ScheduledScanWorker_AssistedFactory;
import com.safeguard.worker.ThreatFeedSyncWorker;
import com.safeguard.worker.ThreatFeedSyncWorker_AssistedFactory;
import com.squareup.moshi.Moshi;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.flags.HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import dagger.internal.SingleCheck;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerSafeGuardApplication_HiltComponents_SingletonC {
  private DaggerSafeGuardApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder databaseModule(DatabaseModule databaseModule) {
      Preconditions.checkNotNull(databaseModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder hiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule(
        HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule hiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule) {
      Preconditions.checkNotNull(hiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder hiltWrapper_WorkerFactoryModule(
        HiltWrapper_WorkerFactoryModule hiltWrapper_WorkerFactoryModule) {
      Preconditions.checkNotNull(hiltWrapper_WorkerFactoryModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder networkConfigModule(NetworkConfigModule networkConfigModule) {
      Preconditions.checkNotNull(networkConfigModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder networkModule(NetworkModule networkModule) {
      Preconditions.checkNotNull(networkModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder playIntegrityModule(PlayIntegrityModule playIntegrityModule) {
      Preconditions.checkNotNull(playIntegrityModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder securityModule(SecurityModule securityModule) {
      Preconditions.checkNotNull(securityModule);
      return this;
    }

    public SafeGuardApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements SafeGuardApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public SafeGuardApplication_HiltComponents.ActivityRetainedC build() {
      return new ActivityRetainedCImpl(singletonCImpl);
    }
  }

  private static final class ActivityCBuilder implements SafeGuardApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public SafeGuardApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements SafeGuardApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public SafeGuardApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements SafeGuardApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SafeGuardApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements SafeGuardApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SafeGuardApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements SafeGuardApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public SafeGuardApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements SafeGuardApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public SafeGuardApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends SafeGuardApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends SafeGuardApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends SafeGuardApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends SafeGuardApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Set<String> getViewModelKeys() {
      return ImmutableSet.<String>of(AuthViewModel_HiltModules_KeyModule_ProvideFactory.provide(), DashboardViewModel_HiltModules_KeyModule_ProvideFactory.provide(), DetailedAnalysisViewModel_HiltModules_KeyModule_ProvideFactory.provide(), DeviceScanProgressViewModel_HiltModules_KeyModule_ProvideFactory.provide(), HistoryViewModel_HiltModules_KeyModule_ProvideFactory.provide(), MainViewModel_HiltModules_KeyModule_ProvideFactory.provide(), ProtectionStatusDetailViewModel_HiltModules_KeyModule_ProvideFactory.provide(), ProtectionStatusViewModel_HiltModules_KeyModule_ProvideFactory.provide(), QuarantineViewModel_HiltModules_KeyModule_ProvideFactory.provide(), ScanResultsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), SettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), ThemeViewModel_HiltModules_KeyModule_ProvideFactory.provide());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends SafeGuardApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<DashboardViewModel> dashboardViewModelProvider;

    private Provider<DetailedAnalysisViewModel> detailedAnalysisViewModelProvider;

    private Provider<DeviceScanProgressViewModel> deviceScanProgressViewModelProvider;

    private Provider<HistoryViewModel> historyViewModelProvider;

    private Provider<MainViewModel> mainViewModelProvider;

    private Provider<ProtectionStatusDetailViewModel> protectionStatusDetailViewModelProvider;

    private Provider<ProtectionStatusViewModel> protectionStatusViewModelProvider;

    private Provider<QuarantineViewModel> quarantineViewModelProvider;

    private Provider<ScanResultsViewModel> scanResultsViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<ThemeViewModel> themeViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.dashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.detailedAnalysisViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.deviceScanProgressViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.historyViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.mainViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.protectionStatusDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.protectionStatusViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.quarantineViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.scanResultsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.themeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
    }

    @Override
    public Map<String, Provider<ViewModel>> getHiltViewModelMap() {
      return ImmutableMap.<String, Provider<ViewModel>>builderWithExpectedSize(12).put("com.safeguard.ui.screens.auth.AuthViewModel", ((Provider) authViewModelProvider)).put("com.safeguard.ui.screens.dashboard.DashboardViewModel", ((Provider) dashboardViewModelProvider)).put("com.safeguard.ui.screens.detailedanalysis.DetailedAnalysisViewModel", ((Provider) detailedAnalysisViewModelProvider)).put("com.safeguard.ui.screens.scanprogress.DeviceScanProgressViewModel", ((Provider) deviceScanProgressViewModelProvider)).put("com.safeguard.ui.screens.history.HistoryViewModel", ((Provider) historyViewModelProvider)).put("com.safeguard.ui.MainViewModel", ((Provider) mainViewModelProvider)).put("com.safeguard.ui.screens.protectionstatus.ProtectionStatusDetailViewModel", ((Provider) protectionStatusDetailViewModelProvider)).put("com.safeguard.ui.screens.protectionstatus.ProtectionStatusViewModel", ((Provider) protectionStatusViewModelProvider)).put("com.safeguard.ui.screens.quarantine.QuarantineViewModel", ((Provider) quarantineViewModelProvider)).put("com.safeguard.ui.screens.scanresults.ScanResultsViewModel", ((Provider) scanResultsViewModelProvider)).put("com.safeguard.ui.screens.settings.SettingsViewModel", ((Provider) settingsViewModelProvider)).put("com.safeguard.ui.theme.ThemeViewModel", ((Provider) themeViewModelProvider)).build();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.safeguard.ui.screens.auth.AuthViewModel 
          return (T) new AuthViewModel(singletonCImpl.provideSecurePreferencesManagerProvider.get(), singletonCImpl.authRepositoryProvider.get());

          case 1: // com.safeguard.ui.screens.dashboard.DashboardViewModel 
          return (T) new DashboardViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.scanRepositoryImpl(), singletonCImpl.quarantineRepositoryImpl(), singletonCImpl.provideSecurePreferencesManagerProvider.get(), singletonCImpl.bindScanAPKUseCaseProvider.get(), singletonCImpl.bindQuarantineAPKUseCaseProvider.get(), singletonCImpl.deviceScanManagerProvider.get(), singletonCImpl.threatFeedRepositoryImplProvider.get());

          case 2: // com.safeguard.ui.screens.detailedanalysis.DetailedAnalysisViewModel 
          return (T) new DetailedAnalysisViewModel(singletonCImpl.scanRepositoryImpl());

          case 3: // com.safeguard.ui.screens.scanprogress.DeviceScanProgressViewModel 
          return (T) new DeviceScanProgressViewModel(singletonCImpl.deviceScanManagerProvider.get());

          case 4: // com.safeguard.ui.screens.history.HistoryViewModel 
          return (T) new HistoryViewModel(singletonCImpl.scanRepositoryImpl());

          case 5: // com.safeguard.ui.MainViewModel 
          return (T) new MainViewModel(singletonCImpl.provideSecurePreferencesManagerProvider.get());

          case 6: // com.safeguard.ui.screens.protectionstatus.ProtectionStatusDetailViewModel 
          return (T) new ProtectionStatusDetailViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.scanRepositoryImpl(), singletonCImpl.quarantineRepositoryImpl(), singletonCImpl.installedAppsRepositoryImpl());

          case 7: // com.safeguard.ui.screens.protectionstatus.ProtectionStatusViewModel 
          return (T) new ProtectionStatusViewModel(singletonCImpl.scanRepositoryImpl(), singletonCImpl.quarantineRepositoryImpl(), singletonCImpl.installedAppsRepositoryImpl(), singletonCImpl.provideSecurePreferencesManagerProvider.get());

          case 8: // com.safeguard.ui.screens.quarantine.QuarantineViewModel 
          return (T) new QuarantineViewModel(singletonCImpl.quarantineRepositoryImpl());

          case 9: // com.safeguard.ui.screens.scanresults.ScanResultsViewModel 
          return (T) new ScanResultsViewModel(singletonCImpl.scanRepositoryImpl(), singletonCImpl.bindQuarantineAPKUseCaseProvider.get(), singletonCImpl.quarantineRepositoryImpl());

          case 10: // com.safeguard.ui.screens.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.provideSecurePreferencesManagerProvider.get(), singletonCImpl.scanRepositoryImpl(), singletonCImpl.quarantineRepositoryImpl(), singletonCImpl.scanFeedbackRepositoryImplProvider.get(), singletonCImpl.provideMoshiProvider.get());

          case 11: // com.safeguard.ui.theme.ThemeViewModel 
          return (T) new ThemeViewModel(singletonCImpl.provideSecurePreferencesManagerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends SafeGuardApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;

      initialize();

    }

    @SuppressWarnings("unchecked")
    private void initialize() {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends SafeGuardApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends SafeGuardApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<FileCrashReporter> fileCrashReporterProvider;

    private Provider<CrashReporter> bindCrashReporterProvider;

    private Provider<SafeGuardDatabase> provideDatabaseProvider;

    private Provider<FeatureExtractor> provideFeatureExtractorProvider;

    private Provider<ModelDecryptor> provideModelDecryptorProvider;

    private Provider<TFLiteRunner> provideTFLiteRunnerProvider;

    private Provider<YaraRuleSet> provideYaraRuleSetProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Moshi> provideMoshiProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<ThreatIntelligenceApi> provideThreatIntelligenceApiProvider;

    private Provider<SecurePreferencesManager> provideSecurePreferencesManagerProvider;

    private Provider<PlayIntegrityConfig> providePlayIntegrityConfigProvider;

    private Provider<NoOpPlayIntegrityChecker> noOpPlayIntegrityCheckerProvider;

    private Provider<PlayIntegrityChecker> providePlayIntegrityCheckerProvider;

    private Provider<List<ProtectionLayer>> provideProtectionLayersProvider;

    private Provider<ZeroTrustDecisionEngine> provideZeroTrustDecisionEngineProvider;

    private Provider<RiskAssessmentEngine> provideRiskAssessmentEngineProvider;

    private Provider<DeviceIntegrityProvider> provideDeviceIntegrityProvider;

    private Provider<ForensicReasoningEngine> provideForensicReasoningEngineProvider;

    private Provider<ScanOrchestrator> provideScanOrchestratorProvider;

    private Provider<NoOpScanTelemetry> noOpScanTelemetryProvider;

    private Provider<PrivacyAwareScanTelemetry> privacyAwareScanTelemetryProvider;

    private Provider<ScanAPKUseCaseImpl> scanAPKUseCaseImplProvider;

    private Provider<ScanAPKUseCase> bindScanAPKUseCaseProvider;

    private Provider<File> provideQuarantineDirProvider;

    private Provider<QuarantineAPKUseCaseImpl> quarantineAPKUseCaseImplProvider;

    private Provider<QuarantineAPKUseCase> bindQuarantineAPKUseCaseProvider;

    private Provider<ApkScanWorker_AssistedFactory> apkScanWorker_AssistedFactoryProvider;

    private Provider<AppInstallScanWorker_AssistedFactory> appInstallScanWorker_AssistedFactoryProvider;

    private Provider<FeedbackPrivacyGate> provideFeedbackPrivacyGateProvider;

    private Provider<ScanFeedbackRepositoryImpl> scanFeedbackRepositoryImplProvider;

    private Provider<FeedbackUploadWorker_AssistedFactory> feedbackUploadWorker_AssistedFactoryProvider;

    private Provider<PeriodicDeepSweepWorker_AssistedFactory> periodicDeepSweepWorker_AssistedFactoryProvider;

    private Provider<ScheduledScanWorker_AssistedFactory> scheduledScanWorker_AssistedFactoryProvider;

    private Provider<ThreatFeedCursorStore> provideThreatFeedCursorStoreProvider;

    private Provider<ThreatFeedStatusStore> provideThreatFeedStatusStoreProvider;

    private Provider<ThreatFeedSigningConfig> provideThreatFeedSigningConfigProvider;

    private Provider<Ed25519ThreatFeedVerifier> ed25519ThreatFeedVerifierProvider;

    private Provider<ThreatFeedRepositoryImpl> threatFeedRepositoryImplProvider;

    private Provider<ThreatFeedSyncWorker_AssistedFactory> threatFeedSyncWorker_AssistedFactoryProvider;

    private Provider<AuthApiService> provideAuthApiServiceProvider;

    private Provider<AuthRepository> authRepositoryProvider;

    private Provider<DeviceScanManager> deviceScanManagerProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private byte[] namedByteArray() {
      return DatabaseModule_ProvideDbPassphraseFactory.provideDbPassphrase(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    private MalwareSignatureDao malwareSignatureDao() {
      return DatabaseModule_ProvideMalwareSignatureDaoFactory.provideMalwareSignatureDao(provideDatabaseProvider.get());
    }

    private TrustedAppDao trustedAppDao() {
      return DatabaseModule_ProvideTrustedAppDaoFactory.provideTrustedAppDao(provideDatabaseProvider.get());
    }

    private ThreatDatabaseRepositoryImpl threatDatabaseRepositoryImpl() {
      return new ThreatDatabaseRepositoryImpl(malwareSignatureDao(), trustedAppDao());
    }

    private HashValidator hashValidator() {
      return SecurityModule_ProvideLayer2Factory.provideLayer2(threatDatabaseRepositoryImpl());
    }

    private Set<String> namedSetOfString() {
      return SecurityModule_ProvideSignatureBlacklistFactory.provideSignatureBlacklist(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    private SignatureValidator signatureValidator() {
      return SecurityModule_ProvideLayer4Factory.provideLayer4(namedSetOfString());
    }

    private MLAnalyzer mLAnalyzer() {
      return SecurityModule_ProvideLayer5Factory.provideLayer5(provideFeatureExtractorProvider.get(), provideTFLiteRunnerProvider.get());
    }

    private YaraScanner yaraScanner() {
      return SecurityModule_ProvideLayer7Factory.provideLayer7(provideYaraRuleSetProvider.get());
    }

    private CloudVerificationRepositoryImpl cloudVerificationRepositoryImpl() {
      return new CloudVerificationRepositoryImpl(provideThreatIntelligenceApiProvider.get());
    }

    private CloudVerifier cloudVerifier() {
      return SecurityModule_ProvideLayer6Factory.provideLayer6(cloudVerificationRepositoryImpl(), provideSecurePreferencesManagerProvider.get(), providePlayIntegrityCheckerProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    private ScanHistoryDao scanHistoryDao() {
      return DatabaseModule_ProvideScanHistoryDaoFactory.provideScanHistoryDao(provideDatabaseProvider.get());
    }

    private AuditLogDao auditLogDao() {
      return DatabaseModule_ProvideAuditLogDaoFactory.provideAuditLogDao(provideDatabaseProvider.get());
    }

    private ScanRepositoryImpl scanRepositoryImpl() {
      return new ScanRepositoryImpl(scanHistoryDao(), auditLogDao(), provideMoshiProvider.get());
    }

    private QuarantineDao quarantineDao() {
      return DatabaseModule_ProvideQuarantineDaoFactory.provideQuarantineDao(provideDatabaseProvider.get());
    }

    private DeletedApkDao deletedApkDao() {
      return DatabaseModule_ProvideDeletedApkDaoFactory.provideDeletedApkDao(provideDatabaseProvider.get());
    }

    private QuarantineRepositoryImpl quarantineRepositoryImpl() {
      return new QuarantineRepositoryImpl(quarantineDao(), deletedApkDao(), provideQuarantineDirProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    private ScanFeedbackEventDao scanFeedbackEventDao() {
      return DatabaseModule_ProvideScanFeedbackEventDaoFactory.provideScanFeedbackEventDao(provideDatabaseProvider.get());
    }

    private Map<String, Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return ImmutableMap.<String, Provider<WorkerAssistedFactory<? extends ListenableWorker>>>builderWithExpectedSize(6).put("com.safeguard.worker.ApkScanWorker", ((Provider) apkScanWorker_AssistedFactoryProvider)).put("com.safeguard.worker.AppInstallScanWorker", ((Provider) appInstallScanWorker_AssistedFactoryProvider)).put("com.safeguard.worker.FeedbackUploadWorker", ((Provider) feedbackUploadWorker_AssistedFactoryProvider)).put("com.safeguard.worker.PeriodicDeepSweepWorker", ((Provider) periodicDeepSweepWorker_AssistedFactoryProvider)).put("com.safeguard.worker.ScheduledScanWorker", ((Provider) scheduledScanWorker_AssistedFactoryProvider)).put("com.safeguard.worker.ThreatFeedSyncWorker", ((Provider) threatFeedSyncWorker_AssistedFactoryProvider)).build();
    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    private InstalledAppsRepositoryImpl installedAppsRepositoryImpl() {
      return new InstalledAppsRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.fileCrashReporterProvider = new SwitchingProvider<>(singletonCImpl, 0);
      this.bindCrashReporterProvider = DoubleCheck.provider((Provider) fileCrashReporterProvider);
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<SafeGuardDatabase>(singletonCImpl, 5));
      this.provideFeatureExtractorProvider = DoubleCheck.provider(new SwitchingProvider<FeatureExtractor>(singletonCImpl, 6));
      this.provideModelDecryptorProvider = DoubleCheck.provider(new SwitchingProvider<ModelDecryptor>(singletonCImpl, 8));
      this.provideTFLiteRunnerProvider = DoubleCheck.provider(new SwitchingProvider<TFLiteRunner>(singletonCImpl, 7));
      this.provideYaraRuleSetProvider = DoubleCheck.provider(new SwitchingProvider<YaraRuleSet>(singletonCImpl, 9));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 12));
      this.provideMoshiProvider = DoubleCheck.provider(new SwitchingProvider<Moshi>(singletonCImpl, 13));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 11));
      this.provideThreatIntelligenceApiProvider = DoubleCheck.provider(new SwitchingProvider<ThreatIntelligenceApi>(singletonCImpl, 10));
      this.provideSecurePreferencesManagerProvider = DoubleCheck.provider(new SwitchingProvider<SecurePreferencesManager>(singletonCImpl, 14));
      this.providePlayIntegrityConfigProvider = DoubleCheck.provider(new SwitchingProvider<PlayIntegrityConfig>(singletonCImpl, 16));
      this.noOpPlayIntegrityCheckerProvider = DoubleCheck.provider(new SwitchingProvider<NoOpPlayIntegrityChecker>(singletonCImpl, 17));
      this.providePlayIntegrityCheckerProvider = DoubleCheck.provider(new SwitchingProvider<PlayIntegrityChecker>(singletonCImpl, 15));
      this.provideProtectionLayersProvider = DoubleCheck.provider(new SwitchingProvider<List<ProtectionLayer>>(singletonCImpl, 4));
      this.provideZeroTrustDecisionEngineProvider = DoubleCheck.provider(new SwitchingProvider<ZeroTrustDecisionEngine>(singletonCImpl, 18));
      this.provideRiskAssessmentEngineProvider = DoubleCheck.provider(new SwitchingProvider<RiskAssessmentEngine>(singletonCImpl, 19));
      this.provideDeviceIntegrityProvider = DoubleCheck.provider(new SwitchingProvider<DeviceIntegrityProvider>(singletonCImpl, 20));
      this.provideForensicReasoningEngineProvider = DoubleCheck.provider(new SwitchingProvider<ForensicReasoningEngine>(singletonCImpl, 21));
      this.provideScanOrchestratorProvider = DoubleCheck.provider(new SwitchingProvider<ScanOrchestrator>(singletonCImpl, 3));
      this.noOpScanTelemetryProvider = DoubleCheck.provider(new SwitchingProvider<NoOpScanTelemetry>(singletonCImpl, 23));
      this.privacyAwareScanTelemetryProvider = DoubleCheck.provider(new SwitchingProvider<PrivacyAwareScanTelemetry>(singletonCImpl, 22));
      this.scanAPKUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, 2);
      this.bindScanAPKUseCaseProvider = DoubleCheck.provider((Provider) scanAPKUseCaseImplProvider);
      this.provideQuarantineDirProvider = DoubleCheck.provider(new SwitchingProvider<File>(singletonCImpl, 25));
      this.quarantineAPKUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, 24);
      this.bindQuarantineAPKUseCaseProvider = DoubleCheck.provider((Provider) quarantineAPKUseCaseImplProvider);
      this.apkScanWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<ApkScanWorker_AssistedFactory>(singletonCImpl, 1));
      this.appInstallScanWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<AppInstallScanWorker_AssistedFactory>(singletonCImpl, 26));
      this.provideFeedbackPrivacyGateProvider = DoubleCheck.provider(new SwitchingProvider<FeedbackPrivacyGate>(singletonCImpl, 29));
      this.scanFeedbackRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ScanFeedbackRepositoryImpl>(singletonCImpl, 28));
      this.feedbackUploadWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<FeedbackUploadWorker_AssistedFactory>(singletonCImpl, 27));
      this.periodicDeepSweepWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<PeriodicDeepSweepWorker_AssistedFactory>(singletonCImpl, 30));
      this.scheduledScanWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<ScheduledScanWorker_AssistedFactory>(singletonCImpl, 31));
      this.provideThreatFeedCursorStoreProvider = DoubleCheck.provider(new SwitchingProvider<ThreatFeedCursorStore>(singletonCImpl, 34));
      this.provideThreatFeedStatusStoreProvider = DoubleCheck.provider(new SwitchingProvider<ThreatFeedStatusStore>(singletonCImpl, 35));
      this.provideThreatFeedSigningConfigProvider = DoubleCheck.provider(new SwitchingProvider<ThreatFeedSigningConfig>(singletonCImpl, 37));
      this.ed25519ThreatFeedVerifierProvider = DoubleCheck.provider(new SwitchingProvider<Ed25519ThreatFeedVerifier>(singletonCImpl, 36));
      this.threatFeedRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ThreatFeedRepositoryImpl>(singletonCImpl, 33));
      this.threatFeedSyncWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<ThreatFeedSyncWorker_AssistedFactory>(singletonCImpl, 32));
      this.provideAuthApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<AuthApiService>(singletonCImpl, 39));
      this.authRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepository>(singletonCImpl, 38));
      this.deviceScanManagerProvider = DoubleCheck.provider(new SwitchingProvider<DeviceScanManager>(singletonCImpl, 40));
    }

    @Override
    public void injectSafeGuardApplication(SafeGuardApplication arg0) {
      injectSafeGuardApplication2(arg0);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private SafeGuardApplication injectSafeGuardApplication2(SafeGuardApplication instance) {
      SafeGuardApplication_MembersInjector.injectCrashReporter(instance, bindCrashReporterProvider.get());
      SafeGuardApplication_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      SafeGuardApplication_MembersInjector.injectPreferences(instance, provideSecurePreferencesManagerProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.safeguard.crash.FileCrashReporter 
          return (T) new FileCrashReporter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.safeguard.worker.ApkScanWorker_AssistedFactory 
          return (T) new ApkScanWorker_AssistedFactory() {
            @Override
            public ApkScanWorker create(Context context, WorkerParameters params) {
              return new ApkScanWorker(context, params, singletonCImpl.bindScanAPKUseCaseProvider.get(), singletonCImpl.bindQuarantineAPKUseCaseProvider.get(), singletonCImpl.quarantineRepositoryImpl());
            }
          };

          case 2: // com.safeguard.domain.ScanAPKUseCaseImpl 
          return (T) new ScanAPKUseCaseImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideScanOrchestratorProvider.get(), singletonCImpl.scanRepositoryImpl(), singletonCImpl.privacyAwareScanTelemetryProvider.get());

          case 3: // com.safeguard.core.orchestration.ScanOrchestrator 
          return (T) SecurityModule_ProvideScanOrchestratorFactory.provideScanOrchestrator(singletonCImpl.provideProtectionLayersProvider.get(), singletonCImpl.provideZeroTrustDecisionEngineProvider.get(), singletonCImpl.provideRiskAssessmentEngineProvider.get(), singletonCImpl.provideDeviceIntegrityProvider.get(), singletonCImpl.provideForensicReasoningEngineProvider.get());

          case 4: // java.util.List<com.safeguard.core.domain.layer.ProtectionLayer> 
          return (T) SecurityModule_ProvideProtectionLayersFactory.provideProtectionLayers(SecurityModule_ProvideLayer1Factory.provideLayer1(), singletonCImpl.hashValidator(), SecurityModule_ProvideLayer3Factory.provideLayer3(), singletonCImpl.signatureValidator(), singletonCImpl.mLAnalyzer(), singletonCImpl.yaraScanner(), singletonCImpl.cloudVerifier());

          case 5: // com.safeguard.data.local.database.SafeGuardDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.namedByteArray());

          case 6: // com.safeguard.mlmodel.FeatureExtractor 
          return (T) SecurityModule_ProvideFeatureExtractorFactory.provideFeatureExtractor();

          case 7: // com.safeguard.mlmodel.TFLiteRunner 
          return (T) SecurityModule_ProvideTFLiteRunnerFactory.provideTFLiteRunner(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideModelDecryptorProvider.get());

          case 8: // com.safeguard.mlmodel.ModelDecryptor 
          return (T) SecurityModule_ProvideModelDecryptorFactory.provideModelDecryptor(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 9: // com.safeguard.security.layers.layer7.YaraRuleSet 
          return (T) SecurityModule_ProvideYaraRuleSetFactory.provideYaraRuleSet(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 10: // com.safeguard.data.remote.api.ThreatIntelligenceApi 
          return (T) NetworkModule_ProvideThreatIntelligenceApiFactory.provideThreatIntelligenceApi(singletonCImpl.provideRetrofitProvider.get());

          case 11: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.provideMoshiProvider.get(), NetworkConfigModule_ProvideBaseUrlFactory.provideBaseUrl());

          case 12: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient(NetworkConfigModule_ProvideBaseUrlFactory.provideBaseUrl(), NetworkConfigModule.INSTANCE.provideCertificatePin(), NetworkConfigModule.INSTANCE.provideHttpLoggingEnabled(), NetworkConfigModule.INSTANCE.provideApiKey());

          case 13: // com.squareup.moshi.Moshi 
          return (T) NetworkModule_ProvideMoshiFactory.provideMoshi();

          case 14: // com.safeguard.data.local.preferences.SecurePreferencesManager 
          return (T) DatabaseModule_ProvideSecurePreferencesManagerFactory.provideSecurePreferencesManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 15: // com.safeguard.core.domain.integrity.PlayIntegrityChecker 
          return (T) PlayIntegrityModule_ProvidePlayIntegrityCheckerFactory.providePlayIntegrityChecker(singletonCImpl.providePlayIntegrityConfigProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.noOpPlayIntegrityCheckerProvider.get());

          case 16: // com.safeguard.core.domain.integrity.PlayIntegrityConfig 
          return (T) PlayIntegrityModule_ProvidePlayIntegrityConfigFactory.providePlayIntegrityConfig();

          case 17: // com.safeguard.core.domain.integrity.NoOpPlayIntegrityChecker 
          return (T) new NoOpPlayIntegrityChecker();

          case 18: // com.safeguard.core.orchestration.ZeroTrustDecisionEngine 
          return (T) SecurityModule_ProvideZeroTrustDecisionEngineFactory.provideZeroTrustDecisionEngine();

          case 19: // com.safeguard.core.orchestration.RiskAssessmentEngine 
          return (T) SecurityModule_ProvideRiskAssessmentEngineFactory.provideRiskAssessmentEngine();

          case 20: // com.safeguard.core.domain.security.DeviceIntegrityProvider 
          return (T) SecurityModule_ProvideDeviceIntegrityProviderFactory.provideDeviceIntegrityProvider(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 21: // com.safeguard.core.orchestration.ForensicReasoningEngine 
          return (T) SecurityModule_ProvideForensicReasoningEngineFactory.provideForensicReasoningEngine();

          case 22: // com.safeguard.telemetry.PrivacyAwareScanTelemetry 
          return (T) new PrivacyAwareScanTelemetry(singletonCImpl.provideSecurePreferencesManagerProvider.get(), singletonCImpl.noOpScanTelemetryProvider.get());

          case 23: // com.safeguard.telemetry.NoOpScanTelemetry 
          return (T) new NoOpScanTelemetry();

          case 24: // com.safeguard.domain.QuarantineAPKUseCaseImpl 
          return (T) new QuarantineAPKUseCaseImpl(singletonCImpl.quarantineRepositoryImpl());

          case 25: // @javax.inject.Named("quarantine_dir") java.io.File 
          return (T) DatabaseModule_ProvideQuarantineDirFactory.provideQuarantineDir(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 26: // com.safeguard.worker.AppInstallScanWorker_AssistedFactory 
          return (T) new AppInstallScanWorker_AssistedFactory() {
            @Override
            public AppInstallScanWorker create(Context context2, WorkerParameters params2) {
              return new AppInstallScanWorker(context2, params2, singletonCImpl.bindScanAPKUseCaseProvider.get(), singletonCImpl.bindQuarantineAPKUseCaseProvider.get());
            }
          };

          case 27: // com.safeguard.worker.FeedbackUploadWorker_AssistedFactory 
          return (T) new FeedbackUploadWorker_AssistedFactory() {
            @Override
            public FeedbackUploadWorker create(Context context3, WorkerParameters params3) {
              return new FeedbackUploadWorker(context3, params3, singletonCImpl.scanFeedbackRepositoryImplProvider.get(), singletonCImpl.provideFeedbackPrivacyGateProvider.get());
            }
          };

          case 28: // com.safeguard.data.repository.ScanFeedbackRepositoryImpl 
          return (T) new ScanFeedbackRepositoryImpl(singletonCImpl.scanFeedbackEventDao(), singletonCImpl.provideThreatIntelligenceApiProvider.get(), singletonCImpl.provideFeedbackPrivacyGateProvider.get(), singletonCImpl.provideMoshiProvider.get());

          case 29: // com.safeguard.core.domain.feedback.FeedbackPrivacyGate 
          return (T) DatabaseModule_ProvideFeedbackPrivacyGateFactory.provideFeedbackPrivacyGate(singletonCImpl.provideSecurePreferencesManagerProvider.get());

          case 30: // com.safeguard.worker.PeriodicDeepSweepWorker_AssistedFactory 
          return (T) new PeriodicDeepSweepWorker_AssistedFactory() {
            @Override
            public PeriodicDeepSweepWorker create(Context context4, WorkerParameters params4) {
              return new PeriodicDeepSweepWorker(context4, params4);
            }
          };

          case 31: // com.safeguard.worker.ScheduledScanWorker_AssistedFactory 
          return (T) new ScheduledScanWorker_AssistedFactory() {
            @Override
            public ScheduledScanWorker create(Context context5, WorkerParameters params5) {
              return new ScheduledScanWorker(context5, params5, singletonCImpl.provideSecurePreferencesManagerProvider.get());
            }
          };

          case 32: // com.safeguard.worker.ThreatFeedSyncWorker_AssistedFactory 
          return (T) new ThreatFeedSyncWorker_AssistedFactory() {
            @Override
            public ThreatFeedSyncWorker create(Context context6, WorkerParameters params6) {
              return new ThreatFeedSyncWorker(context6, params6, singletonCImpl.threatFeedRepositoryImplProvider.get(), singletonCImpl.provideSecurePreferencesManagerProvider.get(), singletonCImpl.provideThreatFeedStatusStoreProvider.get());
            }
          };

          case 33: // com.safeguard.data.repository.ThreatFeedRepositoryImpl 
          return (T) new ThreatFeedRepositoryImpl(singletonCImpl.provideThreatIntelligenceApiProvider.get(), singletonCImpl.malwareSignatureDao(), singletonCImpl.provideThreatFeedCursorStoreProvider.get(), singletonCImpl.provideThreatFeedStatusStoreProvider.get(), singletonCImpl.ed25519ThreatFeedVerifierProvider.get(), singletonCImpl.provideMoshiProvider.get());

          case 34: // com.safeguard.core.domain.repository.ThreatFeedCursorStore 
          return (T) DatabaseModule_ProvideThreatFeedCursorStoreFactory.provideThreatFeedCursorStore(singletonCImpl.provideSecurePreferencesManagerProvider.get());

          case 35: // com.safeguard.core.domain.repository.ThreatFeedStatusStore 
          return (T) DatabaseModule_ProvideThreatFeedStatusStoreFactory.provideThreatFeedStatusStore(singletonCImpl.provideSecurePreferencesManagerProvider.get());

          case 36: // com.safeguard.data.remote.signing.Ed25519ThreatFeedVerifier 
          return (T) new Ed25519ThreatFeedVerifier(singletonCImpl.provideThreatFeedSigningConfigProvider.get(), singletonCImpl.provideMoshiProvider.get());

          case 37: // com.safeguard.core.domain.repository.ThreatFeedSigningConfig 
          return (T) NetworkConfigModule_ProvideThreatFeedSigningConfigFactory.provideThreatFeedSigningConfig();

          case 38: // com.safeguard.data.repository.AuthRepository 
          return (T) new AuthRepository(singletonCImpl.provideAuthApiServiceProvider.get());

          case 39: // com.safeguard.data.remote.api.AuthApiService 
          return (T) NetworkModule_ProvideAuthApiServiceFactory.provideAuthApiService(singletonCImpl.provideRetrofitProvider.get());

          case 40: // com.safeguard.manager.DeviceScanManager 
          return (T) new DeviceScanManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.bindScanAPKUseCaseProvider.get(), singletonCImpl.bindQuarantineAPKUseCaseProvider.get(), singletonCImpl.quarantineRepositoryImpl(), singletonCImpl.provideSecurePreferencesManagerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
