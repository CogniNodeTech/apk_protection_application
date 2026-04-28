package com.safeguard;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;
import com.safeguard.core.domain.crash.CrashReporter;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.service.FileObserverService;
import com.safeguard.worker.FeedbackUploadWorker;
import com.safeguard.worker.PeriodicDeepSweepWorker;
import com.safeguard.worker.ThreatFeedSyncWorker;
import dagger.hilt.android.HiltAndroidApp;
import javax.inject.Inject;

@dagger.hilt.android.HiltAndroidApp
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \u001d2\u00020\u00012\u00020\u0002:\u0001\u001dB\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u001a\u001a\u00020\u001bH\u0002J\b\u0010\u001c\u001a\u00020\u001bH\u0016R\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001e\u0010\n\u001a\u00020\u000b8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u0014\u0010\u0010\u001a\u00020\u00118VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013R\u001e\u0010\u0014\u001a\u00020\u00158\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019\u00a8\u0006\u001e"}, d2 = {"Lcom/safeguard/SafeGuardApplication;", "Landroid/app/Application;", "Landroidx/work/Configuration$Provider;", "()V", "crashReporter", "Lcom/safeguard/core/domain/crash/CrashReporter;", "getCrashReporter", "()Lcom/safeguard/core/domain/crash/CrashReporter;", "setCrashReporter", "(Lcom/safeguard/core/domain/crash/CrashReporter;)V", "preferences", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "getPreferences", "()Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "setPreferences", "(Lcom/safeguard/data/local/preferences/SecurePreferencesManager;)V", "workManagerConfiguration", "Landroidx/work/Configuration;", "getWorkManagerConfiguration", "()Landroidx/work/Configuration;", "workerFactory", "Landroidx/hilt/work/HiltWorkerFactory;", "getWorkerFactory", "()Landroidx/hilt/work/HiltWorkerFactory;", "setWorkerFactory", "(Landroidx/hilt/work/HiltWorkerFactory;)V", "installCrashHandler", "", "onCreate", "Companion", "app_debug"})
public final class SafeGuardApplication extends android.app.Application implements androidx.work.Configuration.Provider {
    @javax.inject.Inject
    public com.safeguard.core.domain.crash.CrashReporter crashReporter;
    @javax.inject.Inject
    public androidx.hilt.work.HiltWorkerFactory workerFactory;
    @javax.inject.Inject
    public com.safeguard.data.local.preferences.SecurePreferencesManager preferences;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SafeGuard";
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.SafeGuardApplication.Companion Companion = null;
    
    public SafeGuardApplication() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.core.domain.crash.CrashReporter getCrashReporter() {
        return null;
    }
    
    public final void setCrashReporter(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.crash.CrashReporter p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.hilt.work.HiltWorkerFactory getWorkerFactory() {
        return null;
    }
    
    public final void setWorkerFactory(@org.jetbrains.annotations.NotNull
    androidx.hilt.work.HiltWorkerFactory p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.preferences.SecurePreferencesManager getPreferences() {
        return null;
    }
    
    public final void setPreferences(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager p0) {
    }
    
    @java.lang.Override
    public void onCreate() {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public androidx.work.Configuration getWorkManagerConfiguration() {
        return null;
    }
    
    private final void installCrashHandler() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/safeguard/SafeGuardApplication$Companion;", "", "()V", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}