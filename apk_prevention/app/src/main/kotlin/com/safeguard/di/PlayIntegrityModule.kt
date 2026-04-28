package com.safeguard.di

import android.content.Context
import android.util.Log
import com.safeguard.BuildConfig
import com.safeguard.core.domain.integrity.NoOpPlayIntegrityChecker
import com.safeguard.core.domain.integrity.PlayIntegrityChecker
import com.safeguard.core.domain.integrity.PlayIntegrityConfig
import com.safeguard.integrity.GooglePlayIntegrityChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Phase 3.4 Hilt wiring for the Play Integrity cross-check.
 *
 * Selects between two impls at injection time:
 *  - [NoOpPlayIntegrityChecker] — default. Used whenever the build does NOT supply a
 *    cloud project number in `local.properties` (the common case for dev / CI / non-
 *    Play distributions).
 *  - [GooglePlayIntegrityChecker] — real Play Integrity SDK wrapper. Used only when
 *    `safeguard.play.integrity.cloud.project.number` is set to a numeric value.
 *
 * We intentionally fail open (NoOp) rather than failing closed when the project number
 * is malformed — a typo in `local.properties` shouldn't bring scanning to a halt, but
 * the operator gets a single clear log line so they can spot the misconfig.
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayIntegrityModule {

    private const val TAG = "PlayIntegrityModule"

    @Provides
    @Singleton
    fun providePlayIntegrityConfig(): PlayIntegrityConfig =
        PlayIntegrityConfig(
            cloudProjectNumber = BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER.orEmpty().trim()
        )

    /**
     * Single-binding provider so tests in `:app` can swap the whole checker by overriding
     * this module — and so the rest of the codebase only ever depends on the abstract
     * [PlayIntegrityChecker], never on a concrete impl.
     */
    @Provides
    @Singleton
    fun providePlayIntegrityChecker(
        config: PlayIntegrityConfig,
        @ApplicationContext context: Context,
        noOp: NoOpPlayIntegrityChecker
    ): PlayIntegrityChecker {
        if (!config.isEnabled) {
            // Either unset (intended dev/CI behaviour, silent) or set to garbage (warn).
            if (config.cloudProjectNumber.isNotBlank()) {
                Log.w(
                    TAG,
                    "PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER='${config.cloudProjectNumber}' is not a " +
                        "numeric Long; falling back to NoOpPlayIntegrityChecker."
                )
            }
            return noOp
        }
        return GooglePlayIntegrityChecker(context.applicationContext, config)
    }
}
