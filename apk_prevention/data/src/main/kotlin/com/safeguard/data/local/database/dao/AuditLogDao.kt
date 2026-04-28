package com.safeguard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safeguard.data.local.database.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AuditLogEntity)

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<AuditLogEntity>

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<AuditLogEntity>>

    @Query("DELETE FROM audit_log")
    suspend fun deleteAll()
}
