package com.safeguard.integrity

import android.content.Context
import android.util.Log
import com.safeguard.core.domain.integrity.PlayIntegrityChecker
import com.safeguard.core.domain.integrity.PlayIntegrityConfig
import com.safeguard.core.domain.integrity.PlayIntegrityVerdict
import com.safeguard.core.domain.integrity.ScanIntegrityContext

/**
 * Phase 3.4 scaffold for the real Play Integrity API checker.
 *
 * **Status: scaffold.** This class exists so that the Hilt wiring path that *would*
 * route to the real Play Integrity SDK is reachable, exercised by tests, and visible
 * in code review — but it does not currently call the SDK. Until a follow-up change
 * lands the SDK dependency (`com.google.android.play:integrity:1.4.0+`), the
 * server-side token decoder, and the ProGuard rules required to keep the SDK methods,
 * we fail open: returning [PlayIntegrityVerdict.Source.PLAY_INTEGRITY_API_ERROR] with
 * a clear note. That makes the scaffold both observable in evidence and
 * cryptographically inert (no fake "PASS" verdicts that could be trusted by mistake).
 *
 * To wire the real call once the dependency is approved:
 *  1. Add `implementation("com.google.android.play:integrity:1.4.0")` to
 *     `app/build.gradle.kts`.
 *  2. Replace the body of [check] with `IntegrityManagerFactory.create(context)
 *     .requestIntegrityToken(IntegrityTokenRequest.builder()
 *         .setNonce(scanContext.requestHash)
 *         .setCloudProjectNumber(config.cloudProjectNumberAsLong()!!)
 *         .build())` — wrapped in `withTimeoutOrNull(5_000)`.
 *  3. Forward the resulting JWS to the SafeGuard server's `/v1/integrity/decode`
 *     endpoint and translate the response into a [PlayIntegrityVerdict].
 *
 * The checker MUST NOT decode the token client-side — Google's API explicitly requires
 * server-side decoding under a service account, and any client-side parsing would be
 * trivially spoofable.
 */
class GooglePlayIntegrityChecker(
    @Suppress("UNUSED_PARAMETER") context: Context,
    private val config: PlayIntegrityConfig
) : PlayIntegrityChecker {

    init {
        Log.i(
            TAG,
            "GooglePlayIntegrityChecker scaffold initialised; cloud_project=${config.cloudProjectNumber}." +
                " Real SDK call is not yet wired — verdicts will report PLAY_INTEGRITY_API_ERROR."
        )
    }

    override suspend fun check(scanContext: ScanIntegrityContext): PlayIntegrityVerdict {
        // Defensive: the Hilt module already guarantees `config.isEnabled` is true here,
        // but we re-check so any future code path that constructs this class directly
        // can't bypass the safety rail.
        if (!config.isEnabled) {
            return PlayIntegrityVerdict.Disabled
        }
        return PlayIntegrityVerdict(
            device = PlayIntegrityVerdict.DeviceIntegrityLevel.UNAVAILABLE,
            app = PlayIntegrityVerdict.AppIntegrityLevel.UNAVAILABLE,
            account = PlayIntegrityVerdict.AccountIntegrityLevel.UNAVAILABLE,
            source = PlayIntegrityVerdict.Source.PLAY_INTEGRITY_API_ERROR,
            note = "scaffold: Play Integrity SDK not yet wired (phase 3.4 follow-up)"
        )
    }

    companion object {
        private const val TAG = "GooglePlayIntegrity"
    }
}
