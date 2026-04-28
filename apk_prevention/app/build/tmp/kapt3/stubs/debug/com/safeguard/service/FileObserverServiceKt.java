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

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\u001a\u0016\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u00012\u0006\u0010\u0003\u001a\u00020\u0004H\u0002\u00a8\u0006\u0005"}, d2 = {"buildRecursiveRoots", "", "Ljava/io/File;", "hasAllFilesAccess", "", "app_debug"})
public final class FileObserverServiceKt {
    
    /**
     * Roots monitored when [Environment.isExternalStorageManager] is granted. A single recursive
     * watcher on the storage root replaces the previous ~200 flat watchers and removes the silent
     * blind spot for nested folders such as
     * `Android/media/<pkg>/Telegram/Telegram Documents/<file>.apk`.
     */
    private static final java.util.List<java.io.File> buildRecursiveRoots(boolean hasAllFilesAccess) {
        return null;
    }
}