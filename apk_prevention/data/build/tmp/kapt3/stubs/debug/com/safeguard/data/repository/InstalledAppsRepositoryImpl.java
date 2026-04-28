package com.safeguard.data.repository;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import com.safeguard.core.domain.model.UnknownSourceApp;
import com.safeguard.core.domain.repository.InstalledAppsRepository;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\u0011\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u0096@\u00a2\u0006\u0002\u0010\bJ\u001a\u0010\t\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\nH\u0002J\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u0096@\u00a2\u0006\u0002\u0010\bJ\u001a\u0010\u000f\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\nH\u0002J\u0014\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060\u0011H\u0016J\u0014\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060\u0011H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/safeguard/data/repository/InstalledAppsRepositoryImpl;", "Lcom/safeguard/core/domain/repository/InstalledAppsRepository;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "getAppsFromUnknownSources", "", "Lcom/safeguard/core/domain/model/UnknownSourceApp;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getInstallerPackageName", "", "pm", "Landroid/content/pm/PackageManager;", "packageName", "getMissedUpdateApps", "getVersionName", "observeMissedUpdates", "Lkotlinx/coroutines/flow/Flow;", "observeUnknownSourceApps", "Companion", "data_debug"})
public final class InstalledAppsRepositoryImpl implements com.safeguard.core.domain.repository.InstalledAppsRepository {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    @java.lang.Deprecated
    public static final java.lang.String PLAY_STORE_INSTALLER = "com.android.vending";
    @org.jetbrains.annotations.NotNull
    private static final com.safeguard.data.repository.InstalledAppsRepositoryImpl.Companion Companion = null;
    
    @javax.inject.Inject
    public InstalledAppsRepositoryImpl(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getAppsFromUnknownSources(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.safeguard.core.domain.model.UnknownSourceApp>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.flow.Flow<java.util.List<com.safeguard.core.domain.model.UnknownSourceApp>> observeUnknownSourceApps() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getMissedUpdateApps(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.safeguard.core.domain.model.UnknownSourceApp>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.flow.Flow<java.util.List<com.safeguard.core.domain.model.UnknownSourceApp>> observeMissedUpdates() {
        return null;
    }
    
    private final java.lang.String getInstallerPackageName(android.content.pm.PackageManager pm, java.lang.String packageName) {
        return null;
    }
    
    private final java.lang.String getVersionName(android.content.pm.PackageManager pm, java.lang.String packageName) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0082\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/safeguard/data/repository/InstalledAppsRepositoryImpl$Companion;", "", "()V", "PLAY_STORE_INSTALLER", "", "data_debug"})
    static final class Companion {
        
        private Companion() {
            super();
        }
    }
}