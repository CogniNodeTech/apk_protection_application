package com.safeguard.ui.screens.scanprogress;

import com.safeguard.manager.DeviceScanManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class DeviceScanProgressViewModel_Factory implements Factory<DeviceScanProgressViewModel> {
  private final Provider<DeviceScanManager> deviceScanManagerProvider;

  public DeviceScanProgressViewModel_Factory(
      Provider<DeviceScanManager> deviceScanManagerProvider) {
    this.deviceScanManagerProvider = deviceScanManagerProvider;
  }

  @Override
  public DeviceScanProgressViewModel get() {
    return newInstance(deviceScanManagerProvider.get());
  }

  public static DeviceScanProgressViewModel_Factory create(
      Provider<DeviceScanManager> deviceScanManagerProvider) {
    return new DeviceScanProgressViewModel_Factory(deviceScanManagerProvider);
  }

  public static DeviceScanProgressViewModel newInstance(DeviceScanManager deviceScanManager) {
    return new DeviceScanProgressViewModel(deviceScanManager);
  }
}
