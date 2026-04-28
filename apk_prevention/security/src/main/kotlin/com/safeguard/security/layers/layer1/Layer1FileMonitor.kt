package com.safeguard.security.layers.layer1

import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.layer.ProtectionLayer

/**
 * Layer 1: File System Monitor - evaluates APK based on source location and file metadata.
 */
class Layer1FileMonitor : ProtectionLayer {

    override suspend fun verify(context: APKContext, previousLayerResults: List<LayerResult>): Layer1Result {
        val start = System.currentTimeMillis()
        val path = context.apkPath
        val apkFile = context.apkFile
        val sourceLocation = when {
            path.contains("Download", ignoreCase = true) -> "Download folder"
            path.contains("WhatsApp", ignoreCase = true) -> "WhatsApp"
            path.contains("Telegram", ignoreCase = true) -> "Telegram"
            path.contains("Bluetooth", ignoreCase = true) -> "Bluetooth"
            else -> "Other location"
        }
        var fileNameRisk = 0
        val name = apkFile.name.lowercase()
        if (name.contains("update") || name.contains("patch") || name.contains("mod") || name.contains("crack"))
            fileNameRisk = 40
        var fileSizeRisk = 0
        val size = apkFile.length()
        if (size < 100_000) fileSizeRisk = 45
        else if (size > 200_000_000) fileSizeRisk = 25
        var locationRisk = 0
        if (sourceLocation == "WhatsApp" || sourceLocation == "Telegram") locationRisk = 30
        if (sourceLocation == "Bluetooth") locationRisk += 10
        val totalRisk = (fileNameRisk + fileSizeRisk + locationRisk).coerceIn(0, 100)
        val verdict = when {
            totalRisk >= 85 -> Verdict.MALICIOUS
            totalRisk >= 45 -> Verdict.SUSPICIOUS
            else -> Verdict.SAFE
        }
        val evidence = mutableListOf<String>()
        evidence.add("Source: $sourceLocation")
        if (fileNameRisk > 0) evidence.add("Filename pattern risk: $fileNameRisk")
        if (fileSizeRisk > 0) evidence.add("File size risk: $fileSizeRisk")
        val time = System.currentTimeMillis() - start
        val confidence = when (verdict) {
            Verdict.MALICIOUS -> (totalRisk / 100f).coerceIn(0.85f, 0.98f)
            Verdict.SUSPICIOUS -> (totalRisk / 100f).coerceIn(0.55f, 0.9f)
            else -> (1 - totalRisk / 100f).coerceIn(0.5f, 1f)
        }
        return Layer1Result(
            verdict = verdict,
            confidence = confidence,
            riskScore = totalRisk,
            sourceLocation = sourceLocation,
            fileNameRisk = fileNameRisk,
            fileSizeRisk = fileSizeRisk,
            evidence = evidence.ifEmpty { listOf("Standard location and file metadata") },
            executionTimeMs = time
        )
    }
}
