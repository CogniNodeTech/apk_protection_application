package com.safeguard.security.integrity

import android.os.Debug

/**
 * Detects debugger attachment. Use to warn user or degrade experience in production.
 * Does not kill the process by default to avoid breaking legitimate development.
 */
object AntiDebugChecker {

    /**
     * Returns true if a debugger is currently attached (e.g. Android Studio, adb).
     */
    fun isDebuggerAttached(): Boolean = Debug.isDebuggerConnected()

}
