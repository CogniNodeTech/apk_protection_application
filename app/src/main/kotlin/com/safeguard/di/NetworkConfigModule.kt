package com.safeguard.di

import com.safeguard.BuildConfig
import com.safeguard.core.domain.repository.ThreatFeedSigningConfig
import com.safeguard.data.remote.signing.Ed25519ThreatFeedVerifier
import com.safeguard.data.remote.signing.ThreatFeedSignatureVerifier
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Cloud API URL, certificate pin, and debug HTTP logging from BuildConfig / local.properties.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkConfigModule {

    @Provides
    @Named("base_url")
    fun provideBaseUrl(): String {
        val raw = BuildConfig.API_BASE_URL.trim()
        if (raw.isEmpty()) return "http://10.0.2.2:3000/"
        return if (raw.endsWith("/")) raw else "$raw/"
    }

    @Provides
    @Named("cert_pin")
    fun provideCertificatePin(): String? {
        val pin = BuildConfig.CERT_PIN
        return if (pin.isNullOrBlank()) null else pin
    }

    @Provides
    @Named("http_logging_enabled")
    fun provideHttpLoggingEnabled(): Boolean = BuildConfig.DEBUG

    @Provides
    @Named("api_key")
    fun provideApiKey(): String? {
        val key = BuildConfig.API_KEY
        return if (key.isBlank()) null else key
    }

    /**
     * Phase 3.1: pinned Ed25519 public key the on-device threat-feed verifier uses to
     * authenticate `/v1/threat-feed` responses. Empty values mean signing is disabled in
     * this build, which is fine for dev/mock and falls back to the legacy unsigned shape.
     */
    @Provides
    @Singleton
    fun provideThreatFeedSigningConfig(): ThreatFeedSigningConfig =
        ThreatFeedSigningConfig(
            keyId = BuildConfig.THREAT_FEED_SIGNING_KEY_ID.orEmpty(),
            publicKeyB64 = BuildConfig.THREAT_FEED_SIGNING_PUBLIC_KEY_B64.orEmpty()
        )
}

/**
 * Hilt binding for the signature verifier interface so callers in `:data` consume the
 * abstraction (`ThreatFeedSignatureVerifier`) rather than the Ed25519-specific impl. Lets
 * unit tests in `:data` swap a fake without touching `i2p:eddsa`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ThreatFeedSignatureModule {
    @Binds
    @Singleton
    abstract fun bindThreatFeedSignatureVerifier(
        impl: Ed25519ThreatFeedVerifier
    ): ThreatFeedSignatureVerifier
}
