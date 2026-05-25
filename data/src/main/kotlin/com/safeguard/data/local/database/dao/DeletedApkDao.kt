package com.safeguard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safeguard.data.local.database.entity.DeletedApkEntity

@Dao
interface DeletedApkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DeletedApkEntity)

    @Query("SELECT COUNT(*) > 0 FROM deleted_apks WHERE apkName = :apkName")
    suspend fun isApkBlocked(apkName: String): Boolean

    @Query("SELECT COUNT(*) > 0 FROM deleted_apks WHERE apkSha256 = :apkSha256")
    suspend fun isApkHashBlocked(apkSha256: String): Boolean

    @Query("SELECT * FROM deleted_apks ORDER BY deletedAt DESC")
    suspend fun getAll(): List<DeletedApkEntity>

    @Query("DELETE FROM deleted_apks")
    suspend fun deleteAll()
}
