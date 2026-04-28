package com.safeguard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.work.Data;
import androidx.work.WorkManager;
import com.safeguard.worker.AppInstallScanWorker;

/**
 * Triggers a background scan whenever an APK is **installed** or **updated** on the device.
 *
 * Listens for two complementary system broadcasts:
 *  - [Intent.ACTION_PACKAGE_ADDED]    — fresh installs (and updates with EXTRA_REPLACING=true,
 *                                       which we de-duplicate against PACKAGE_REPLACED).
 *  - [Intent.ACTION_PACKAGE_REPLACED] — completed package updates. Scanned because malicious
 *                                       actors weaponize updates of previously-benign apps
 *                                       (a recurring real-world Android campaign vector).
 *
 * Intentionally excludes [Intent.ACTION_PACKAGE_REMOVED] / [Intent.ACTION_PACKAGE_FULLY_REMOVED]
 * — there is no APK to scan once the package is uninstalled.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \r2\u00020\u0001:\u0001\rB\u0005\u00a2\u0006\u0002\u0010\u0002J \u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\bH\u0002J\u0018\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\fH\u0016\u00a8\u0006\u000e"}, d2 = {"Lcom/safeguard/receiver/AppInstallReceiver;", "Landroid/content/BroadcastReceiver;", "()V", "enqueueScan", "", "context", "Landroid/content/Context;", "packageName", "", "reason", "onReceive", "intent", "Landroid/content/Intent;", "Companion", "app_debug"})
public final class AppInstallReceiver extends android.content.BroadcastReceiver {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "AppInstallReceiver";
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.receiver.AppInstallReceiver.Companion Companion = null;
    
    public AppInstallReceiver() {
        super();
    }
    
    @java.lang.Override
    public void onReceive(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    android.content.Intent intent) {
    }
    
    private final void enqueueScan(android.content.Context context, java.lang.String packageName, java.lang.String reason) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/safeguard/receiver/AppInstallReceiver$Companion;", "", "()V", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}