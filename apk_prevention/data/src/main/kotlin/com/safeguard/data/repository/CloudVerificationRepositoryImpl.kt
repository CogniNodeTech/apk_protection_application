package com.safeguard.data.repository

import com.safeguard.core.domain.repository.CloudVerificationRepository
import com.safeguard.core.domain.repository.CloudVerificationResponse
import com.safeguard.data.remote.api.ThreatIntelligenceApi
import com.safeguard.core.domain.repository.DeviceCloudMetadata
import com.safeguard.core.domain.repository.LocalLayerScores
import com.safeguard.data.remote.dto.DeviceMetadataJson
import com.safeguard.data.remote.dto.LocalLayerScoresJson
import com.safeguard.data.remote.dto.VerificationRequest
import kotlinx.coroutines.delay
import retrofit2.HttpException
import javax.inject.Inject
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

private const val MAX_ATTEMPTS = 3
private const val INITIAL_DELAY_MS = 1000L
private const val CIRCUIT_FAIL_THRESHOLD = 5
private const val CIRCUIT_OPEN_MS = 60_000L

class CloudVerificationRepositoryImpl @Inject constructor(
    private val api: ThreatIntelligenceApi
) : CloudVerificationRepository {
    private val consecutiveFailures = AtomicInteger(0)
    private val circuitOpenUntil = AtomicLong(0L)

    override suspend fun verify(
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
    ): CloudVerificationResponse {
        val now = System.currentTimeMillis()
        if (circuitOpenUntil.get() > now) {
            return CloudVerificationResponse(
                verdict = "UNKNOWN",
                confidence = 0f,
                threatName = null,
                threatFamily = null,
                avDetections = null,
                totalAvScanned = null,
                communityReports = null,
                virustotalLink = null,
                evidence = listOf("Cloud verification temporarily paused (circuit open)"),
                recommendation = "WARN_USER"
            )
        }
        val request = VerificationRequest(
            apkHashSha256 = sha256,
            apkHashSha512 = sha512,
            packageName = packageName,
            versionCode = versionCode,
            permissions = permissions,
            fileSize = fileSize,
            targetSdk = targetSdk,
            signatureFingerprint = signatureFingerprint,
            localLayerScores = LocalLayerScoresJson(
                layer2HashResult = localLayerScores.layer2HashResult,
                layer3PermissionScore = localLayerScores.layer3PermissionScore,
                layer4SignatureScore = localLayerScores.layer4SignatureScore,
                layer5MlProbability = localLayerScores.layer5MlProbability
            ),
            deviceMetadata = DeviceMetadataJson(
                androidVersion = deviceMetadata.androidVersion,
                deviceLocale = deviceMetadata.deviceLocale
            ),
            timestamp = System.currentTimeMillis()
        )
        var delayMs = INITIAL_DELAY_MS
        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                val response = api.verifyAPK(request)
                consecutiveFailures.set(0)
                return CloudVerificationResponse(
                    verdict = response.verdict,
                    confidence = response.confidence,
                    threatName = response.threatName,
                    threatFamily = response.threatFamily,
                    avDetections = response.avDetections,
                    totalAvScanned = response.totalAvScanned,
                    communityReports = response.communityReports,
                    virustotalLink = response.virustotalLink,
                    evidence = response.evidence ?: emptyList(),
                    recommendation = response.recommendation ?: "WARN_USER"
                )
            } catch (e: HttpException) {
                val code = e.code()
                if (code in 400..499 && code != 429) {
                    return CloudVerificationResponse(
                        verdict = "UNKNOWN",
                        confidence = 0f,
                        threatName = null,
                        threatFamily = null,
                        avDetections = null,
                        totalAvScanned = null,
                        communityReports = null,
                        virustotalLink = null,
                        evidence = listOf("Cloud API client error: HTTP $code (not retried)"),
                        recommendation = "WARN_USER"
                    )
                }
                // Treat 429 as retryable (rate-limited). Prefer Retry-After to avoid slow backoff.
                val retryAfterSec = e.response()?.headers()?.get("Retry-After")?.toLongOrNull()
                val failures = consecutiveFailures.incrementAndGet()
                if (failures >= CIRCUIT_FAIL_THRESHOLD) {
                    circuitOpenUntil.set(System.currentTimeMillis() + CIRCUIT_OPEN_MS)
                }
                if (attempt < MAX_ATTEMPTS - 1) {
                    val retryDelayMs = if (code == 429 && retryAfterSec != null) {
                        (retryAfterSec * 1000L).coerceAtLeast(0L)
                    } else {
                        delayMs
                    }
                    delay(retryDelayMs)
                }
                delayMs *= 2
            } catch (e: Exception) {
                val failures = consecutiveFailures.incrementAndGet()
                if (failures >= CIRCUIT_FAIL_THRESHOLD) {
                    circuitOpenUntil.set(System.currentTimeMillis() + CIRCUIT_OPEN_MS)
                }
                if (attempt < MAX_ATTEMPTS - 1) delay(delayMs)
                delayMs *= 2
            }
        }
        return CloudVerificationResponse(
            verdict = "UNKNOWN",
            confidence = 0f,
            threatName = null,
            threatFamily = null,
            avDetections = null,
            totalAvScanned = null,
            communityReports = null,
            virustotalLink = null,
            evidence = listOf("Cloud verification unavailable after $MAX_ATTEMPTS attempts"),
            recommendation = "WARN_USER"
        )
    }
}
