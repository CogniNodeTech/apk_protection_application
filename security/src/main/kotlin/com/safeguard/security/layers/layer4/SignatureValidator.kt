package com.safeguard.security.layers.layer4

import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.layer.ProtectionLayer
import com.android.apksig.ApkVerifier
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.security.MessageDigest
import java.security.cert.CertificateExpiredException
import java.security.cert.X509Certificate
import java.util.zip.ZipFile

class SignatureValidator(
    private val blacklistedSigners: Set<String>
) : ProtectionLayer {

    override suspend fun verify(context: APKContext, previousLayerResults: List<LayerResult>): Layer4Result {
        val start = System.currentTimeMillis()
        val apkFile = context.apkFile
        val certBytes = try {
            ZipFile(apkFile).use { zip ->
                zip.entries().toList().firstOrNull { it.name.startsWith("META-INF/") && (it.name.endsWith(".RSA") || it.name.endsWith(".DSA")) }
                    ?.let { zip.getInputStream(it)?.readBytes() }
            }
        } catch (e: Exception) {
            null
        }
        if (certBytes == null || certBytes.isEmpty()) {
            val time = System.currentTimeMillis() - start
            return Layer4Result(
                layerId = 4,
                layerName = "Signature Validator",
                verdict = Verdict.MALICIOUS,
                confidence = 1.0f,
                riskScore = 100,
                certificateInfo = null,
                evidence = listOf("Unsigned APK - CRITICAL"),
                executionTimeMs = time
            )
        }
        var riskScore = 0
        val evidence = mutableListOf<String>()
        var certInfo: CertificateInfo? = null
        try {
            val apkSigResult = ApkVerifier.Builder(apkFile).build().verify()
            if (!apkSigResult.isVerified) {
                val t = System.currentTimeMillis() - start
                return Layer4Result(
                    verdict = Verdict.MALICIOUS,
                    confidence = 1.0f,
                    riskScore = 100,
                    certificateInfo = null,
                    evidence = listOf("APK signature verification failed (v1/v2/v3/v4)"),
                    executionTimeMs = t
                )
            }
            if (!apkSigResult.isVerifiedUsingV2Scheme && !apkSigResult.isVerifiedUsingV3Scheme) {
                riskScore += 20
                evidence.add("Legacy-only signing without v2/v3 scheme")
            }
            val cert = java.security.cert.CertificateFactory.getInstance("X.509")
                .generateCertificates(certBytes.inputStream()).firstOrNull() as? X509Certificate ?: run {
                val t = System.currentTimeMillis() - start
                return Layer4Result(
                    verdict = Verdict.SUSPICIOUS,
                    confidence = 0.5f,
                    riskScore = 60,
                    certificateInfo = null,
                    evidence = listOf("Invalid certificate"),
                    executionTimeMs = t
                )
            }
            try {
                cert.checkValidity()
            } catch (e: CertificateExpiredException) {
                riskScore += 60
                evidence.add("Certificate expired: ${cert.notAfter}")
            }
            val alg = cert.sigAlgName ?: ""
            if (alg.contains("MD5", ignoreCase = true) || alg.contains("SHA1", ignoreCase = true)) {
                riskScore += 40
                evidence.add("Weak algorithm: $alg")
            }
            val fingerprint = MessageDigest.getInstance("SHA-256").digest(cert.encoded).joinToString("") { "%02x".format(it) }
            if (fingerprint in blacklistedSigners) {
                val t = System.currentTimeMillis() - start
                return Layer4Result(
                    verdict = Verdict.MALICIOUS,
                    confidence = 1.0f,
                    riskScore = 100,
                    certificateInfo = null,
                    evidence = listOf("KNOWN MALICIOUS SIGNER"),
                    executionTimeMs = t
                )
            }
            val issuer = cert.issuerX500Principal?.name ?: ""
            val subject = cert.subjectX500Principal?.name ?: ""
            val selfSigned = issuer == subject
            if (selfSigned) {
                riskScore += 30
                evidence.add("Self-signed certificate")
            }
            if (subject.contains("Android Debug", ignoreCase = true) || subject.contains("Unknown", ignoreCase = true)) {
                riskScore += 25
                evidence.add("Suspicious subject: $subject")
            }
            if (subject.contains("OU=", ignoreCase = false) && (subject.contains("OU=Android", ignoreCase = true) || subject.contains("OU=Mobile", ignoreCase = true))) {
                riskScore += 15
                evidence.add("Genetic OU pattern often used in malware: $subject")
            }
            val validityDays = (cert.notAfter.time - cert.notBefore.time) / (1000 * 60 * 60 * 24)
            if (validityDays < 365) {
                riskScore += 20
                evidence.add("Short-lived certificate ($validityDays days) - suspicious for production apps")
            }
            certInfo = CertificateInfo(issuer, subject, cert.notAfter?.time ?: 0L, alg, selfSigned)
        } catch (e: Exception) {
            evidence.add("Certificate parse error: ${e.message}")
            riskScore += 50
        }
        val finalRisk = riskScore.coerceIn(0, 100)
        val verdict = when {
            finalRisk >= 100 -> Verdict.MALICIOUS
            finalRisk >= 50 -> Verdict.SUSPICIOUS
            else -> Verdict.SAFE
        }
        val confidence = (1 - finalRisk / 100f * 0.5f).coerceIn(0.5f, 1f)
        val time = System.currentTimeMillis() - start
        return Layer4Result(
            layerId = 4,
            layerName = "Signature Validator",
            verdict = verdict,
            confidence = confidence,
            riskScore = finalRisk,
            certificateInfo = certInfo,
            evidence = evidence.ifEmpty { listOf("Certificate valid") },
            executionTimeMs = time
        )
    }
}
