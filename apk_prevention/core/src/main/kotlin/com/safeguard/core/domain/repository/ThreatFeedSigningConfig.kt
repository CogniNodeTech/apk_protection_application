package com.safeguard.core.domain.repository

/**
 * Build-time-pinned configuration for the on-device verifier of the SafeGuard threat-feed
 * (Phase 3.1). The verifier checks the Ed25519 signature on every `/v1/threat-feed`
 * response before any row reaches the local malware table.
 *
 * Why pinned at *build* time, not refreshed from the server: a compromised server could
 * simply ship its own "rotated" public key alongside its own malicious feed, defeating the
 * whole point of signing. So the trust anchor lives in the APK and rotates with app
 * releases. The server's [`/v1/threat-feed/public-key`] endpoint is operational metadata
 * only — useful for ops dashboards, never used by the verifier at runtime.
 *
 * Both fields are optional. [publicKeyB64] empty means signing is disabled in this build
 * (mock / dev / unsigned-fallback). When empty, `ThreatFeedSignatureVerifier` accepts the
 * legacy unsigned envelope unconditionally; when non-empty, it requires every response to
 * be the Phase 3.1 signed envelope and verifies the signature against this key.
 *
 * @property keyId stable identifier (e.g. `feed-2026-04`) — currently informational only,
 *   reserved for future multi-key rotation support.
 * @property publicKeyB64 base64-encoded raw 32-byte Ed25519 public key. Empty = signing
 *   disabled in this build.
 */
data class ThreatFeedSigningConfig(
    val keyId: String,
    val publicKeyB64: String
) {
    /**
     * `true` when this build was packaged with a real public key. The repository uses this
     * to decide whether to *require* signed envelopes (build with key) or to gracefully
     * accept legacy unsigned ones (dev/mock builds without a key).
     */
    val isSigningEnforced: Boolean
        get() = publicKeyB64.isNotBlank()

    companion object {
        /** Sentinel for builds that have not configured threat-feed signing. */
        val DISABLED = ThreatFeedSigningConfig(keyId = "", publicKeyB64 = "")
    }
}
