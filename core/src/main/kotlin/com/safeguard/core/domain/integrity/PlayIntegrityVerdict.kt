package com.safeguard.core.domain.integrity

/**
 * Result of a Play Integrity API cross-check (Phase 3.4).
 *
 * The Play Integrity API answers three orthogonal questions about the *device and app
 * environment* (it is **not** a malware scan against the APK that Layer 6 is currently
 * inspecting — that distinction matters):
 *
 *  1. **Device integrity** — is the device that's running SafeGuard a real Android
 *     device with the security model intact (i.e. not rooted, not running on a
 *     Frida-rehosted emulator, etc.).
 *  2. **App integrity** — is the SafeGuard binary running on the device the unmodified
 *     one Google Play distributed (no patched dex, no instrumented test build).
 *  3. **Account integrity** — is the user's Google Play account in a state where
 *     license-bound features can be trusted.
 *
 * For our "is this APK malicious?" decision the device and app verdicts are
 * informational signals: they tell us *how much weight* to put on the on-device layer
 * scores. A `RAW_OR_UNKNOWN` device verdict for example means a sandboxed accessibility
 * tampering scenario is *more* plausible, so a SUSPICIOUS local verdict shouldn't be
 * casually downgraded.
 *
 * We deliberately keep the verdict opaque rather than collapsing it into a numeric
 * score — collapsing would invite "well it's 0.7 trustworthy, ship it" callsites, and
 * the only correct downstream behaviour is gating decisions on the categorical levels.
 */
data class PlayIntegrityVerdict(
    val device: DeviceIntegrityLevel,
    val app: AppIntegrityLevel,
    val account: AccountIntegrityLevel,
    val source: Source,
    /**
     * Free-form one-line note explaining *why* we're at this verdict — useful for
     * forensic logging when an investigator is trying to reconstruct "did the cloud
     * verify trust the local layer scores when this banker was missed".
     */
    val note: String? = null
) {

    /**
     * Mirrors Google Play's `deviceRecognitionVerdict` field. We collapse the three
     * "MEETS_*" tiers into a single ordinal so call sites can reason monotonically.
     *
     * Order matters: higher ordinal => stronger guarantee.
     */
    enum class DeviceIntegrityLevel {
        /** No verdict was obtained — checker disabled, error, or not yet implemented. */
        UNAVAILABLE,
        /** Verdict obtained but the device fails *all* tiers (jailbroken / rooted / hostile env). */
        RAW_OR_UNKNOWN,
        /** Basic integrity only — passes minimal checks but the device may be rooted. */
        BASIC,
        /** Ships an Android-certified hardware-backed attestation. The default tier we expect on real Play devices. */
        DEVICE,
        /** Strong + recently updated — the highest tier Google reports today. */
        STRONG
    }

    /** Mirrors `appRecognitionVerdict`. */
    enum class AppIntegrityLevel {
        UNAVAILABLE,
        UNRECOGNIZED_VERSION,
        PLAY_RECOGNIZED,
    }

    /** Mirrors `accountDetails.appLicensingVerdict`. */
    enum class AccountIntegrityLevel {
        UNAVAILABLE,
        UNLICENSED,
        LICENSED,
    }

    /**
     * Where the verdict came from. Used purely for evidence labelling — the decision
     * engine doesn't branch on it.
     */
    enum class Source {
        /** No checker is wired up in this build — Phase 3.4 NoOp default. */
        DISABLED,
        /** The on-device Play Integrity SDK was called; the verdict is fresh. */
        PLAY_INTEGRITY_API,
        /** A token was obtained but server-side decoding failed; the verdict is
         *  best-effort and shouldn't be used as a hard gate. */
        PLAY_INTEGRITY_API_DECODE_FAILED,
        /** The checker hit a network / SDK error and could not produce a verdict. */
        PLAY_INTEGRITY_API_ERROR,
    }

    /**
     * Compact label for evidence lists (e.g. `"PlayIntegrity: device=DEVICE app=PLAY_RECOGNIZED"`).
     * Stable contract — investigative tooling and the forensic engine grep this format.
     */
    fun toEvidenceLine(): String = buildString {
        append("PlayIntegrity: ")
        append("device=").append(device.name)
        append(" app=").append(app.name)
        append(" account=").append(account.name)
        append(" source=").append(source.name)
        if (!note.isNullOrBlank()) {
            append(" note=\"").append(note.take(120)).append('"')
        }
    }

    /**
     * Convenience predicate: should the cloud-verify path *trust* the on-device layer
     * scores enough to forward them as-is? We require a non-trivial device verdict and a
     * Play-recognised app build. UNAVAILABLE / DISABLED leaves the decision unchanged
     * (Layer 6 already knows how to handle "no integrity data").
     *
     * Currently informational — Phase 3.4 ships the plumbing but does not gate verdicts
     * on it. Wiring it into the zero-trust engine is a follow-up after we have a real
     * Play Integrity backend handshake to test against.
     */
    fun looksTrustworthy(): Boolean =
        device.ordinal >= DeviceIntegrityLevel.BASIC.ordinal &&
            app == AppIntegrityLevel.PLAY_RECOGNIZED

    companion object {
        /**
         * The verdict every `NoOpPlayIntegrityChecker` returns. Pulled out as a
         * companion so call sites can compare with `===` for trace logging.
         */
        val Disabled: PlayIntegrityVerdict = PlayIntegrityVerdict(
            device = DeviceIntegrityLevel.UNAVAILABLE,
            app = AppIntegrityLevel.UNAVAILABLE,
            account = AccountIntegrityLevel.UNAVAILABLE,
            source = Source.DISABLED,
            note = "Play Integrity checker not configured"
        )
    }
}
