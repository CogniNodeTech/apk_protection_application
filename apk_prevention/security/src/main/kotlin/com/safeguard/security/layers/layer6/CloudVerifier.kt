package com.safeguard.security.layers.layer6

import com.safeguard.core.domain.integrity.NoOpPlayIntegrityChecker
import com.safeguard.core.domain.integrity.PlayIntegrityChecker
import com.safeguard.core.domain.integrity.PlayIntegrityVerdict
import com.safeguard.core.domain.integrity.ScanIntegrityContext
import com.safeguard.core.domain.model.ThreatInfo
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.layer.ProtectionLayer
import com.safeguard.core.domain.repository.CloudVerificationRepository
import com.safeguard.core.domain.repository.DeviceCloudMetadata
import com.safeguard.core.domain.repository.LocalLayerScores
import com.safeguard.core.domain.model.LayerResult
import com.safeguard.data.local.preferences.SecurePreferencesManager
import com.safeguard.security.layers.layer5.Layer5Result
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.withTimeout

class CloudVerifier(
    private val cloudRepo: CloudVerificationRepository,
    private val androidVersion: Int,
    private val deviceLocale: String,
    private val preferences: SecurePreferencesManager,
    /**
     * Phase 3.4: Play Integrity cross-check. Defaults to [NoOpPlayIntegrityChecker] so
     * existing call sites and tests don't have to thread a checker through; production
     * Hilt swaps in the real Google-backed impl when a cloud project number is
     * configured. Today the verdict is added to evidence only — it doesn't currently
     * gate the cloud verdict, because we want the plumbing landed and observable
     * before a future change starts trusting the integrity verdict for routing
     * decisions.
     */
    private val playIntegrityChecker: PlayIntegrityChecker = NoOpPlayIntegrityChecker()
) : ProtectionLayer {

    override suspend fun verify(context: APKContext, previousLayerResults: List<LayerResult>): Layer6Result {
        val start = System.currentTimeMillis()
        if (!preferences.privacyOnboardingAcknowledged || !preferences.cloudVerificationEnabled) {
            val reason = when {
                !preferences.privacyOnboardingAcknowledged ->
                    "Cloud verification is paused until you review data practices in the welcome screen."
                else -> "Cloud verification is turned off in Settings."
            }
            return Layer6Result(
                verdict = Verdict.UNKNOWN,
                confidence = 0f,
                riskScore = 0,
                avDetections = null,
                totalAvScanned = null,
                communityReports = null,
                threatName = null,
                evidence = listOf(reason),
                executionTimeMs = System.currentTimeMillis() - start,
                threatInfo = null
            )
        }

        // Avoid heavy CPU/IO work on very large APKs for cloud verification.
        val maxCloudApkBytes = 50L * 1024L * 1024L // 50 MiB
        val apkFile = context.apkFile
        if (apkFile.length() > maxCloudApkBytes) {
            return Layer6Result(
                verdict = Verdict.UNKNOWN,
                confidence = 0f,
                riskScore = 0,
                avDetections = null,
                totalAvScanned = null,
                communityReports = null,
                threatName = null,
                evidence = listOf("Cloud verification skipped: APK too large"),
                executionTimeMs = System.currentTimeMillis() - start,
                threatInfo = null
            )
        }
        val sha256 = context.getCached<String>(APKContext.KEY_SHA256) ?: calculateSHA256(apkFile).also {
            context.putCached(APKContext.KEY_SHA256, it)
        }

        // Phase 3.4: Play Integrity cross-check. We bind the request hash to the APK's
        // SHA-256 so the server (when a real impl ships) can reject replayed attestation
        // tokens that were minted for some *other* scan. The check has its own timeout
        // inside the impl; we don't want it to add to the cloud-verify budget either way.
        val integrityVerdict: PlayIntegrityVerdict = try {
            playIntegrityChecker.check(ScanIntegrityContext(requestHash = sha256))
        } catch (e: Exception) {
            // Defensive: spec says implementations must not throw, but if a third-party
            // checker plugs in and breaks the contract we degrade rather than crash.
            PlayIntegrityVerdict(
                device = PlayIntegrityVerdict.DeviceIntegrityLevel.UNAVAILABLE,
                app = PlayIntegrityVerdict.AppIntegrityLevel.UNAVAILABLE,
                account = PlayIntegrityVerdict.AccountIntegrityLevel.UNAVAILABLE,
                source = PlayIntegrityVerdict.Source.PLAY_INTEGRITY_API_ERROR,
                note = "checker threw ${e.javaClass.simpleName}"
            )
        }

        // Bound cloud-verification work to prevent crafted APKs from stalling the pipeline.
        val response = try {
            withTimeout(15_000L) {
                val sha512 = calculateSHA512(apkFile)
                val meta = extractMeta(apkFile)
                val packageName = meta.packageName
                val versionCode = meta.versionCode
                val permissions = meta.permissions
                val targetSdk = meta.targetSdk
                val fileSize = apkFile.length()
                val signatureFingerprint = extractSignatureFingerprint(apkFile)
                val localLayerScores = buildLocalLayerScores(previousLayerResults)
                val deviceMetadata = DeviceCloudMetadata(
                    androidVersion = androidVersion,
                    deviceLocale = deviceLocale
                )
                cloudRepo.verify(
                    sha256 = sha256,
                    sha512 = sha512,
                    packageName = packageName,
                    versionCode = versionCode,
                    permissions = permissions,
                    fileSize = fileSize,
                    targetSdk = targetSdk,
                    signatureFingerprint = signatureFingerprint,
                    localLayerScores = localLayerScores,
                    deviceMetadata = deviceMetadata
                )
            }
        } catch (_: Exception) {
            return Layer6Result(
                verdict = Verdict.UNKNOWN,
                confidence = 0f,
                riskScore = 0,
                avDetections = null,
                totalAvScanned = null,
                communityReports = null,
                threatName = null,
                evidence = listOf(
                    "Cloud verification failed (timeout/error); falling back to on-device layers",
                    integrityVerdict.toEvidenceLine()
                ),
                executionTimeMs = System.currentTimeMillis() - start,
                threatInfo = null
            )
        }

        val verdict = when (response.verdict.uppercase()) {
            "MALICIOUS" -> Verdict.MALICIOUS
            "SUSPICIOUS" -> Verdict.SUSPICIOUS
            "SAFE" -> Verdict.SAFE
            else -> Verdict.UNKNOWN
        }
        val riskScore = (response.confidence * 100).toInt().coerceIn(0, 100)
        val threatInfo = if (response.threatName != null)
            ThreatInfo(response.threatName, response.threatFamily, riskScore, response.avDetections, response.totalAvScanned, response.communityReports)
        else null
        val time = System.currentTimeMillis() - start
        // Append the Play Integrity verdict line to whatever the cloud returned. We
        // never replace the cloud's evidence — the integrity verdict is a *secondary*
        // signal and Layer 6's primary contract is still "report what cloud said".
        val baseEvidence = response.evidence.ifEmpty { listOf("Cloud: ${response.verdict}") }
        val combinedEvidence = baseEvidence + integrityVerdict.toEvidenceLine()
        return Layer6Result(
            verdict = verdict,
            confidence = response.confidence,
            riskScore = riskScore,
            avDetections = response.avDetections,
            totalAvScanned = response.totalAvScanned,
            communityReports = response.communityReports,
            threatName = response.threatName,
            evidence = combinedEvidence,
            executionTimeMs = time,
            threatInfo = threatInfo
        )
    }

    private fun buildLocalLayerScores(previousLayerResults: List<LayerResult>): LocalLayerScores {
        val layer2 = previousLayerResults.find { it.layerId == 2 }
        val layer3 = previousLayerResults.find { it.layerId == 3 }
        val layer4 = previousLayerResults.find { it.layerId == 4 }
        val layer5Prob = previousLayerResults
            .filterIsInstance<Layer5Result>()
            .firstOrNull()
            ?.malwareProbability
            ?.toDouble()
            ?: (previousLayerResults.find { it.layerId == 5 }?.confidence?.toDouble() ?: 0.5)
        return LocalLayerScores(
            layer2HashResult = layer2?.verdict?.name ?: "UNKNOWN",
            layer3PermissionScore = layer3?.riskScore ?: 0,
            layer4SignatureScore = layer4?.riskScore ?: 0,
            layer5MlProbability = layer5Prob
        )
    }

    private data class ApkMetaExtract(val packageName: String, val versionCode: Int, val permissions: List<String>, val targetSdk: Int)

    private fun extractMeta(apkFile: File): ApkMetaExtract {
        return try {
            ApkFile(apkFile).use { apk ->
                val meta = apk.apkMeta
                val pkg = meta?.packageName ?: ""
                val version = (meta?.versionCode as? Number)?.toInt() ?: 0
                val perms = meta?.usesPermissions?.map { it.toString() } ?: emptyList()
                val sdk = meta?.targetSdkVersion?.toString()?.toIntOrNull() ?: 0
                ApkMetaExtract(pkg, version, perms, sdk)
            }
        } catch (e: Exception) {
            ApkMetaExtract("", 0, emptyList(), 0)
        }
    }

    private fun calculateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } > 0) digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun calculateSHA512(file: File): String {
        val digest = MessageDigest.getInstance("SHA-512")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } > 0) digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun extractSignatureFingerprint(apkFile: File): String? {
        return try {
            java.util.zip.ZipFile(apkFile).use { zip ->
                val entries = zip.entries()
                var fingerprint: String? = null
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.startsWith("META-INF/") && entry.name.endsWith(".RSA")) {
                        zip.getInputStream(entry)?.use { input ->
                            val cert = java.security.cert.CertificateFactory.getInstance("X.509")
                                .generateCertificates(input).firstOrNull()
                            fingerprint = cert?.let {
                                MessageDigest.getInstance("SHA-256")
                                    .digest(it.encoded)
                                    .joinToString("") { b -> "%02x".format(b) }
                            }
                        }
                        if (fingerprint != null) break
                    }
                }
                fingerprint
            }
        } catch (e: Exception) {
            null
        }
    }

}
