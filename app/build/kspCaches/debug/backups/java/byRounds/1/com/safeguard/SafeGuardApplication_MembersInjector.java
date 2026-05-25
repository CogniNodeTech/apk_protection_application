package com.safeguard;

import androidx.hilt.work.HiltWorkerFactory;
import com.safeguard.core.domain.crash.CrashReporter;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SafeGuardApplication_MembersInjector implements MembersInjector<SafeGuardApplication> {
  private final Provider<CrashReporter> crashReporterProvider;

  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  private final Provider<SecurePreferencesManager> preferencesProvider;

  public SafeGuardApplication_MembersInjector(Provider<CrashReporter> crashReporterProvider,
      Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<SecurePreferencesManager> preferencesProvider) {
    this.crashReporterProvider = crashReporterProvider;
    this.workerFactoryProvider = workerFactoryProvider;
    this.preferencesProvider = preferencesProvider;
  }

  public static MembersInjector<SafeGuardApplication> create(
      Provider<CrashReporter> crashReporterProvider,
      Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<SecurePreferencesManager> preferencesProvider) {
    return new SafeGuardApplication_MembersInjector(crashReporterProvider, workerFactoryProvider, preferencesProvider);
  }

  @Override
  public void injectMembers(SafeGuardApplication instance) {
    injectCrashReporter(instance, crashReporterProvider.get());
    injectWorkerFactory(instance, workerFactoryProvider.get());
    injectPreferences(instance, preferencesProvider.get());
  }

  @InjectedFieldSignature("com.safeguard.SafeGuardApplication.crashReporter")
  public static void injectCrashReporter(SafeGuardApplication instance,
      CrashReporter crashReporter) {
    instance.crashReporter = crashReporter;
  }

  @InjectedFieldSignature("com.safeguard.SafeGuardApplication.workerFactory")
  public static void injectWorkerFactory(SafeGuardApplication instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }

  @InjectedFieldSignature("com.safeguard.SafeGuardApplication.preferences")
  public static void injectPreferences(SafeGuardApplication instance,
      SecurePreferencesManager preferences) {
    instance.preferences = preferences;
  }
}
