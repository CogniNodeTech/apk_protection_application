package com.safeguard.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.WorkManager;
import com.safeguard.MainActivity;
import com.safeguard.security.layers.layer1.FileSystemMonitor;
import com.safeguard.worker.ApkScanWorker;
import java.io.File;

/**
 * Foreground service that runs the file system monitor for real-time APK detection.
 * Monitors all accessible public directories and common app subfolders (Downloads, Documents,
 * Telegram, WhatsApp, etc.). When an APK is detected, enqueues a one-time scan via WorkManager.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0004\u0018\u0000 \u001e2\u00020\u0001:\u0001\u001eB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\f\u001a\u00020\rH\u0002J\b\u0010\u000e\u001a\u00020\u000fH\u0002J\u0010\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u000bH\u0002J\b\u0010\u0012\u001a\u00020\u0013H\u0002J\u0014\u0010\u0014\u001a\u0004\u0018\u00010\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0016J\b\u0010\u0018\u001a\u00020\rH\u0016J\b\u0010\u0019\u001a\u00020\rH\u0016J\"\u0010\u001a\u001a\u00020\u001b2\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\u0006\u0010\u001c\u001a\u00020\u001b2\u0006\u0010\u001d\u001a\u00020\u001bH\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00040\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/safeguard/service/FileObserverService;", "Landroid/app/Service;", "()V", "debounceMs", "", "mediaStoreObserver", "Lcom/safeguard/service/MediaStoreApkObserver;", "monitor", "Lcom/safeguard/security/layers/layer1/FileSystemMonitor;", "recentEnqueues", "", "", "createChannel", "", "createNotification", "Landroid/app/Notification;", "enqueueScan", "pathOrUri", "hasStoragePermission", "", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onStartCommand", "", "flags", "startId", "Companion", "app_debug"})
public final class FileObserverService extends android.app.Service {
    @org.jetbrains.annotations.Nullable
    private com.safeguard.security.layers.layer1.FileSystemMonitor monitor;
    @org.jetbrains.annotations.Nullable
    private com.safeguard.service.MediaStoreApkObserver mediaStoreObserver;
    
    /**
     * Debounce: avoid enqueueing the same path/URI within this window (FileObserver can fire CREATE + CLOSE_WRITE).
     */
    @org.jetbrains.annotations.NotNull
    private final java.util.Map<java.lang.String, java.lang.Long> recentEnqueues = null;
    private final long debounceMs = 3000L;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SafeGuard";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String CHANNEL_ID = "safeguard_monitor";
    private static final int NOTIFICATION_ID = 1001;
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.service.FileObserverService.Companion Companion = null;
    
    public FileObserverService() {
        super();
    }
    
    @java.lang.Override
    public void onCreate() {
    }
    
    private final boolean hasStoragePermission() {
        return false;
    }
    
    private final void enqueueScan(java.lang.String pathOrUri) {
    }
    
    @java.lang.Override
    public int onStartCommand(@org.jetbrains.annotations.Nullable
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    @java.lang.Override
    public void onDestroy() {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable
    android.content.Intent intent) {
        return null;
    }
    
    private final void createChannel() {
    }
    
    private final android.app.Notification createNotification() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/safeguard/service/FileObserverService$Companion;", "", "()V", "CHANNEL_ID", "", "NOTIFICATION_ID", "", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}