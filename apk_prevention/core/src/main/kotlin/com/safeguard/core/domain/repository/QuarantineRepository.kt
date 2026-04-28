package com.safeguard.core.domain.repository

import com.safeguard.core.domain.model.ScanResult

interface QuarantineRepository {
    suspend fun quarantine(apkPath: String, result: ScanResult): QuarantineRecord
    suspend fun deleteFromQuarantine(id: String)
    suspend fun permanentlyDelete(id: String)
    suspend fun deleteAndBlockApk(apkPath: String, result: ScanResult)
    suspend fun isApkBlocked(apkName: String, apkSha256: String? = null): Boolean
    suspend fun restoreFromQuarantine(id: String): String?
    fun getQuarantineList(): kotlinx.coroutines.flow.Flow<List<QuarantineRecord>>
    suspend fun getQuarantineCount(): Int
    suspend fun getAutoDeleteCountdown(): Int
    suspend fun clearAllQuarantine()
}

data class QuarantineRecord(
    val id: String,
    val originalPath: String,
    val quarantinePath: String,
    val apkHash: String,
    val threatName: String?,
    val apkName: String?,
    val riskScore: Int,
    val quarantinedAt: Long,
    val autoDeleteAt: Long,
    val sizeBytes: Long
)
