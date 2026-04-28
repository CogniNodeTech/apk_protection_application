package com.safeguard.core.orchestration

import com.safeguard.core.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForensicReasoningEngine @Inject constructor() {

    /**
     * Synthesizes a human-readable forensic reason and categorizes the malware.
     */
    fun analyze(result: ScanResult): ForensicAnalysis {
        val evidence = result.layerResults
        val layerVerdicts = evidence.associate { it.layerId to it.verdict }
        val layerDetails = evidence.associate { it.layerId to it.evidence }

        val technicalReasoning = mutableListOf<String>()
        var category = MalwareCategory.CLEAN
        var confidenceBonus = 0f

        // 1. Signature & Source Correlation
        val isRepackaged = layerVerdicts[4] == Verdict.MALICIOUS // Signature Layer
        val isUnknownSource = result.installerSource == "Unknown" || result.installerSource == "Browser"

        if (isRepackaged) {
            technicalReasoning.add("App signature mismatch detected: The original developer certificate has been tampered with or replaced.")
            category = MalwareCategory.TROJAN_SMS // Default to Trojan if repackaged
        }

        // 2. Permission vs AI Intent Correlation
        val hasSmsPerms = layerDetails[3]?.any { it.contains("SMS", ignoreCase = true) } == true
        val hasNetworkPerms = layerDetails[3]?.any { it.contains("INTERNET", ignoreCase = true) } == true
        val aiSuspicious = layerVerdicts[5] == Verdict.MALICIOUS || layerVerdicts[5] == Verdict.SUSPICIOUS

        if (hasSmsPerms && aiSuspicious) {
            technicalReasoning.add("Critical Flow Detected: App requests sensitive SMS access while bytecode patterns indicate background interceptor behavior.")
            category = MalwareCategory.TROJAN_SMS
        }

        if (hasNetworkPerms && layerDetails[3]?.any { it.contains("Overlay", ignoreCase = true) } == true) {
            technicalReasoning.add("Phishing Risk: System overlay permissions combined with network access indicate a high probability of a Banking 'Overlay' attack.")
            category = MalwareCategory.BANKER
        }

        // 3. Ransomware Detection (Heuristic)
        val hasStoragePerms = layerDetails[3]?.any { it.contains("STORAGE", ignoreCase = true) } == true
        if (hasStoragePerms && aiSuspicious && layerDetails[5]?.any { it.contains("Encryption", ignoreCase = true) } == true) {
            technicalReasoning.add("Ransomware Pattern: App attempts to iterate over storage directories using cryptographic libraries without a clear functional reason.")
            category = MalwareCategory.RANSOMWARE
        }

        // 4. Source Reputation Logic
        if (isUnknownSource && (aiSuspicious || isRepackaged)) {
            technicalReasoning.add("Source Integrity Failure: The app was downloaded from an unverified web/P2P source, significantly increasing the risk of malicious payload injection.")
            confidenceBonus += 0.1f
        }

        // 5. Advanced Flow Correlation: SMS Interceptor/Stealer
        val hasInterceptorIntents = layerDetails[5]?.any { it.contains("SMS", ignoreCase = true) || it.contains("NEW_OUTGOING_CALL", ignoreCase = true) } == true
        if (hasSmsPerms && hasInterceptorIntents && aiSuspicious) {
            technicalReasoning.add("Malicious Flow: sensitive SMS permissions combined with background intent filters (BOOT/SMS_RECEIVED) indicate an active SMS interception Trojan.")
            category = MalwareCategory.TROJAN_SMS
            confidenceBonus += 0.15f
        }

        // 6. Advanced Flow Correlation: Banking Overlay + Accessibility
        val hasAccessibility = layerDetails[3]?.any { it.contains("ACCESSIBILITY", ignoreCase = true) } == true
        val hasOverlay = layerDetails[3]?.any { it.contains("SYSTEM_ALERT_WINDOW", ignoreCase = true) || it.contains("Overlay", ignoreCase = true) } == true
        if (hasOverlay && hasAccessibility && hasNetworkPerms) {
            technicalReasoning.add("Critical Threat Pattern: Combination of Screen Overlay, Accessibility, and Network access is characteristic of modern Banking Trojans (Credential Harvesters).")
            category = MalwareCategory.BANKER
            confidenceBonus += 0.2f
        }

        // 7. Obfuscation & Dropper Logic
        val isHighlyObfuscated = layerDetails[5]?.any { it.contains("Obfuscation", ignoreCase = true) } == true
        val canInstall = layerDetails[3]?.any { it.contains("INSTALL_PACKAGES", ignoreCase = true) || it.contains("REQUEST_INSTALL_PACKAGES", ignoreCase = true) } == true
        if (isHighlyObfuscated && canInstall && isUnknownSource) {
            technicalReasoning.add("Dropper Behavior: High obfuscation entropy combined with silent package installation permissions suggests a stage-1 dropper payload.")
            category = MalwareCategory.DROPPER
            confidenceBonus += 0.1f
        }

        // 8. Spyware/Stalkerware Synthesis
        if (category == MalwareCategory.CLEAN && hasNetworkPerms && (layerDetails[3]?.any { it.contains("LOCATION", ignoreCase = true) } == true || layerDetails[3]?.any { it.contains("CONTACTS", ignoreCase = true) } == true)) {
           if (aiSuspicious || isUnknownSource) {
               technicalReasoning.add("Spyware Indicators: Stealthy collection of PII (Location/Contacts) synced to remote servers without clear user-facing utility.")
               category = MalwareCategory.SPYWARE
           }
        }

        // Final Verdict Logic — never show CLEAN when the pipeline verdict is malicious/suspicious
        val finalCategory = when (result.finalVerdict) {
            Verdict.SAFE -> MalwareCategory.CLEAN
            Verdict.MALICIOUS -> when {
                category != MalwareCategory.CLEAN -> category
                else -> MalwareCategory.UNSPECIFIED
            }
            Verdict.SUSPICIOUS -> when {
                category != MalwareCategory.CLEAN -> category
                else -> MalwareCategory.RISKWARE
            }
            else -> when {
                category != MalwareCategory.CLEAN -> category
                else -> MalwareCategory.UNSPECIFIED
            }
        }

        return ForensicAnalysis(
            category = finalCategory,
            reasoning = if (technicalReasoning.isEmpty() && result.finalVerdict != Verdict.SAFE)
                listOf("Unknown malicious behavior detected in app logic.")
            else technicalReasoning,
            confidenceAdjustment = confidenceBonus
        )
    }

    data class ForensicAnalysis(
        val category: MalwareCategory,
        val reasoning: List<String>,
        val confidenceAdjustment: Float
    )
}
