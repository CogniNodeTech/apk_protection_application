package com.safeguard.worker;

import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.hilt.work.HiltWorker;
import androidx.work.CoroutineWorker;
import androidx.work.WorkerParameters;
import com.safeguard.core.domain.model.Action;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
import com.safeguard.core.domain.usecase.ScanAPKUseCase;
import com.safeguard.notification.SafeGuardNotificationManager;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import kotlinx.coroutines.Dispatchers;
import android.util.Log;
import java.io.File;
import java.security.MessageDigest;
import java.util.UUID;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\b\u0007\u0018\u0000 \u001b2\u00020\u0001:\u0001\u001bB3\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u000e\u0010\r\u001a\u00020\u000eH\u0096@\u00a2\u0006\u0002\u0010\u000fJ\u0012\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0002J\u001e\u0010\u0014\u001a\u0010\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u0017\u0018\u00010\u00152\u0006\u0010\u0018\u001a\u00020\u0011H\u0002J\u0012\u0010\u0019\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u001a\u001a\u00020\u0016H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/safeguard/worker/ApkScanWorker;", "Landroidx/work/CoroutineWorker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "scanAPKUseCase", "Lcom/safeguard/core/domain/usecase/ScanAPKUseCase;", "quarantineAPKUseCase", "Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;", "quarantineRepository", "Lcom/safeguard/core/domain/repository/QuarantineRepository;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;Lcom/safeguard/core/domain/usecase/ScanAPKUseCase;Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;Lcom/safeguard/core/domain/repository/QuarantineRepository;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDisplayName", "", "uri", "Landroid/net/Uri;", "resolveToFile", "Lkotlin/Pair;", "Ljava/io/File;", "", "pathOrUri", "sha256", "file", "Companion", "app_debug"})
@androidx.hilt.work.HiltWorker
public final class ApkScanWorker extends androidx.work.CoroutineWorker {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.usecase.ScanAPKUseCase scanAPKUseCase = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.usecase.QuarantineAPKUseCase quarantineAPKUseCase = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SafeGuard";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String KEY_APK_PATH = "apk_path";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String KEY_APP_NAME = "app_name";
    private static final int MAX_RUN_ATTEMPTS = 3;
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.worker.ApkScanWorker.Companion Companion = null;
    
    @dagger.assisted.AssistedInject
    public ApkScanWorker(@dagger.assisted.Assisted
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @dagger.assisted.Assisted
    @org.jetbrains.annotations.NotNull
    androidx.work.WorkerParameters params, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.usecase.ScanAPKUseCase scanAPKUseCase, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.usecase.QuarantineAPKUseCase quarantineAPKUseCase, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository) {
        super(null, null);
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object doWork(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> $completion) {
        return null;
    }
    
    /**
     * Resolves display name from a content URI (e.g. "MyApp.apk") for use in scan result.
     */
    private final java.lang.String getDisplayName(android.net.Uri uri) {
        return null;
    }
    
    /**
     * Returns (File, isTemp) or null. If pathOrUri starts with content://, copies to a temp file.
     */
    private final kotlin.Pair<java.io.File, java.lang.Boolean> resolveToFile(java.lang.String pathOrUri) {
        return null;
    }
    
    private final java.lang.String sha256(java.io.File file) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/safeguard/worker/ApkScanWorker$Companion;", "", "()V", "KEY_APK_PATH", "", "KEY_APP_NAME", "MAX_RUN_ATTEMPTS", "", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}