package com.safeguard.core.domain.model

data class UnknownSourceApp(
    val packageName: String,
    val appName: String,
    val versionName: String?,
    val installerPackage: String?,
    val installTime: Long,
    val lastUpdateTime: Long
)

