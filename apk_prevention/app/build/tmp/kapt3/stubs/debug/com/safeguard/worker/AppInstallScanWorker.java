package com.safeguard.worker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.hilt.work.HiltWorker;
import androidx.work.CoroutineWorker;
import androidx.work.WorkerParameters;
import com.safeguard.core.domain.model.Action;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
import com.safeguard.core.domain.usecase.ScanAPKUseCase;
import com.safeguard.notification.SafeGuardNotificationManager;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import kotlinx.coroutines.Dispatchers;
import java.io.File;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB+\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u000e\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/safeguard/worker/AppInstallScanWorker;", "Landroidx/work/CoroutineWorker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "scanAPKUseCase", "Lcom/safeguard/core/domain/usecase/ScanAPKUseCase;", "quarantineAPKUseCase", "Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;Lcom/safeguard/core/domain/usecase/ScanAPKUseCase;Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
@androidx.hilt.work.HiltWorker
public final class AppInstallScanWorker extends androidx.work.CoroutineWorker {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.usecase.ScanAPKUseCase scanAPKUseCase = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.usecase.QuarantineAPKUseCase quarantineAPKUseCase = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "AppInstallScanWorker";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String KEY_PACKAGE_NAME = "package_name";
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.worker.AppInstallScanWorker.Companion Companion = null;
    
    @dagger.assisted.AssistedInject
    public AppInstallScanWorker(@dagger.assisted.Assisted
    @org.jetbrains.annotations.NotNull
    android.content.Context context, @dagger.assisted.Assisted
    @org.jetbrains.annotations.NotNull
    androidx.work.WorkerParameters params, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.usecase.ScanAPKUseCase scanAPKUseCase, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.usecase.QuarantineAPKUseCase quarantineAPKUseCase) {
        super(null, null);
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object doWork(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/safeguard/worker/AppInstallScanWorker$Companion;", "", "()V", "KEY_PACKAGE_NAME", "", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}