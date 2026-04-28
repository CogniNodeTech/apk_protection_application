package com.safeguard.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanRecordEntity(
    @PrimaryKey val id: String,
    val apkHash: String,
    val apkName: String,
    val apkPath: String,
    val scanTimestamp: Long,
    val finalVerdict: String,
    val riskScore: Int,
    val layerResultsJson: String,
    val wasBlocked: Boolean
)
