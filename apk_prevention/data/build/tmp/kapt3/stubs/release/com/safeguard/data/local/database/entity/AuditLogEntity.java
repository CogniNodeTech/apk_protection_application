package com.safeguard.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Append-only audit log entry for scan verdicts and actions (forensics/support).
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B?\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\u0006\u0010\b\u001a\u00020\u0006\u0012\u0006\u0010\t\u001a\u00020\u0006\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u000bH\u00c6\u0003JO\u0010\u001e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\u00062\b\b\u0002\u0010\t\u001a\u00020\u00062\b\b\u0002\u0010\n\u001a\u00020\u000bH\u00c6\u0001J\u0013\u0010\u001f\u001a\u00020 2\b\u0010!\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\"\u001a\u00020\u000bH\u00d6\u0001J\t\u0010#\u001a\u00020\u0006H\u00d6\u0001R\u0011\u0010\t\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000eR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011R\u0011\u0010\b\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u000e\u00a8\u0006$"}, d2 = {"Lcom/safeguard/data/local/database/entity/AuditLogEntity;", "", "id", "", "timestamp", "scanId", "", "apkName", "verdict", "action", "riskScore", "", "(JJLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V", "getAction", "()Ljava/lang/String;", "getApkName", "getId", "()J", "getRiskScore", "()I", "getScanId", "getTimestamp", "getVerdict", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "toString", "data_release"})
@androidx.room.Entity(tableName = "audit_log")
public final class AuditLogEntity {
    @androidx.room.PrimaryKey(autoGenerate = true)
    private final long id = 0L;
    private final long timestamp = 0L;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String scanId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String apkName = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String verdict = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String action = null;
    private final int riskScore = 0;
    
    public AuditLogEntity(long id, long timestamp, @org.jetbrains.annotations.NotNull
    java.lang.String scanId, @org.jetbrains.annotations.NotNull
    java.lang.String apkName, @org.jetbrains.annotations.NotNull
    java.lang.String verdict, @org.jetbrains.annotations.NotNull
    java.lang.String action, int riskScore) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    public final long getTimestamp() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getScanId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getApkName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getVerdict() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getAction() {
        return null;
    }
    
    public final int getRiskScore() {
        return 0;
    }
    
    public final long component1() {
        return 0L;
    }
    
    public final long component2() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component6() {
        return null;
    }
    
    public final int component7() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.database.entity.AuditLogEntity copy(long id, long timestamp, @org.jetbrains.annotations.NotNull
    java.lang.String scanId, @org.jetbrains.annotations.NotNull
    java.lang.String apkName, @org.jetbrains.annotations.NotNull
    java.lang.String verdict, @org.jetbrains.annotations.NotNull
    java.lang.String action, int riskScore) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}