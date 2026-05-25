package com.safeguard.core.domain.repository

import com.safeguard.core.domain.model.ScanResult

interface ScanRepository {
    suspend fun saveScanResult(result: ScanResult)
    suspend fun getScanResult(id: String): ScanResult?
    fun getScanHistory(): kotlinx.coroutines.flow.Flow<List<ScanResult>>
    suspend fun getRecentScans(limit: Int): List<ScanResult>
    /** All scan records for user export (access / portability requests). */
    suspend fun getAllScanResultsForExport(): List<ScanResult>
    suspend fun getScanCountSince(timestamp: Long): Int
    suspend fun getBlockedCountSince(timestamp: Long): Int
    suspend fun clearScanHistory()
}
