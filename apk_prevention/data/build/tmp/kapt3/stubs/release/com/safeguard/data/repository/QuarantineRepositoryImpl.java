package com.safeguard.data.repository;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.repository.QuarantineRecord;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.data.local.database.dao.DeletedApkDao;
import com.safeguard.data.local.database.dao.QuarantineDao;
import com.safeguard.data.local.database.entity.DeletedApkEntity;
import com.safeguard.data.local.database.entity.QuarantineRecordEntity;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.flow.Flow;
import java.io.File;
import java.security.MessageDigest;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\b\u0018\u0000 &2\u00020\u0001:\u0001&B+\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0001\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u000e\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\rJ\u001e\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u0096@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u0014\u001a\u00020\f2\u0006\u0010\u0015\u001a\u00020\u0010H\u0096@\u00a2\u0006\u0002\u0010\u0016J\u000e\u0010\u0017\u001a\u00020\u0018H\u0096@\u00a2\u0006\u0002\u0010\rJ\u000e\u0010\u0019\u001a\u00020\u0018H\u0096@\u00a2\u0006\u0002\u0010\rJ\u0014\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001d0\u001c0\u001bH\u0016J \u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u00102\b\u0010!\u001a\u0004\u0018\u00010\u0010H\u0096@\u00a2\u0006\u0002\u0010\"J\u0016\u0010#\u001a\u00020\f2\u0006\u0010\u0015\u001a\u00020\u0010H\u0096@\u00a2\u0006\u0002\u0010\u0016J\u001e\u0010$\u001a\u00020\u001d2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u0096@\u00a2\u0006\u0002\u0010\u0013J\u0018\u0010%\u001a\u0004\u0018\u00010\u00102\u0006\u0010\u0015\u001a\u00020\u0010H\u0096@\u00a2\u0006\u0002\u0010\u0016R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/safeguard/data/repository/QuarantineRepositoryImpl;", "Lcom/safeguard/core/domain/repository/QuarantineRepository;", "quarantineDao", "Lcom/safeguard/data/local/database/dao/QuarantineDao;", "deletedApkDao", "Lcom/safeguard/data/local/database/dao/DeletedApkDao;", "quarantineDir", "Ljava/io/File;", "appContext", "Landroid/content/Context;", "(Lcom/safeguard/data/local/database/dao/QuarantineDao;Lcom/safeguard/data/local/database/dao/DeletedApkDao;Ljava/io/File;Landroid/content/Context;)V", "clearAllQuarantine", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAndBlockApk", "apkPath", "", "result", "Lcom/safeguard/core/domain/model/ScanResult;", "(Ljava/lang/String;Lcom/safeguard/core/domain/model/ScanResult;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteFromQuarantine", "id", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAutoDeleteCountdown", "", "getQuarantineCount", "getQuarantineList", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/safeguard/core/domain/repository/QuarantineRecord;", "isApkBlocked", "", "apkName", "apkSha256", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "permanentlyDelete", "quarantine", "restoreFromQuarantine", "Companion", "data_release"})
public final class QuarantineRepositoryImpl implements com.safeguard.core.domain.repository.QuarantineRepository {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.database.dao.QuarantineDao quarantineDao = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.database.dao.DeletedApkDao deletedApkDao = null;
    @org.jetbrains.annotations.NotNull
    private final java.io.File quarantineDir = null;
    @org.jetbrains.annotations.NotNull
    private final android.content.Context appContext = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "QuarantineRepo";
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.data.repository.QuarantineRepositoryImpl.Companion Companion = null;
    
    @javax.inject.Inject
    public QuarantineRepositoryImpl(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.dao.QuarantineDao quarantineDao, @org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.dao.DeletedApkDao deletedApkDao, @javax.inject.Named(value = "quarantine_dir")
    @org.jetbrains.annotations.NotNull
    java.io.File quarantineDir, @dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context appContext) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object quarantine(@org.jetbrains.annotations.NotNull
    java.lang.String apkPath, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.ScanResult result, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.safeguard.core.domain.repository.QuarantineRecord> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object deleteFromQuarantine(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object permanentlyDelete(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object deleteAndBlockApk(@org.jetbrains.annotations.NotNull
    java.lang.String apkPath, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.ScanResult result, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object isApkBlocked(@org.jetbrains.annotations.NotNull
    java.lang.String apkName, @org.jetbrains.annotations.Nullable
    java.lang.String apkSha256, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object restoreFromQuarantine(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.flow.Flow<java.util.List<com.safeguard.core.domain.repository.QuarantineRecord>> getQuarantineList() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getQuarantineCount(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getAutoDeleteCountdown(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object clearAllQuarantine(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0005\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0006\u001a\u00020\u00042\b\u0010\u0007\u001a\u0004\u0018\u00010\bH\u0002J\u0014\u0010\t\u001a\u0004\u0018\u00010\u00042\b\u0010\u0007\u001a\u0004\u0018\u00010\bH\u0002J\u0012\u0010\n\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u000b\u001a\u00020\fH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/safeguard/data/repository/QuarantineRepositoryImpl$Companion;", "", "()V", "TAG", "", "extractOrComputeSha256", "apkPath", "result", "Lcom/safeguard/core/domain/model/ScanResult;", "parseSha256FromEvidence", "sha256", "file", "Ljava/io/File;", "data_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        private final java.lang.String parseSha256FromEvidence(com.safeguard.core.domain.model.ScanResult result) {
            return null;
        }
        
        private final java.lang.String sha256(java.io.File file) {
            return null;
        }
        
        private final java.lang.String extractOrComputeSha256(java.lang.String apkPath, com.safeguard.core.domain.model.ScanResult result) {
            return null;
        }
    }
}