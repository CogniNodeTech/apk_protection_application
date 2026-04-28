package com.safeguard.core.domain.repository

interface ThreatDatabaseRepository {
    suspend fun findMalwareBySha256(sha256: String): MalwareSignature?
    suspend fun findTrustedBySha256(sha256: String): TrustedApp?
    suspend fun findSimilarByFuzzyHash(fuzzyHash: String, minSimilarity: Int): List<FuzzyMatch>
    suspend fun isTrustedExpired(trustedApp: TrustedApp): Boolean
}

data class MalwareSignature(
    val sha256: String,
    val sha512: String?,
    val fuzzyHash: String?,
    val threatName: String,
    val threatFamily: String?,
    val severity: Int,
    val firstSeen: Long?,
    val source: String?
)

data class TrustedApp(
    val id: String,
    val sha256: String,
    val packageName: String,
    val addedAt: Long,
    val expiresAt: Long
)

data class FuzzyMatch(
    val threatName: String,
    val similarity: Int
)
