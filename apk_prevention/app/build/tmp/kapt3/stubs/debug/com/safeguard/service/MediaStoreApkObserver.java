package com.safeguard.service;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Observes MediaStore (Downloads) for new APK files on Android 10+.
 * When the content provider changes, debounces and queries for recent APKs, then invokes
 * [onApkFound] with the content URI (or path if available). Scoped storage limits what we can
 * access; this improves coverage for standard download locations.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010#\n\u0002\b\u0006\u0018\u00002\u00020\u0001B!\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u00a2\u0006\u0002\u0010\bJ\u0012\u0010\u0016\u001a\u00020\u00072\b\b\u0002\u0010\u0017\u001a\u00020\u0013H\u0002J\b\u0010\u0018\u001a\u00020\u0007H\u0002J\u0006\u0010\u0019\u001a\u00020\u0007J\u0006\u0010\u001a\u001a\u00020\u0007R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082D\u00a2\u0006\u0002\n\u0000R\u0016\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\f0\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/safeguard/service/MediaStoreApkObserver;", "", "contentResolver", "Landroid/content/ContentResolver;", "onApkFound", "Lkotlin/Function1;", "", "", "(Landroid/content/ContentResolver;Lkotlin/jvm/functions/Function1;)V", "contentObserver", "Landroid/database/ContentObserver;", "debounceMs", "", "debounceRunnable", "Ljava/util/concurrent/atomic/AtomicReference;", "Ljava/lang/Runnable;", "handler", "Landroid/os/Handler;", "initialQueryDone", "", "seenIds", "", "queryRecentApks", "notifyNewOnly", "scheduleQuery", "start", "stop", "app_debug"})
public final class MediaStoreApkObserver {
    @org.jetbrains.annotations.NotNull
    private final android.content.ContentResolver contentResolver = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onApkFound = null;
    @org.jetbrains.annotations.NotNull
    private final android.os.Handler handler = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.concurrent.atomic.AtomicReference<java.lang.Runnable> debounceRunnable = null;
    @org.jetbrains.annotations.Nullable
    private android.database.ContentObserver contentObserver;
    @org.jetbrains.annotations.NotNull
    private final java.util.Set<java.lang.Long> seenIds = null;
    private boolean initialQueryDone = false;
    private final long debounceMs = 800L;
    
    public MediaStoreApkObserver(@org.jetbrains.annotations.NotNull
    android.content.ContentResolver contentResolver, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onApkFound) {
        super();
    }
    
    public final void start() {
    }
    
    public final void stop() {
    }
    
    private final void scheduleQuery() {
    }
    
    /**
     * @param notifyNewOnly if false, only populate seenIds (initial run). if true, call onApkFound for new ids.
     */
    private final void queryRecentApks(boolean notifyNewOnly) {
    }
}