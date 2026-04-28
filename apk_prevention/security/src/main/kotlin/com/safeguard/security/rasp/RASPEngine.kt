package com.safeguard.security.rasp

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

import com.safeguard.core.domain.security.DeviceIntegrityProvider
import com.safeguard.core.domain.security.DeviceIntegrityProvider.*

@Singleton
class RASPEngine @Inject constructor(
    private val context: Context,
    /**
     * Expected SHA-256 fingerprint (hex pairs separated by `:` like `AA:BB:CC...`) for the app signing cert.
     *
     * If empty/blank, signature-mismatch is intentionally NOT enforced (dev builds / misconfigured releases).
     */
    private val expectedSignatureHash: String
) : DeviceIntegrityProvider {

    /** 
     * Normalized signature fingerprint for comparison.
     */
    private val expectedSignatureHashNormalized: String =
        expectedSignatureHash.trim().uppercase()

    override fun checkSecurityStatus(): SecurityStatus {
        val threats = mutableListOf<String>()
        val rooted = checkRoot(threats)
        val hooked = checkHooking(threats)
        val debugging = checkDebug(threats)
        val tampered = checkIntegrity(threats)

        val level = when {
            tampered || hooked -> ThreatLevel.CRITICAL
            debugging -> ThreatLevel.HIGH
            rooted -> ThreatLevel.MEDIUM
            else -> ThreatLevel.NONE
        }

        return SecurityStatus(level, threats, rooted, hooked, debugging, tampered)
    }

    private fun checkRoot(threats: MutableList<String>): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"
        )
        val binaryFound = paths.any { File(it).exists() } || canExecuteSu()
        val testKeys = Build.TAGS?.contains("test-keys") ?: false
        
        if (binaryFound) threats.add("Root binary detected")
        if (testKeys) threats.add("Custom ROM / Test-keys detected")
        
        return binaryFound || testKeys
    }

    private fun canExecuteSu(): Boolean {
        return try {
            Runtime.getRuntime().exec("su").destroy()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun checkHooking(threats: MutableList<String>): Boolean {
        // Check for Xposed/Frida classes in memory
        val hookClasses = arrayOf("de.robv.android.xposed.XposedBridge", "com.saurik.substrate.MS")
        val classesFound = hookClasses.any { 
            try { Class.forName(it); true } catch (_: Exception) { false }
        }
        
        // Check for suspicious libraries in maps
        val mapsFile = File("/proc/self/maps")
        var libraryFound = false
        if (mapsFile.exists()) {
            try {
                mapsFile.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        if (line.contains("frida-agent") || line.contains("xposed.aspectj")) {
                            libraryFound = true
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        if (classesFound) threats.add("Instrumentation framework detected (Xposed/Substrate)")
        if (libraryFound) threats.add("Hooking library detected (Frida)")
        
        return classesFound || libraryFound
    }

    private fun checkDebug(threats: MutableList<String>): Boolean {
        val connected = Debug.isDebuggerConnected()
        if (connected) threats.add("Debugger attached")
        return connected
    }

    private fun checkIntegrity(threats: MutableList<String>): Boolean {
        val signature = getAppSignatureHash()
        if (expectedSignatureHashNormalized.isBlank()) {
            // No trusted baseline configured -> skip signature mismatch checks.
            return false
        }

        if (signature == null) {
            threats.add("App signature could not be verified")
            return true
        }

        val mismatch = signature != expectedSignatureHashNormalized
        if (mismatch) threats.add("App signature mismatch (Potential Clone)")
        return mismatch
    }

    private fun getAppSignatureHash(): String? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            signatures?.firstOrNull()?.let { sig ->
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(sig.toByteArray())
                digest.joinToString(":") { "%02X".format(it) }
            }
        } catch (e: Exception) {
            null
        }
    }
}
