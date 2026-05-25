package com.safeguard.di;

import android.content.Context;
import com.safeguard.security.layers.layer7.YaraRuleSet;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SecurityModule_ProvideYaraRuleSetFactory implements Factory<YaraRuleSet> {
  private final Provider<Context> contextProvider;

  public SecurityModule_ProvideYaraRuleSetFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public YaraRuleSet get() {
    return provideYaraRuleSet(contextProvider.get());
  }

  public static SecurityModule_ProvideYaraRuleSetFactory create(Provider<Context> contextProvider) {
    return new SecurityModule_ProvideYaraRuleSetFactory(contextProvider);
  }

  public static YaraRuleSet provideYaraRuleSet(Context context) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideYaraRuleSet(context));
  }
}
