package com.safeguard.core.domain.repository

/**
 * Typed payload for Layer 6 cloud verification (replaces untyped maps for safe JSON with Moshi).
 */
data class LocalLayerScores(
    val layer2HashResult: String,
    val layer3PermissionScore: Int,
    val layer4SignatureScore: Int,
    val layer5MlProbability: Double
)

data class DeviceCloudMetadata(
    val androidVersion: Int,
    val deviceLocale: String
)
