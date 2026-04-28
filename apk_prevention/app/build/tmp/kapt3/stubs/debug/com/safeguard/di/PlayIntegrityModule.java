package com.safeguard.di;

import android.content.Context;
import android.util.Log;
import com.safeguard.BuildConfig;
import com.safeguard.core.domain.integrity.NoOpPlayIntegrityChecker;
import com.safeguard.core.domain.integrity.PlayIntegrityChecker;
import com.safeguard.core.domain.integrity.PlayIntegrityConfig;
import com.safeguard.integrity.GooglePlayIntegrityChecker;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/**
 * Phase 3.4 Hilt wiring for the Play Integrity cross-check.
 *
 * Selects between two impls at injection time:
 * - [NoOpPlayIntegrityChecker] — default. Used whenever the build does NOT supply a
 *   cloud project number in `local.properties` (the common case for dev / CI / non-
 *   Play distributions).
 * - [GooglePlayIntegrityChecker] — real Play Integrity SDK wrapper. Used only when
 *   `safeguard.play.integrity.cloud.project.number` is set to a numeric value.
 *
 * We intentionally fail open (NoOp) rather than failing closed when the project number
 * is malformed — a typo in `local.properties` shouldn't bring scanning to a halt, but
 * the operator gets a single clear log line so they can spot the misconfig.
 */
@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\"\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0001\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0007J\b\u0010\r\u001a\u00020\bH\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/safeguard/di/PlayIntegrityModule;", "", "()V", "TAG", "", "providePlayIntegrityChecker", "Lcom/safeguard/core/domain/integrity/PlayIntegrityChecker;", "config", "Lcom/safeguard/core/domain/integrity/PlayIntegrityConfig;", "context", "Landroid/content/Context;", "noOp", "Lcom/safeguard/core/domain/integrity/NoOpPlayIntegrityChecker;", "providePlayIntegrityConfig", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class PlayIntegrityModule {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "PlayIntegrityModule";
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.di.PlayIntegrityModule INSTANCE = null;
    
    private PlayIntegrityModule() {
        super();
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.integrity.PlayIntegrityConfig providePlayIntegrityConfig() {
        return null;
    }
    
    /**
     * Single-binding provider so tests in `:app` can swap the whole checker by overriding
     * this module — and so the rest of the codebase only ever depends on the abstract
     * [PlayIntegrityChecker], never on a concrete impl.
     */
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.integrity.PlayIntegrityChecker providePlayIntegrityChecker(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.integrity.PlayIntegrityConfig config, @dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.integrity.NoOpPlayIntegrityChecker noOp) {
        return null;
    }
}