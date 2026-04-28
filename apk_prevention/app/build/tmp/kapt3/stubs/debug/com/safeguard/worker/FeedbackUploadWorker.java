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
import com.safeguard.core.domain.feedback.FeedbackPrivacyGate;
import com.safeguard.core.domain.feedback.FeedbackUploadResult;
import com.safeguard.core.domain.repository.ScanFeedbackRepository;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import kotlinx.coroutines.Dispatchers;
import java.util.concurrent.TimeUnit;

/**
 * Periodic uploader for the Phase 3.2 privacy-preserving feedback queue.
 *
 * Runs every ~6h under "connected + not low battery" constraints so a power-saving
 * device or a metered hotspot never gets surprised by a feedback flush. The worker is
 * intentionally separate from [ThreatFeedSyncWorker]: that one is a *pull* (we want
 * fresher signatures, server is authoritative) on a 12h cadence, this one is a *push*
 * (we want to hand off opt-in user data) on a 6h cadence so a chatty device doesn't
 * accumulate weeks of events in the local queue if the server drops a few uploads.
 *
 * Privacy gating is **double-checked** here even though the repository already enforces
 * it: the repository's gate guards the *send*, this worker's gate guards even *waking up*
 * to read the queue. Either is sufficient on its own; together they tolerate either layer
 * regressing in a future refactor without leaking events.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB+\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u000e\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\rR\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/safeguard/worker/FeedbackUploadWorker;", "Landroidx/work/CoroutineWorker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "repository", "Lcom/safeguard/core/domain/repository/ScanFeedbackRepository;", "privacyGate", "Lcom/safeguard/core/domain/feedback/FeedbackPrivacyGate;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;Lcom/safeguard/core/domain/repository/ScanFeedbackRepository;Lcom/safeguard/core/domain/feedback/FeedbackPrivacyGate;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
@androidx.hilt.work.HiltWorker
public final class FeedbackUploadWorker extends androidx.work.CoroutineWorker {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ScanFeedbackRepository repository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.feedback.FeedbackPrivacyGate privacyGate = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "FeedbackUploadWorker";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String UNIQUE_WORK_NAME = "safeguard_feedback_upload";
    private static final long UPLOAD_INTERVAL_HOURS = 6L;
    private static final long INITIAL_BACKOFF_MINUTES = 30L;
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.worker.FeedbackUploadWorker.Companion Companion = null;
    
    @dagger.assisted.AssistedInject
    public FeedbackUploadWorker(@dagger.assisted.Assisted
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @dagger.assisted.Assisted
    @org.jetbrains.annotations.NotNull
    androidx.work.WorkerParameters params, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ScanFeedbackRepository repository, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.feedback.FeedbackPrivacyGate privacyGate) {
        super(null, null);
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object doWork(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\r\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/safeguard/worker/FeedbackUploadWorker$Companion;", "", "()V", "INITIAL_BACKOFF_MINUTES", "", "TAG", "", "UNIQUE_WORK_NAME", "UPLOAD_INTERVAL_HOURS", "cancel", "", "context", "Landroid/content/Context;", "schedule", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Schedule (or refresh) the unique periodic upload. `KEEP` so we don't reset the
         * inter-upload timer on every app launch — that would let a user who repeatedly
         * cold-starts the app starve the queue forever.
         *
         * Idempotent. Safe to call from `SafeGuardApplication.onCreate`.
         */
        public final void schedule(@org.jetbrains.annotations.NotNull
        android.content.Context context) {
        }
        
        /**
         * Cancel the scheduled upload. Called from the settings toggle when the user opts
         * out so the worker stops running entirely (the privacy gate would already make
         * each run a no-op, but cancelling is both cleaner and saves a few job slots).
         */
        public final void cancel(@org.jetbrains.annotations.NotNull
        android.content.Context context) {
        }
    }
}