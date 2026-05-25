package com.safeguard.core.domain.integrity

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [PlayIntegrityChecker] that always returns [PlayIntegrityVerdict.Disabled].
 *
 * Wired by Hilt whenever [PlayIntegrityConfig.isEnabled] is false — i.e. the build has
 * no `safeguard.play.integrity.cloud.project.number` configured in `local.properties`.
 * That covers dev/CI environments, devices without Play Services, and intentionally
 * "lights-out" deployments.
 *
 * Crucially this is not just a `null` placeholder — Layer 6 still surfaces the
 * `source=DISABLED` evidence line so the forensic engine can distinguish "we don't
 * know because we didn't ask" from "we asked and got UNKNOWN".
 */
@Singleton
class NoOpPlayIntegrityChecker @Inject constructor() : PlayIntegrityChecker {
    override suspend fun check(scanContext: ScanIntegrityContext): PlayIntegrityVerdict =
        PlayIntegrityVerdict.Disabled
}
