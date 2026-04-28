package com.safeguard.di;

import com.safeguard.BuildConfig;
import com.safeguard.core.domain.repository.ThreatFeedSigningConfig;
import com.safeguard.data.remote.signing.Ed25519ThreatFeedVerifier;
import com.safeguard.data.remote.signing.ThreatFeedSignatureVerifier;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Hilt binding for the signature verifier interface so callers in `:data` consume the
 * abstraction (`ThreatFeedSignatureVerifier`) rather than the Ed25519-specific impl. Lets
 * unit tests in `:data` swap a fake without touching `i2p:eddsa`.
 */
@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'\u00a8\u0006\u0007"}, d2 = {"Lcom/safeguard/di/ThreatFeedSignatureModule;", "", "()V", "bindThreatFeedSignatureVerifier", "Lcom/safeguard/data/remote/signing/ThreatFeedSignatureVerifier;", "impl", "Lcom/safeguard/data/remote/signing/Ed25519ThreatFeedVerifier;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class ThreatFeedSignatureModule {
    
    public ThreatFeedSignatureModule() {
        super();
    }
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.data.remote.signing.ThreatFeedSignatureVerifier bindThreatFeedSignatureVerifier(@org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.signing.Ed25519ThreatFeedVerifier impl);
}