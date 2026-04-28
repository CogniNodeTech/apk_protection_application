package com.safeguard.data.remote.dto

import com.squareup.moshi.Json

data class VerificationRequest(
    @Json(name = "apk_hash_sha256") val apkHashSha256: String,
    @Json(name = "apk_hash_sha512") val apkHashSha512: String,
    @Json(name = "package_name") val packageName: String,
    @Json(name = "version_code") val versionCode: Int,
    @Json(name = "permissions") val permissions: List<String>,
    @Json(name = "file_size") val fileSize: Long,
    @Json(name = "target_sdk") val targetSdk: Int,
    @Json(name = "signature_fingerprint") val signatureFingerprint: String?,
    @Json(name = "local_layer_scores") val localLayerScores: LocalLayerScoresJson,
    @Json(name = "device_metadata") val deviceMetadata: DeviceMetadataJson,
    @Json(name = "timestamp") val timestamp: Long
)

data class LocalLayerScoresJson(
    @Json(name = "layer2_hash_result") val layer2HashResult: String,
    @Json(name = "layer3_permission_score") val layer3PermissionScore: Int,
    @Json(name = "layer4_signature_score") val layer4SignatureScore: Int,
    @Json(name = "layer5_ml_probability") val layer5MlProbability: Double
)

data class DeviceMetadataJson(
    @Json(name = "android_version") val androidVersion: Int,
    @Json(name = "device_locale") val deviceLocale: String
)
