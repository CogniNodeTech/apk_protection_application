package com.safeguard.security.integrity

import android.os.Build
import java.io.File

/**
 * Lightweight checks for rooted or emulator environment.
 * Used to warn the user; does not block functionality.
 * Detection is best-effort and can be bypassed by determined attackers.
 */
object RuntimeEnvironmentChecker {

    fun isProbablyEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic", ignoreCase = true) ||
            Build.FINGERPRINT.contains("vbox", ignoreCase = true) ||
            Build.MODEL.contains("sdk_gphone", ignoreCase = true) ||
            Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
            (Build.BRAND.startsWith("generic", ignoreCase = true) && Build.DEVICE.startsWith("generic", ignoreCase = true)) ||
            "google_sdk" == Build.PRODUCT ||
            Build.PRODUCT.contains("sdk", ignoreCase = true) ||
            Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
            Build.HARDWARE.contains("ranchu", ignoreCase = true) ||
            Build.HARDWARE.contains("vbox", ignoreCase = true) ||
            Build.BOARD.contains("unknown", ignoreCase = true)
    }

    private val rootPaths = listOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/system/app/SuperSU",
        "/system/app/SuperSU.apk",
        "/system/xbin/daemonsu",
        "/system/etc/init.d/99SuperSUDaemon",
        "/dev/com.koushikdutta.superuser.daemon/"
    )

    fun isProbablyRooted(): Boolean {
        if (rootPaths.any { File(it).exists() }) return true
        return try {
            Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su")).waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    fun isRiskyEnvironment(): Boolean = isProbablyEmulator() || isProbablyRooted()
}
