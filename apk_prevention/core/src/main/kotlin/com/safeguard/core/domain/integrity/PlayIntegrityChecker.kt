package com.safeguard.core.domain.integrity

/**
 * Phase 3.4 Play Integrity API cross-check.
 *
 * Implementations live in `:app` (Hilt-wired) — `:core` only declares the contract so
 * Layer 6 (`security:`) can pull a verdict in without depending on the Play Integrity
 * SDK. Two concrete impls ship today:
 *
 *  - [NoOpPlayIntegrityChecker] — the default. Always returns
 *    [PlayIntegrityVerdict.Disabled]. Used in dev builds, on devices without Play
 *    Services, and any build whose `local.properties` does not set a cloud project
 *    number for Play Integrity.
 *  - `GooglePlayIntegrityChecker` (in `:app`) — gated on a build-time GCP cloud
 *    project number. Wraps the official `com.google.android.play:integrity` SDK and
 *    forwards the resulting attestation token to the SafeGuard server for decoding.
 *    NOT yet wired in code; Hilt selects between the two via [PlayIntegrityConfig].
 *
 * Why an interface and a NoOp instead of an `Optional<…>`? The decision engine likes
 * to ask "what does Play Integrity say about this scan?" *every* time, and an absent
 * verdict needs an explicit `Source.DISABLED` so the forensic log always knows whether
 * we *tried*. A `null` would mush "didn't try" into "tried and failed".
 */
interface PlayIntegrityChecker {

    /**
     * Returns the freshest available verdict, blocking the caller. Implementations are
     * expected to apply their own timeouts (the default real impl uses 5s) and cache
     * recent verdicts so a tight loop of scans doesn't spam the SDK / server. The NoOp
     * is constant-time so no caching is needed there.
     *
     * Must never throw — checkers report failure as
     * [PlayIntegrityVerdict.Source.PLAY_INTEGRITY_API_ERROR] with `note` populated. A
     * thrown exception here would crash the scan pipeline mid-Layer-6, which is far
     * worse than a degraded verdict.
     */
    suspend fun check(scanContext: ScanIntegrityContext): PlayIntegrityVerdict
}

/**
 * Inputs the integrity checker may use to seed its request. Today these are only used
 * by the real Google checker (the SDK requires a request hash that ties the
 * attestation to a specific operation). The NoOp ignores them.
 *
 * @property requestHash A small opaque value the server expects to see echoed inside
 *   the decoded Play Integrity token. Production clients typically pass the SHA-256
 *   of the APK being verified — that gives the server a guarantee the integrity
 *   verdict was produced *for this specific scan*, not replayed from an earlier one.
 */
data class ScanIntegrityContext(
    val requestHash: String
)
