package com.safeguard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safeguard.data.local.database.entity.TrustedAppEntity

@Dao
interface TrustedAppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TrustedAppEntity)

    @Query("SELECT * FROM trusted_apps WHERE sha256 = :sha256 LIMIT 1")
    suspend fun findBySha256(sha256: String): TrustedAppEntity?
}
