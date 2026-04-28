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
 * Cloud API URL, certificate pin, and debug HTTP logging from BuildConfig / local.properties.
 */
@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\n\u0010\u0003\u001a\u0004\u0018\u00010\u0004H\u0007J\b\u0010\u0005\u001a\u00020\u0004H\u0007J\n\u0010\u0006\u001a\u0004\u0018\u00010\u0004H\u0007J\b\u0010\u0007\u001a\u00020\bH\u0007J\b\u0010\t\u001a\u00020\nH\u0007\u00a8\u0006\u000b"}, d2 = {"Lcom/safeguard/di/NetworkConfigModule;", "", "()V", "provideApiKey", "", "provideBaseUrl", "provideCertificatePin", "provideHttpLoggingEnabled", "", "provideThreatFeedSigningConfig", "Lcom/safeguard/core/domain/repository/ThreatFeedSigningConfig;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class NetworkConfigModule {
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.di.NetworkConfigModule INSTANCE = null;
    
    private NetworkConfigModule() {
        super();
    }
    
    @dagger.Provides
    @javax.inject.Named(value = "base_url")
    @org.jetbrains.annotations.NotNull
    public final java.lang.String provideBaseUrl() {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Named(value = "cert_pin")
    @org.jetbrains.annotations.Nullable
    public final java.lang.String provideCertificatePin() {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Named(value = "http_logging_enabled")
    public final boolean provideHttpLoggingEnabled() {
        return false;
    }
    
    @dagger.Provides
    @javax.inject.Named(value = "api_key")
    @org.jetbrains.annotations.Nullable
    public final java.lang.String provideApiKey() {
        return null;
    }
    
    /**
     * Phase 3.1: pinned Ed25519 public key the on-device threat-feed verifier uses to
     * authenticate `/v1/threat-feed` responses. Empty values mean signing is disabled in
     * this build, which is fine for dev/mock and falls back to the legacy unsigned shape.
     */
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.repository.ThreatFeedSigningConfig provideThreatFeedSigningConfig() {
        return null;
    }
}