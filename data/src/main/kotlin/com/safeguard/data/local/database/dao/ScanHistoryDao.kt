package com.safeguard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safeguard.data.local.database.entity.ScanRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScanRecordEntity)

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getById(id: String): ScanRecordEntity?

    @Query("SELECT * FROM scan_history ORDER BY scanTimestamp DESC")
    fun getAllFlow(): Flow<List<ScanRecordEntity>>

    @Query("SELECT * FROM scan_history ORDER BY scanTimestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ScanRecordEntity>

    @Query("SELECT * FROM scan_history ORDER BY scanTimestamp DESC")
    suspend fun getAll(): List<ScanRecordEntity>

    @Query("SELECT COUNT(*) FROM scan_history WHERE scanTimestamp >= :since")
    suspend fun getCountSince(since: Long): Int

    @Query("SELECT COUNT(*) FROM scan_history WHERE scanTimestamp >= :since AND wasBlocked = 1")
    suspend fun getBlockedCountSince(since: Long): Int

    @Query("DELETE FROM scan_history")
    suspend fun deleteAll()
}
