package com.safeguard.core.domain.integrity

/**
 * Build-time configuration the Hilt module uses to decide whether to wire up the real
 * [PlayIntegrityChecker] or fall back to [NoOpPlayIntegrityChecker].
 *
 * Sourced from `local.properties` ➜ BuildConfig in `:app`:
 *   - `safeguard.play.integrity.cloud.project.number` ➜ [cloudProjectNumber]
 *
 * @property cloudProjectNumber The numeric GCP project ID associated with the Play
 *   Integrity API client. Empty/blank disables the real path. We keep this as a
 *   `String` rather than a `Long` because (a) the real SDK takes a long but local-
 *   property loading is string-based, and (b) it lets us round-trip "set but invalid"
 *   into a single empty-check at injection time.
 */
data class PlayIntegrityConfig(
    val cloudProjectNumber: String = ""
) {
    /**
     * True iff the build has a usable cloud project number. Anything else returns
     * false — callers should *not* try to "best-effort" the check. A typo'd project
     * number that fails real-time would be far more dangerous than a NoOp.
     */
    val isEnabled: Boolean
        get() = cloudProjectNumber.isNotBlank() && cloudProjectNumber.all { it.isDigit() }

    /**
     * Parses the project number as `Long` for the real SDK. Returns null when
     * [isEnabled] is false. Matches the SDK's `IntegrityManagerFactory.create(...)`
     * `cloudProjectNumber` parameter type.
     */
    fun cloudProjectNumberAsLong(): Long? =
        cloudProjectNumber.toLongOrNull()?.takeIf { isEnabled }
}
