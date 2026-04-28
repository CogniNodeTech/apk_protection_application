package com.safeguard.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.safeguard.data.local.database.entity.ScanFeedbackEventEntity;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u0007\u001a\u00020\u00062\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u001c\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000e0\t2\u0006\u0010\u0011\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0012\u00a8\u0006\u0013"}, d2 = {"Lcom/safeguard/data/local/database/dao/ScanFeedbackEventDao;", "", "count", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAll", "", "deleteByIds", "ids", "", "", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insert", "entity", "Lcom/safeguard/data/local/database/entity/ScanFeedbackEventEntity;", "(Lcom/safeguard/data/local/database/entity/ScanFeedbackEventEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "nextBatch", "limit", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
@androidx.room.Dao
public abstract interface ScanFeedbackEventDao {
    
    /**
     * Insert one event. `IGNORE` (not REPLACE) on conflict so a (very unlikely) UUID
     * collision doesn't silently drop the *original* row, which would be surprising — the
     * UUID is supposed to be unique. We accept the lost duplicate instead.
     */
    @androidx.room.Insert(onConflict = 5)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.entity.ScanFeedbackEventEntity entity, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * FIFO drain so transient outages can't starve old events.
     */
    @androidx.room.Query(value = "SELECT * FROM scan_feedback_queue ORDER BY createdAtMs ASC LIMIT :limit")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object nextBatch(int limit, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.safeguard.data.local.database.entity.ScanFeedbackEventEntity>> $completion);
    
    @androidx.room.Query(value = "DELETE FROM scan_feedback_queue WHERE id IN (:ids)")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteByIds(@org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> ids, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM scan_feedback_queue")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object count(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "DELETE FROM scan_feedback_queue")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}