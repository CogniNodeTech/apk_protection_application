package com.safeguard.core.domain.security

interface DeviceIntegrityProvider {
    enum class ThreatLevel {
        NONE, LOW, MEDIUM, HIGH, CRITICAL
    }

    data class SecurityStatus(
        val threatLevel: ThreatLevel,
        val detectedThreats: List<String>,
        val isRooted: Boolean,
        val isHooked: Boolean,
        val isDebuggerAttached: Boolean,
        val isTampered: Boolean
    )

    fun checkSecurityStatus(): SecurityStatus
}
