package com.safeguard.di;

import com.safeguard.core.domain.layer.ProtectionLayer;
import com.safeguard.security.layers.layer1.Layer1FileMonitor;
import com.safeguard.security.layers.layer2.HashValidator;
import com.safeguard.security.layers.layer3.PermissionAnalyzer;
import com.safeguard.security.layers.layer4.SignatureValidator;
import com.safeguard.security.layers.layer5.MLAnalyzer;
import com.safeguard.security.layers.layer6.CloudVerifier;
import com.safeguard.security.layers.layer7.YaraScanner;
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
public final class SecurityModule_ProvideProtectionLayersFactory implements Factory<List<ProtectionLayer>> {
  private final Provider<Layer1FileMonitor> layer1Provider;

  private final Provider<HashValidator> layer2Provider;

  private final Provider<PermissionAnalyzer> layer3Provider;

  private final Provider<SignatureValidator> layer4Provider;

  private final Provider<MLAnalyzer> layer5Provider;

  private final Provider<YaraScanner> layer7Provider;

  private final Provider<CloudVerifier> layer6Provider;

  public SecurityModule_ProvideProtectionLayersFactory(Provider<Layer1FileMonitor> layer1Provider,
      Provider<HashValidator> layer2Provider, Provider<PermissionAnalyzer> layer3Provider,
      Provider<SignatureValidator> layer4Provider, Provider<MLAnalyzer> layer5Provider,
      Provider<YaraScanner> layer7Provider, Provider<CloudVerifier> layer6Provider) {
    this.layer1Provider = layer1Provider;
    this.layer2Provider = layer2Provider;
    this.layer3Provider = layer3Provider;
    this.layer4Provider = layer4Provider;
    this.layer5Provider = layer5Provider;
    this.layer7Provider = layer7Provider;
    this.layer6Provider = layer6Provider;
  }

  @Override
  public List<ProtectionLayer> get() {
    return provideProtectionLayers(layer1Provider.get(), layer2Provider.get(), layer3Provider.get(), layer4Provider.get(), layer5Provider.get(), layer7Provider.get(), layer6Provider.get());
  }

  public static SecurityModule_ProvideProtectionLayersFactory create(
      Provider<Layer1FileMonitor> layer1Provider, Provider<HashValidator> layer2Provider,
      Provider<PermissionAnalyzer> layer3Provider, Provider<SignatureValidator> layer4Provider,
      Provider<MLAnalyzer> layer5Provider, Provider<YaraScanner> layer7Provider,
      Provider<CloudVerifier> layer6Provider) {
    return new SecurityModule_ProvideProtectionLayersFactory(layer1Provider, layer2Provider, layer3Provider, layer4Provider, layer5Provider, layer7Provider, layer6Provider);
  }

  public static List<ProtectionLayer> provideProtectionLayers(Layer1FileMonitor layer1,
      HashValidator layer2, PermissionAnalyzer layer3, SignatureValidator layer4, MLAnalyzer layer5,
      YaraScanner layer7, CloudVerifier layer6) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideProtectionLayers(layer1, layer2, layer3, layer4, layer5, layer7, layer6));
  }
}
