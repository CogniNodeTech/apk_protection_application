package com.safeguard.worker;

import android.content.Context;
import android.util.Log;
import androidx.hilt.work.HiltWorker;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.CoroutineWorker;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;
import com.safeguard.core.domain.repository.ThreatFeedRepository;
import com.safeguard.core.domain.repository.ThreatFeedStatus;
import com.safeguard.core.domain.repository.ThreatFeedStatusStore;
import com.safeguard.core.domain.repository.ThreatFeedSyncResult;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import kotlinx.coroutines.Dispatchers;
import java.util.concurrent.TimeUnit;

/**
 * Periodically pulls fresh malware signatures from the SafeGuard threat-intel server and
 * upserts them into the on-device DB. Without this, Layer 2's hash + TLSH lookups score
 * against whatever subset of MalwareBazaar was bundled at install time, which goes stale
 * fast (abuse.ch ingests new APK samples every hour).
 *
 * Why 12 h and not "real-time" or "hourly"?
 * - We refresh on a *device* schedule, not a *threat* schedule. The decision engine is
 *   online via Layer 6 (`/v1/verify`) for SHA-256 hits, so the local feed is mainly for
 *   TLSH variant detection (resilient to repacks) — daily-ish freshness is plenty.
 * - Each pull is up to ~200 rows × ~150 bytes ≈ 30 KB. Twice a day keeps mobile-data
 *   impact negligible while still catching same-day campaigns.
 * - Sync is gated on user-consented [SecurePreferencesManager.cloudVerificationEnabled];
 *   privacy onboarding flips this on after the user accepts.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \u00102\u00020\u0001:\u0001\u0010B3\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u000e\u0010\r\u001a\u00020\u000eH\u0096@\u00a2\u0006\u0002\u0010\u000fR\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/safeguard/worker/ThreatFeedSyncWorker;", "Landroidx/work/CoroutineWorker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "repository", "Lcom/safeguard/core/domain/repository/ThreatFeedRepository;", "preferences", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "statusStore", "Lcom/safeguard/core/domain/repository/ThreatFeedStatusStore;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;Lcom/safeguard/core/domain/repository/ThreatFeedRepository;Lcom/safeguard/data/local/preferences/SecurePreferencesManager;Lcom/safeguard/core/domain/repository/ThreatFeedStatusStore;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
@androidx.hilt.work.HiltWorker
public final class ThreatFeedSyncWorker extends androidx.work.CoroutineWorker {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ThreatFeedRepository repository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.preferences.SecurePreferencesManager preferences = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ThreatFeedStatusStore statusStore = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "ThreatFeedSyncWorker";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String UNIQUE_WORK_NAME = "safeguard_threat_feed_sync";
    private static final long SYNC_INTERVAL_HOURS = 12L;
    private static final long INITIAL_BACKOFF_MINUTES = 30L;
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.worker.ThreatFeedSyncWorker.Companion Companion = null;
    
    @dagger.assisted.AssistedInject
    public ThreatFeedSyncWorker(@dagger.assisted.Assisted
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @dagger.assisted.Assisted
    @org.jetbrains.annotations.NotNull
    androidx.work.WorkerParameters params, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ThreatFeedRepository repository, @org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager preferences, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ThreatFeedStatusStore statusStore) {
        super(null, null);
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object doWork(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/safeguard/worker/ThreatFeedSyncWorker$Companion;", "", "()V", "INITIAL_BACKOFF_MINUTES", "", "SYNC_INTERVAL_HOURS", "TAG", "", "UNIQUE_WORK_NAME", "schedule", "", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Enqueue (or refresh) the unique periodic sync. Safe to call multiple times — `KEEP`
         * preserves an existing schedule so we don't reset the inter-sync timer on every app
         * launch. Called from `SafeGuardApplication.onCreate`.
         */
        public final void schedule(@org.jetbrains.annotations.NotNull
        android.content.Context context) {
        }
    }
}