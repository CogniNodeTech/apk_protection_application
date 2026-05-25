package com.safeguard.core.domain.model

import java.io.File

/**
 * Shared context for an APK scan. 
 * Prevents redundant I/O by caching the file reference and frequently used metadata (like hashes).
 */
data class APKContext(
    val apkFile: File,
    val cache: MutableMap<String, Any> = mutableMapOf()
) {
    val apkName: String get() = apkFile.name
    val apkPath: String get() = apkFile.absolutePath
    val apkSizeBytes: Long get() = apkFile.length()

    /** Lazy-loaded bytes of the APK. Returns null if file is too large (> 100MB) to avoid OOM. */
    val apkBytes: ByteArray? by lazy {
        if (apkSizeBytes > 100 * 1024 * 1024) null
        else apkFile.readBytes()
    }

    fun getInputStream() = apkFile.inputStream()

    @Suppress("UNCHECKED_CAST")
    fun <T> getCached(key: String): T? = cache[key] as? T
    
    fun putCached(key: String, value: Any) {
        cache[key] = value
    }

    companion object {
        const val KEY_SHA256 = "sha256"

        /**
         * SHA-512 digest of the APK file (lowercase hex, no `0x` prefix). Computed lazily by
         * [com.safeguard.security.layers.layer2.HashValidator] alongside [KEY_SHA256] in a
         * single I/O pass — once a layer reads either value through [getCached], both will
         * be present in the cache. Used by the collision-detection branch (Phase 2.5): a
         * SHA-256 hit on a known-malware row is cross-checked against the row's stored
         * SHA-512, and a mismatch downgrades the verdict from MALICIOUS to SUSPICIOUS
         * (since either the local file is a SHA-256 collision artefact or the threat-DB
         * row was tampered with — both warrant suspicion, neither warrants a confident
         * malware label).
         */
        const val KEY_SHA512 = "sha512"

        const val KEY_PACKAGE_NAME = "package_name"
        const val KEY_CERT_HASH = "cert_hash"
        const val KEY_RASP_THREAT = "rasp_threat_level"
        const val KEY_INSTALLER_SOURCE = "installer_source"
    }
}
