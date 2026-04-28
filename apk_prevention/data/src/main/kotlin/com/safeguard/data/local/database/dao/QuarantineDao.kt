package com.safeguard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safeguard.data.local.database.entity.QuarantineRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuarantineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QuarantineRecordEntity)

    @Query("DELETE FROM quarantine WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM quarantine WHERE apkName = :apkName")
    suspend fun deleteByApkName(apkName: String)

    @Query("SELECT * FROM quarantine ORDER BY quarantineTimestamp DESC")
    fun getAllFlow(): Flow<List<QuarantineRecordEntity>>

    @Query("SELECT * FROM quarantine WHERE id = :id")
    suspend fun getById(id: String): QuarantineRecordEntity?

    @Query("SELECT COUNT(*) FROM quarantine")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM quarantine WHERE autoDeleteAt <= :before")
    suspend fun getAutoDeleteCount(before: Long): Int

    @Query("DELETE FROM quarantine")
    suspend fun deleteAll()
}
