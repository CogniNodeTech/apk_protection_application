package com.safeguard.data.remote.signing

/**
 * Result of verifying / decoding a `/v1/threat-feed` response. The repository switches on
 * this sealed result to decide whether to upsert rows or short-circuit the sync as failed.
 *
 * Why a sealed type instead of throwing exceptions: the verifier is on the hot path of the
 * threat-feed worker. Exception flow control would force every test (and every catch site)
 * to enumerate JCE / I2P-EdDSA exception classes that aren't part of our domain. The
 * sealed type also makes the four failure modes self-documenting: missing envelope vs
 * malformed envelope vs bad signature vs malformed inner JSON.
 */
sealed interface VerifiedThreatFeed {

    /**
     * The response was the legacy unsigned shape and verification is *not* enforced in
     * this build. Carries the same payload fields the legacy code path used so callers can
     * stay agnostic of which mode they're in.
     */
    data class Unsigned(val payloadJson: String) : VerifiedThreatFeed

    /**
     * The response was the Phase 3.1 signed envelope and the signature matched the
     * pinned public key. [keyId] is the server-claimed key ID (informational; the
     * verifier already trusted the bundled key, not this field). [signedAtMs] is the
     * server-claimed signing timestamp — useful for diagnosing replay-of-stale-feed
     * attacks if we ever decide to enforce a max-age window in a later phase.
     */
    data class Signed(
        val keyId: String,
        val signedAtMs: Long,
        val payloadJson: String
    ) : VerifiedThreatFeed

    /**
     * Verification refused this response. The repository propagates [reason] up to the
     * status store so the dashboard can render a precise failure (e.g. `signature_invalid`
     * vs `unsigned_response_in_signed_build`) instead of the generic `parse_error`.
     */
    data class Rejected(val reason: String) : VerifiedThreatFeed
}
