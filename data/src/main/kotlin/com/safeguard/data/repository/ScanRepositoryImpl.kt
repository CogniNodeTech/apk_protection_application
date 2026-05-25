package com.safeguard.data.repository

import com.safeguard.core.domain.model.Action
import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.model.ThreatInfo
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.repository.ScanRepository
import com.safeguard.data.local.cache.LayerResultDto
import com.safeguard.data.local.database.dao.AuditLogDao
import com.safeguard.data.local.database.dao.ScanHistoryDao
import com.safeguard.data.local.database.entity.AuditLogEntity
import com.safeguard.data.local.database.entity.ScanRecordEntity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScanRepositoryImpl @Inject constructor(
    private val scanHistoryDao: ScanHistoryDao,
    private val auditLogDao: AuditLogDao,
    moshi: Moshi
) : ScanRepository {

    private val layerListAdapter: JsonAdapter<List<LayerResultDto>> =
        moshi.adapter(
            Types.newParameterizedType(List::class.java, LayerResultDto::class.java)
        )

    override suspend fun saveScanResult(result: ScanResult) {
        val dtos = result.layerResults.map { layer ->
            LayerResultDto(
                layerId = layer.layerId,
                layerName = layer.layerName,
                verdict = layer.verdict.name,
                confidence = layer.confidence,
                riskScore = layer.riskScore,
                evidence = layer.evidence,
                executionTimeMs = layer.executionTimeMs,
                threatName = layer.threatInfo?.threatName,
                threatFamily = layer.threatInfo?.threatFamily,
                threatRiskScore = layer.threatInfo?.severity
            )
        }
        val entity = ScanRecordEntity(
            id = result.id,
            apkHash = "", // optional: hash can be computed when needed
            apkName = result.apkName,
            apkPath = result.apkPath,
            scanTimestamp = result.scanTimestamp,
            finalVerdict = result.finalVerdict.name,
            riskScore = result.overallRiskScore,
            layerResultsJson = layerListAdapter.toJson(dtos),
            wasBlocked = result.recommendedAction == Action.BLOCK || result.recommendedAction == Action.QUARANTINE
        )
        scanHistoryDao.insert(entity)
        auditLogDao.insert(
            AuditLogEntity(
                timestamp = result.scanTimestamp,
                scanId = result.id,
                apkName = result.apkName,
                verdict = result.finalVerdict.name,
                action = result.recommendedAction.name,
                riskScore = result.overallRiskScore
            )
        )
    }

    override suspend fun getScanResult(id: String): ScanResult? {
        val entity = scanHistoryDao.getById(id) ?: return null
        return entity.toDomain(layerListAdapter) ?: return null
    }

    override fun getScanHistory(): Flow<List<ScanResult>> =
        scanHistoryDao.getAllFlow().map { list -> list.mapNotNull { it.toDomain(layerListAdapter) } }

    override suspend fun getRecentScans(limit: Int): List<ScanResult> {
        return scanHistoryDao.getRecent(limit).mapNotNull { it.toDomain(layerListAdapter) }
    }

    override suspend fun getAllScanResultsForExport(): List<ScanResult> {
        return scanHistoryDao.getAll().mapNotNull { it.toDomain(layerListAdapter) }
    }

    override suspend fun getScanCountSince(timestamp: Long): Int {
        return scanHistoryDao.getCountSince(timestamp)
    }

    override suspend fun getBlockedCountSince(timestamp: Long): Int {
        return scanHistoryDao.getBlockedCountSince(timestamp)
    }

    override suspend fun clearScanHistory() {
        scanHistoryDao.deleteAll()
        auditLogDao.deleteAll()
    }
}

private fun ScanRecordEntity.toDomain(adapter: JsonAdapter<List<LayerResultDto>>): ScanResult? {
    return try {
        val dtos: List<LayerResultDto> = adapter.fromJson(layerResultsJson) ?: emptyList()
        val layerResults = dtos.map { dto ->
            object : LayerResult {
                override val layerId = dto.layerId
                override val layerName = dto.layerName
                override val verdict = runCatching { Verdict.valueOf(dto.verdict) }.getOrDefault(Verdict.UNKNOWN)
                override val confidence = dto.confidence
                override val riskScore = dto.riskScore
                override val evidence = dto.evidence
                override val executionTimeMs = dto.executionTimeMs
                override val threatInfo: ThreatInfo? =
                    if (dto.threatName != null || dto.threatFamily != null || dto.threatRiskScore != null) {
                        ThreatInfo(
                            threatName = dto.threatName,
                            threatFamily = dto.threatFamily,
                            severity = dto.threatRiskScore ?: dto.riskScore,
                            avDetections = null,
                            totalAvScanned = null,
                            communityReports = null
                        )
                    } else null
            }
        }
        val verdict = runCatching { Verdict.valueOf(finalVerdict) }.getOrDefault(Verdict.UNKNOWN)
        ScanResult(
            id = id,
            apkPath = apkPath,
            apkName = apkName,
            apkSizeBytes = 0,
            scanTimestamp = scanTimestamp,
            finalVerdict = verdict,
            overallConfidence = if (layerResults.isEmpty()) 0f else layerResults.map { it.confidence }.average().toFloat(),
            overallRiskScore = riskScore,
            layerResults = layerResults,
            aggregatedEvidence = layerResults.flatMap { it.evidence },
            recommendedAction = if (wasBlocked) Action.QUARANTINE else Action.ALLOW,
            userDecision = null,
            threatInfo = layerResults.lastOrNull { it.threatInfo != null }?.threatInfo
        )
    } catch (e: Exception) {
        null
    }
}
