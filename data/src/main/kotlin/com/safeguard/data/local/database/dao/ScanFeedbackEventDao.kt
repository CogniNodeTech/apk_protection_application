package com.safeguard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safeguard.data.local.database.entity.ScanFeedbackEventEntity

@Dao
interface ScanFeedbackEventDao {

    /**
     * Insert one event. `IGNORE` (not REPLACE) on conflict so a (very unlikely) UUID
     * collision doesn't silently drop the *original* row, which would be surprising — the
     * UUID is supposed to be unique. We accept the lost duplicate instead.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ScanFeedbackEventEntity)

    /** FIFO drain so transient outages can't starve old events. */
    @Query("SELECT * FROM scan_feedback_queue ORDER BY createdAtMs ASC LIMIT :limit")
    suspend fun nextBatch(limit: Int): List<ScanFeedbackEventEntity>

    @Query("DELETE FROM scan_feedback_queue WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("SELECT COUNT(*) FROM scan_feedback_queue")
    suspend fun count(): Int

    @Query("DELETE FROM scan_feedback_queue")
    suspend fun deleteAll()
}
