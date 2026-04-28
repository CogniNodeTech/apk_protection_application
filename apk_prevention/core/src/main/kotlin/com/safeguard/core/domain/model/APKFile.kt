package com.safeguard.core.domain.model

/**
 * Metadata for an APK file (input to scanning).
 */
data class APKFile(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val mimeType: String = "application/vnd.android.package-archive",
    val sourceApp: String? = null,
    val initialRiskScore: Int = 0
)
