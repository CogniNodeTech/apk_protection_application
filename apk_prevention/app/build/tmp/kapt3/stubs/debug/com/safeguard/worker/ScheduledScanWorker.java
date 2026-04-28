package com.safeguard.worker;

import androidx.hilt.work.HiltWorker;
import androidx.work.CoroutineWorker;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.notification.SafeGuardNotificationManager;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Runs at the scheduled time: shows a "Time for your scheduled scan" notification,
 * then enqueues the next run (daily or weekly at the same time).
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 \u00152\u00020\u0001:\u0001\u0015B#\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\t\u001a\u00020\nH\u0096@\u00a2\u0006\u0002\u0010\u000bJ \u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\b\u0010\u0013\u001a\u00020\u0014H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/safeguard/worker/ScheduledScanWorker;", "Landroidx/work/CoroutineWorker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "preferences", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;Lcom/safeguard/data/local/preferences/SecurePreferencesManager;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "nextDelayMillis", "", "hour", "", "minute", "frequency", "", "scheduleNext", "", "Companion", "app_debug"})
@androidx.hilt.work.HiltWorker
public final class ScheduledScanWorker extends androidx.work.CoroutineWorker {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.preferences.SecurePreferencesManager preferences = null;
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String TAG = "scheduled_scan";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String UNIQUE_NAME = "scheduled_scan";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String KEY_HOUR = "schedule_hour";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String KEY_MINUTE = "schedule_minute";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String KEY_FREQUENCY = "schedule_frequency";
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.worker.ScheduledScanWorker.Companion Companion = null;
    
    @dagger.assisted.AssistedInject
    public ScheduledScanWorker(@dagger.assisted.Assisted
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @dagger.assisted.Assisted
    @org.jetbrains.annotations.NotNull
    androidx.work.WorkerParameters params, @org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager preferences) {
        super(null, null);
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object doWork(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> $completion) {
        return null;
    }
    
    private final void scheduleNext() {
    }
    
    private final long nextDelayMillis(int hour, int minute, java.lang.String frequency) {
        return 0L;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/safeguard/worker/ScheduledScanWorker$Companion;", "", "()V", "KEY_FREQUENCY", "", "KEY_HOUR", "KEY_MINUTE", "TAG", "UNIQUE_NAME", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}