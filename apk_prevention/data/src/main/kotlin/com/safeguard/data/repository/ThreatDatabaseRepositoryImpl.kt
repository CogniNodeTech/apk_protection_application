package com.safeguard.data.repository

import com.safeguard.core.domain.repository.FuzzyMatch
import com.safeguard.core.domain.repository.MalwareSignature
import com.safeguard.core.domain.repository.ThreatDatabaseRepository
import com.safeguard.core.domain.repository.TrustedApp
import com.safeguard.core.util.FuzzyHasher
import com.safeguard.data.local.database.dao.MalwareSignatureDao
import com.safeguard.data.local.database.dao.TrustedAppDao
import com.safeguard.data.local.database.entity.MalwareSignatureEntity
import com.safeguard.data.local.database.entity.TrustedAppEntity
import javax.inject.Inject

class ThreatDatabaseRepositoryImpl @Inject constructor(
    private val malwareDao: MalwareSignatureDao,
    private val trustedAppDao: TrustedAppDao
) : ThreatDatabaseRepository {

    override suspend fun findMalwareBySha256(sha256: String): MalwareSignature? {
        return malwareDao.findBySha256(sha256)?.toDomain()
    }

    override suspend fun findTrustedBySha256(sha256: String): TrustedApp? {
        return trustedAppDao.findBySha256(sha256)?.toDomain()
    }

    override suspend fun findSimilarByFuzzyHash(fuzzyHash: String, minSimilarity: Int): List<FuzzyMatch> {
        if (fuzzyHash.isBlank()) return emptyList()
        // O(N) linear scan over all rows that have a fuzzy hash. Acceptable in practice because
        // (a) fuzzy lookup is gated by SHA-256 miss + trusted miss in HashValidator, and
        // (b) the malware table is bounded by what we ship + ingest from the cloud (low thousands).
        // If/when the row count grows past ~50k, switch to bucketed lookups (e.g. by lValue / q1).
        val all = malwareDao.getAllWithFuzzyHash()
        return all.mapNotNull { entity ->
            val candidate = entity.fuzzyHash ?: return@mapNotNull null
            val sim = FuzzyHasher.similarity(fuzzyHash, candidate)
            if (sim >= minSimilarity) FuzzyMatch(entity.threatName, sim) else null
        }
    }

    override suspend fun isTrustedExpired(trustedApp: TrustedApp): Boolean {
        return System.currentTimeMillis() > trustedApp.expiresAt
    }
}

private fun MalwareSignatureEntity.toDomain() = MalwareSignature(
    sha256 = sha256,
    sha512 = sha512,
    fuzzyHash = fuzzyHash,
    threatName = threatName,
    threatFamily = threatFamily,
    severity = severity,
    firstSeen = firstSeen,
    source = source
)

private fun TrustedAppEntity.toDomain() = TrustedApp(
    id = id,
    sha256 = sha256,
    packageName = packageName,
    addedAt = addedAt,
    expiresAt = expiresAt
)
