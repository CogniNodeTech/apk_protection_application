package com.safeguard.data.repository;

import com.safeguard.core.domain.model.Action;
import com.safeguard.core.domain.model.LayerResult;
import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.model.ThreatInfo;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.data.local.cache.LayerResultDto;
import com.safeguard.data.local.database.dao.AuditLogDao;
import com.safeguard.data.local.database.dao.ScanHistoryDao;
import com.safeguard.data.local.database.entity.AuditLogEntity;
import com.safeguard.data.local.database.entity.ScanRecordEntity;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\r\u001a\u00020\u000eH\u0096@\u00a2\u0006\u0002\u0010\u000fJ\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u000bH\u0096@\u00a2\u0006\u0002\u0010\u000fJ\u0016\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015H\u0096@\u00a2\u0006\u0002\u0010\u0016J\u001c\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00110\u000b2\u0006\u0010\u0018\u001a\u00020\u0013H\u0096@\u00a2\u0006\u0002\u0010\u0019J\u0016\u0010\u001a\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015H\u0096@\u00a2\u0006\u0002\u0010\u0016J\u0014\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u000b0\u001cH\u0016J\u0018\u0010\u001d\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u001e\u001a\u00020\u001fH\u0096@\u00a2\u0006\u0002\u0010 J\u0016\u0010!\u001a\u00020\u000e2\u0006\u0010\"\u001a\u00020\u0011H\u0096@\u00a2\u0006\u0002\u0010#R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/safeguard/data/repository/ScanRepositoryImpl;", "Lcom/safeguard/core/domain/repository/ScanRepository;", "scanHistoryDao", "Lcom/safeguard/data/local/database/dao/ScanHistoryDao;", "auditLogDao", "Lcom/safeguard/data/local/database/dao/AuditLogDao;", "moshi", "Lcom/squareup/moshi/Moshi;", "(Lcom/safeguard/data/local/database/dao/ScanHistoryDao;Lcom/safeguard/data/local/database/dao/AuditLogDao;Lcom/squareup/moshi/Moshi;)V", "layerListAdapter", "Lcom/squareup/moshi/JsonAdapter;", "", "Lcom/safeguard/data/local/cache/LayerResultDto;", "clearScanHistory", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllScanResultsForExport", "Lcom/safeguard/core/domain/model/ScanResult;", "getBlockedCountSince", "", "timestamp", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRecentScans", "limit", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getScanCountSince", "getScanHistory", "Lkotlinx/coroutines/flow/Flow;", "getScanResult", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveScanResult", "result", "(Lcom/safeguard/core/domain/model/ScanResult;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class ScanRepositoryImpl implements com.safeguard.core.domain.repository.ScanRepository {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.database.dao.ScanHistoryDao scanHistoryDao = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.database.dao.AuditLogDao auditLogDao = null;
    @org.jetbrains.annotations.NotNull
    private final com.squareup.moshi.JsonAdapter<java.util.List<com.safeguard.data.local.cache.LayerResultDto>> layerListAdapter = null;
    
    @javax.inject.Inject
    public ScanRepositoryImpl(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.dao.ScanHistoryDao scanHistoryDao, @org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.dao.AuditLogDao auditLogDao, @org.jetbrains.annotations.NotNull
    com.squareup.moshi.Moshi moshi) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object saveScanResult(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.ScanResult result, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getScanResult(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.safeguard.core.domain.model.ScanResult> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.flow.Flow<java.util.List<com.safeguard.core.domain.model.ScanResult>> getScanHistory() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getRecentScans(int limit, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.safeguard.core.domain.model.ScanResult>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getAllScanResultsForExport(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.safeguard.core.domain.model.ScanResult>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getScanCountSince(long timestamp, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getBlockedCountSince(long timestamp, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object clearScanHistory(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}