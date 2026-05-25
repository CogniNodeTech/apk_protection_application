package com.safeguard.di;

import com.safeguard.security.layers.layer7.YaraRuleSet;
import com.safeguard.security.layers.layer7.YaraScanner;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class SecurityModule_ProvideLayer7Factory implements Factory<YaraScanner> {
  private final Provider<YaraRuleSet> ruleSetProvider;

  public SecurityModule_ProvideLayer7Factory(Provider<YaraRuleSet> ruleSetProvider) {
    this.ruleSetProvider = ruleSetProvider;
  }

  @Override
  public YaraScanner get() {
    return provideLayer7(ruleSetProvider.get());
  }

  public static SecurityModule_ProvideLayer7Factory create(Provider<YaraRuleSet> ruleSetProvider) {
    return new SecurityModule_ProvideLayer7Factory(ruleSetProvider);
  }

  public static YaraScanner provideLayer7(YaraRuleSet ruleSet) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideLayer7(ruleSet));
  }
}
