package com.safeguard.security.layers.layer3

import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.layer.ProtectionLayer
import com.safeguard.core.domain.model.LayerResult
import net.dongliu.apk.parser.ApkFile
import java.io.File

private val PERMISSION_RISKS = mapOf(
    "android.permission.SEND_SMS" to 70,
    "android.permission.READ_SMS" to 60,
    "android.permission.CALL_PHONE" to 50,
    "android.permission.CAMERA" to 30,
    "android.permission.RECORD_AUDIO" to 40,
    "android.permission.READ_CONTACTS" to 45,
    "android.permission.WRITE_CONTACTS" to 50,
    "android.permission.ACCESS_FINE_LOCATION" to 45,
    "android.permission.ACCESS_COARSE_LOCATION" to 40,
    "android.permission.INTERNET" to 10,
    "android.permission.INSTALL_PACKAGES" to 90,
    "android.permission.REQUEST_INSTALL_PACKAGES" to 85,
    "android.permission.RECEIVE_SMS" to 55,
    "android.permission.READ_CALL_LOG" to 60,
    "android.permission.WRITE_CALL_LOG" to 60,
    "android.permission.READ_PHONE_STATE" to 40,
    "android.permission.BIND_ACCESSIBILITY_SERVICE" to 95,
    "android.permission.SYSTEM_ALERT_WINDOW" to 80,
    "android.permission.PACKAGE_USAGE_STATS" to 75,
    "android.permission.WRITE_SETTINGS" to 70,
    "android.permission.READ_EXTERNAL_STORAGE" to 25,
    "android.permission.WRITE_EXTERNAL_STORAGE" to 30,
    "android.permission.QUERY_ALL_PACKAGES" to 50,
    "android.permission.GET_TASKS" to 40,
    "android.permission.REORDER_TASKS" to 40,
    "android.permission.KILL_BACKGROUND_PROCESSES" to 50,
    "android.permission.READ_LOGS" to 60,
    "android.permission.SET_DEBUG_APP" to 80,
    "android.permission.CHANGE_NETWORK_STATE" to 30,
    "android.permission.CHANGE_WIFI_STATE" to 30,
    "android.permission.BLUETOOTH_ADMIN" to 30,
    "android.permission.DISABLE_KEYGUARD" to 70,
    "android.permission.EXPAND_STATUS_BAR" to 30,
    "android.permission.GET_PACKAGE_SIZE" to 10,
    "android.permission.ACCESS_NETWORK_STATE" to 10,
    "android.permission.ACCESS_WIFI_STATE" to 10,
    "android.permission.WAKE_LOCK" to 15,
    "android.permission.VIBRATE" to 5,
    "android.permission.REQUEST_DELETE_PACKAGES" to 80,
    "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" to 60,
    "android.permission.USE_FULL_SCREEN_INTENT" to 70,
    "android.permission.BROADCAST_PACKAGE_REMOVED" to 90,
    "android.permission.BROADCAST_SMS" to 95,
    "android.permission.USE_SIP" to 40,
    "android.permission.PROCESS_OUTGOING_CALLS" to 85,
    "android.permission.ADD_VOICEMAIL" to 40,
    "android.permission.READ_SYNC_SETTINGS" to 20,
    "android.permission.WRITE_SYNC_SETTINGS" to 20,
    "android.permission.AUTHENTICATE_ACCOUNTS" to 70,
    "android.permission.MANAGE_ACCOUNTS" to 80,
    "android.permission.USE_CREDENTIALS" to 80,
    "android.permission.READ_PROFILE" to 30,
    "android.permission.WRITE_PROFILE" to 30,
    "android.permission.READ_SOCIAL_STREAM" to 40,
    "android.permission.WRITE_SOCIAL_STREAM" to 40
)

private data class Combo(val permissions: Set<String>, val threatType: String, val riskScore: Int)

private val DANGEROUS_COMBOS = listOf(
    Combo(setOf("android.permission.SEND_SMS", "android.permission.READ_SMS", "android.permission.INTERNET"), "SMS Fraud", 90),
    Combo(setOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO", "android.permission.INTERNET"), "Surveillance", 85),
    Combo(setOf("android.permission.REQUEST_INSTALL_PACKAGES", "android.permission.INTERNET"), "Dropper", 95),
    Combo(setOf("android.permission.ACCESS_FINE_LOCATION", "android.permission.INTERNET", "android.permission.READ_CONTACTS"), "Stalkerware", 75),
    Combo(setOf("android.permission.BIND_ACCESSIBILITY_SERVICE", "android.permission.SYSTEM_ALERT_WINDOW", "android.permission.INTERNET"), "Banking Trojan (Overlay)", 98),
    Combo(setOf("android.permission.READ_SMS", "android.permission.RECEIVE_SMS", "android.permission.INTERNET", "android.permission.QUERY_ALL_PACKAGES"), "Spyware (SMS Stealer)", 95),
    Combo(setOf("android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.INTERNET", "android.permission.RECEIVE_BOOT_COMPLETED"), "Possible Ransomware", 80),
    Combo(setOf("android.permission.MANAGE_ACCOUNTS", "android.permission.USE_CREDENTIALS", "android.permission.INTERNET"), "Account Stealer", 90)
)

/**
 * Layer 3: Permission Analyzer - risk from requested permissions and combinations.
 */
class PermissionAnalyzer : ProtectionLayer {

    override suspend fun verify(context: APKContext, previousLayerResults: List<LayerResult>): Layer3Result {
        val start = System.currentTimeMillis()
        val apkFile = context.apkFile
        val permissions = try {
            ApkFile(apkFile).use { apk ->
                apk.apkMeta?.usesPermissions?.map { it.toString() }?.distinct() ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
        var totalRisk = 0
        val evidence = mutableListOf<String>()
        permissions.forEach { perm ->
            val risk = PERMISSION_RISKS[perm] ?: 5
            totalRisk += risk
            if (risk > 50) evidence.add("High-risk: $perm (score: $risk)")
        }
        val combosFound = mutableListOf<String>()
        DANGEROUS_COMBOS.forEach { combo ->
            val permSet = permissions.toSet()
            if (combo.permissions.all { p: String -> permSet.any { perm -> perm == p } }) {
                totalRisk += combo.riskScore
                combosFound.add(combo.threatType)
                evidence.add("⚠️ ${combo.threatType} pattern detected")
            }
        }
        if (permissions.size > 30) {
            totalRisk += 25
            evidence.add("Excessive permissions: ${permissions.size}")
        }
        val finalRisk = totalRisk.coerceIn(0, 100)
        val verdict = when {
            finalRisk >= 70 -> Verdict.MALICIOUS
            finalRisk >= 40 -> Verdict.SUSPICIOUS
            else -> Verdict.SAFE
        }
        val confidence = (1 - finalRisk / 100f * 0.5f).coerceIn(0.5f, 1f)
        val time = System.currentTimeMillis() - start
        return Layer3Result(
            verdict = verdict,
            confidence = confidence,
            riskScore = finalRisk,
            permissionCount = permissions.size,
            dangerousCombinations = combosFound,
            evidence = evidence.ifEmpty { listOf("No high-risk permission patterns") },
            executionTimeMs = time
        )
    }
}
