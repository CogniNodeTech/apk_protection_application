package com.safeguard.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Append-only audit log entry for scan verdicts and actions (forensics/support). */
@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val scanId: String,
    val apkName: String,
    val verdict: String,
    val action: String,
    val riskScore: Int
)
