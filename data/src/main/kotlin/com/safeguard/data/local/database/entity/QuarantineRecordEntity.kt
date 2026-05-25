package com.safeguard.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quarantine")
data class QuarantineRecordEntity(
    @PrimaryKey val id: String,
    val originalPath: String,
    val quarantinePath: String,
    val apkHash: String,
    val threatName: String?,
    val apkName: String? = null,
    val riskScore: Int = 0,
    val quarantineTimestamp: Long,
    val autoDeleteAt: Long,
    val sizeBytes: Long
)
