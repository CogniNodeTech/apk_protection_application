package com.safeguard.core.domain.repository

interface CloudVerificationRepository {
    suspend fun verify(
        sha256: String,
        sha512: String,
        packageName: String,
        versionCode: Int,
        permissions: List<String>,
        fileSize: Long,
        targetSdk: Int,
        signatureFingerprint: String?,
        localLayerScores: LocalLayerScores,
        deviceMetadata: DeviceCloudMetadata
    ): CloudVerificationResponse
}

data class CloudVerificationResponse(
    val verdict: String,
    val confidence: Float,
    val threatName: String?,
    val threatFamily: String?,
    val avDetections: Int?,
    val totalAvScanned: Int?,
    val communityReports: Int?,
    val virustotalLink: String?,
    val evidence: List<String>,
    val recommendation: String
)
