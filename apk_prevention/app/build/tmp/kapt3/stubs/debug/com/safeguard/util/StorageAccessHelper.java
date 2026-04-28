package com.safeguard.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.Manifest;

/**
 * Deep storage scanning needs broad read access. On Android 11+ this maps to
 * [Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION] when available.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\t"}, d2 = {"Lcom/safeguard/util/StorageAccessHelper;", "", "()V", "canReadStorageDeep", "", "context", "Landroid/content/Context;", "createManageAllFilesIntent", "Landroid/content/Intent;", "app_debug"})
public final class StorageAccessHelper {
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.util.StorageAccessHelper INSTANCE = null;
    
    private StorageAccessHelper() {
        super();
    }
    
    public final boolean canReadStorageDeep(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return false;
    }
    
    /**
     * Open system screen to grant “All files access” for this app (Android 11+).
     */
    @org.jetbrains.annotations.NotNull
    public final android.content.Intent createManageAllFilesIntent(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
}