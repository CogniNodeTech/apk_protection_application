package com.safeguard.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Persisted form of [com.safeguard.core.domain.feedback.ScanFeedbackEvent] (Phase 3.2).
 * Stored in the encrypted SQLCipher DB so the queue is not readable by other on-device
 * processes even with root.
 *
 * Schema notes:
 * - [layerScoresJson] and [triggeredRulesJson] hold serialised JSON (Moshi). Storing them
 *   inline avoids a child table for the rare event payloads we'll actually see (≤ 7 layer
 *   scores per row, typically 0–3 rule names) and keeps the queue self-contained for the
 *   "purge everything" privacy guarantee — `DELETE FROM scan_feedback_queue` reaches all
 *   fields without needing CASCADE plumbing.
 * - [createdAtMs] is indexed because the upload worker drains in FIFO order (oldest
 *   first) so transient outages don't starve the head of the queue.
 * - The primary key is [id] (a UUID) rather than `sha256`. Re-scanning the same APK on
 *   the same device should produce a *new* row; the server collapses by `sha256` after
 *   aggregation. If we keyed on `sha256` we'd lose the time-of-day signal that's useful
 *   for diagnosing "this APK was clean last week, now it's flagged" regressions.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b$\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001Ba\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\u0010\n\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\u0006\u0010\r\u001a\u00020\u0003\u0012\u0006\u0010\u000e\u001a\u00020\u0003\u0012\u0006\u0010\u000f\u001a\u00020\f\u0012\u0006\u0010\u0010\u001a\u00020\f\u00a2\u0006\u0002\u0010\u0011J\t\u0010#\u001a\u00020\u0003H\u00c6\u0003J\t\u0010$\u001a\u00020\fH\u00c6\u0003J\t\u0010%\u001a\u00020\fH\u00c6\u0003J\t\u0010&\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0003H\u00c6\u0003J\t\u0010(\u001a\u00020\u0003H\u00c6\u0003J\t\u0010)\u001a\u00020\tH\u00c6\u0003J\u000b\u0010*\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u0010+\u001a\u0004\u0018\u00010\fH\u00c6\u0003\u00a2\u0006\u0002\u0010!J\t\u0010,\u001a\u00020\u0003H\u00c6\u0003J\t\u0010-\u001a\u00020\u0003H\u00c6\u0003J\u0080\u0001\u0010.\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\b\b\u0002\u0010\r\u001a\u00020\u00032\b\b\u0002\u0010\u000e\u001a\u00020\u00032\b\b\u0002\u0010\u000f\u001a\u00020\f2\b\b\u0002\u0010\u0010\u001a\u00020\fH\u00c6\u0001\u00a2\u0006\u0002\u0010/J\u0013\u00100\u001a\u0002012\b\u00102\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00103\u001a\u00020\fH\u00d6\u0001J\t\u00104\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u000f\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0010\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0013R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\r\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001aR\u0013\u0010\n\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001aR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001aR\u0011\u0010\u000e\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001aR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001aR\u0015\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\n\n\u0002\u0010\"\u001a\u0004\b \u0010!\u00a8\u00065"}, d2 = {"Lcom/safeguard/data/local/database/entity/ScanFeedbackEventEntity;", "", "id", "", "createdAtMs", "", "sha256", "verdict", "confidence", "", "packageName", "versionCode", "", "layerScoresJson", "triggeredRulesJson", "androidApiLevel", "appVersionCode", "(Ljava/lang/String;JLjava/lang/String;Ljava/lang/String;FLjava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;II)V", "getAndroidApiLevel", "()I", "getAppVersionCode", "getConfidence", "()F", "getCreatedAtMs", "()J", "getId", "()Ljava/lang/String;", "getLayerScoresJson", "getPackageName", "getSha256", "getTriggeredRulesJson", "getVerdict", "getVersionCode", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "component1", "component10", "component11", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;JLjava/lang/String;Ljava/lang/String;FLjava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;II)Lcom/safeguard/data/local/database/entity/ScanFeedbackEventEntity;", "equals", "", "other", "hashCode", "toString", "data_debug"})
@androidx.room.Entity(tableName = "scan_feedback_queue", indices = {@androidx.room.Index(value = {"createdAtMs"})})
public final class ScanFeedbackEventEntity {
    @androidx.room.PrimaryKey
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    private final long createdAtMs = 0L;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String sha256 = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String verdict = null;
    private final float confidence = 0.0F;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String packageName = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer versionCode = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String layerScoresJson = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String triggeredRulesJson = null;
    private final int androidApiLevel = 0;
    private final int appVersionCode = 0;
    
    public ScanFeedbackEventEntity(@org.jetbrains.annotations.NotNull
    java.lang.String id, long createdAtMs, @org.jetbrains.annotations.NotNull
    java.lang.String sha256, @org.jetbrains.annotations.NotNull
    java.lang.String verdict, float confidence, @org.jetbrains.annotations.Nullable
    java.lang.String packageName, @org.jetbrains.annotations.Nullable
    java.lang.Integer versionCode, @org.jetbrains.annotations.NotNull
    java.lang.String layerScoresJson, @org.jetbrains.annotations.NotNull
    java.lang.String triggeredRulesJson, int androidApiLevel, int appVersionCode) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    public final long getCreatedAtMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getSha256() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getVerdict() {
        return null;
    }
    
    public final float getConfidence() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getPackageName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getVersionCode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getLayerScoresJson() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getTriggeredRulesJson() {
        return null;
    }
    
    public final int getAndroidApiLevel() {
        return 0;
    }
    
    public final int getAppVersionCode() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    public final int component10() {
        return 0;
    }
    
    public final int component11() {
        return 0;
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
    
    public final float component5() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.local.database.entity.ScanFeedbackEventEntity copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, long createdAtMs, @org.jetbrains.annotations.NotNull
    java.lang.String sha256, @org.jetbrains.annotations.NotNull
    java.lang.String verdict, float confidence, @org.jetbrains.annotations.Nullable
    java.lang.String packageName, @org.jetbrains.annotations.Nullable
    java.lang.Integer versionCode, @org.jetbrains.annotations.NotNull
    java.lang.String layerScoresJson, @org.jetbrains.annotations.NotNull
    java.lang.String triggeredRulesJson, int androidApiLevel, int appVersionCode) {
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