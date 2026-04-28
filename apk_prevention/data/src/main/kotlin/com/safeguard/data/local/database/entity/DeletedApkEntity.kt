package com.safeguard.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks APKs that have been permanently deleted by the user.
 * Used to block reinstallation of the same APK through the application.
 */
@Entity(tableName = "deleted_apks")
data class DeletedApkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val apkName: String,
    val apkSha256: String?,
    val originalPath: String,
    val threatName: String?,
    val riskScore: Int,
    val deletedAt: Long
)
