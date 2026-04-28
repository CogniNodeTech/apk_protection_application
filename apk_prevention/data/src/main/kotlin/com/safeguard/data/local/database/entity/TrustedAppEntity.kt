package com.safeguard.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trusted_apps",
    indices = [Index(value = ["sha256"])]
)
data class TrustedAppEntity(
    @PrimaryKey val id: String,
    val sha256: String,
    val packageName: String,
    val addedAt: Long,
    val expiresAt: Long
)
