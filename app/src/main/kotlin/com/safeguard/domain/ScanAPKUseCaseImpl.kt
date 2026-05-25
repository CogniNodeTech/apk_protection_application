package com.safeguard.domain

import com.safeguard.core.domain.model.Action
import com.safeguard.core.domain.model.MalwareCategory
import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.model.ThreatInfo
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.repository.ScanRepository
import com.safeguard.core.domain.telemetry.ScanTelemetry
import com.safeguard.core.domain.usecase.ScanAPKUseCase
import com.safeguard.core.orchestration.ScanOrchestrator
import java.io.File
import java.util.UUID
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context

class ScanAPKUseCaseImpl @javax.inject.Inject constructor(
    @ApplicationContext private val context: Context,
    private val orchestrator: ScanOrchestrator,
    private val scanRepository: ScanRepository,
    private val telemetry: ScanTelemetry
) : ScanAPKUseCase {

    override suspend fun execute(apkFile: File, displayName: String?): ScanResult = withContext(Dispatchers.IO) {
        val startedAt = System.currentTimeMillis()
        val pm = context.packageManager
        val pkgInfoEarly = try {
            pm.getPackageArchiveInfo(apkFile.absolutePath, 0)
        } catch (_: Exception) {
            null
        }
        if (pkgInfoEarly?.packageName == context.packageName && pkgInfoEarly != null) {
            val trusted = trustedHostAppScanResult(apkFile, displayName, pkgInfoEarly, startedAt)
            scanRepository.saveScanResult(trusted)
            telemetry.onScanComplete(trusted.finalVerdict, trusted.overallRiskScore)
            return@withContext trusted
        }
        var installerSource: String? = null
        try {
            val pkgInfo = pkgInfoEarly ?: pm.getPackageArchiveInfo(apkFile.absolutePath, 0)
            if (pkgInfo != null) {
                val installerName = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    pm.getInstallSourceInfo(pkgInfo.packageName).installingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstallerPackageName(pkgInfo.packageName)
                }
                installerSource = when (installerName) {
                    "com.android.vending" -> "Google Play Store"
                    "com.whatsapp", "com.whatsapp.w4b" -> "WhatsApp"
                    "org.telegram.messenger" -> "Telegram"
                    "com.android.chrome" -> "Chrome"
                    "com.sec.android.app.sbrowser" -> "Samsung Internet"
                    "com.brave.browser" -> "Brave Browser"
                    "com.microsoft.emmx" -> "Microsoft Edge"
                    "org.mozilla.firefox" -> "Firefox"
                    null -> "Unknown Source"
                    else -> installerName
                }
            }
        } catch (e: Exception) {
            // Ignored, might not be a valid app or not installed
        }

        val baseResult = orchestrator.scan(apkFile, displayName)
        val duration = System.currentTimeMillis() - startedAt
        val result = baseResult.copy(
            installerSource = installerSource,
            aggregatedEvidence = baseResult.aggregatedEvidence + "pipeline_scan_time_ms=$duration"
        )
        
        scanRepository.saveScanResult(result)
        telemetry.onScanComplete(result.finalVerdict, result.overallRiskScore)
        result
    }

    /**
     * Never score the host app as malware (device scans include our own base.apk).
     */
    private fun trustedHostAppScanResult(
        apkFile: File,
        displayName: String?,
        pkgInfo: android.content.pm.PackageInfo,
        startedAt: Long
    ): ScanResult {
        val pm = context.packageManager
        val installerSource = try {
            val installerName = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(pkgInfo.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(pkgInfo.packageName)
            }
            when (installerName) {
                "com.android.vending" -> "Google Play Store"
                null -> "Unknown Source"
                else -> installerName
            }
        } catch (_: Exception) {
            "Unknown Source"
        }
        val duration = System.currentTimeMillis() - startedAt
        return ScanResult(
            id = UUID.randomUUID().toString(),
            apkPath = apkFile.absolutePath,
            apkName = displayName?.takeIf { it.isNotBlank() } ?: apkFile.name,
            apkSizeBytes = apkFile.length(),
            scanTimestamp = System.currentTimeMillis(),
            finalVerdict = Verdict.SAFE,
            overallConfidence = 1f,
            overallRiskScore = 0,
            layerResults = emptyList(),
            aggregatedEvidence = listOf("trusted_host_app=1", "pipeline_scan_time_ms=$duration"),
            recommendedAction = Action.ALLOW,
            threatInfo = ThreatInfo(
                threatName = null,
                threatFamily = null,
                severity = 0,
                avDetections = null,
                totalAvScanned = null,
                communityReports = null,
                category = MalwareCategory.CLEAN,
                technicalReasoning = listOf("This is the SafeGuard app package; it is trusted and excluded from threat scoring.")
            ),
            installerSource = installerSource
        )
    }
}
