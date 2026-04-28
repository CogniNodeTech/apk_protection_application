package com.safeguard.data.remote.signing

import com.safeguard.core.domain.repository.ThreatFeedSigningConfig
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verifies the Phase 3.1 signed envelope shipped by the SafeGuard server's
 * `GET /v1/threat-feed` endpoint. The trust anchor is the build-time-pinned public key in
 * [ThreatFeedSigningConfig] — *not* whatever the server claims via `key_id` at runtime, so
 * a compromised server cannot rotate to its own key.
 *
 * Verifier policy (enforced here, not in the repository, so the test matrix is local):
 *
 * | Build has public key? | Response is signed envelope?     | Result                    |
 * |-----------------------|----------------------------------|---------------------------|
 * | No                    | No (legacy)                      | Unsigned                  |
 * | No                    | Yes                              | Signed (treated as good)  |
 * | Yes                   | No (legacy)                      | Rejected — refuses to     |
 * |                       |                                  | downgrade once enforced   |
 * | Yes                   | Yes, signature OK                | Signed                    |
 * | Yes                   | Yes, signature mismatch          | Rejected (sig_invalid)    |
 * | Yes                   | Yes, malformed b64 / lengths     | Rejected (sig_malformed)  |
 *
 * The "build with key sees a signed envelope it can't verify" case is treated identically
 * regardless of whether the server tried to sign with a *different* key (`key_id` mismatch
 * is fine to log but should still fail verify, otherwise it's a bypass) — we just say
 * "rejected, signature_invalid".
 *
 * Implementation notes:
 *  - `EdDSANamedCurveTable.ED_25519_CURVE_SPEC` is the RFC 8032 PureEdDSA-Ed25519 curve.
 *  - We re-create the engine per call. EdDSAEngine state isn't documented as thread-safe
 *    and the throughput requirement is one verify per ~12h periodic worker run, so the
 *    object-allocation cost is negligible compared to the ~0.5 ms verify itself.
 */
@Singleton
class Ed25519ThreatFeedVerifier @Inject constructor(
    private val signingConfig: ThreatFeedSigningConfig,
    private val moshi: Moshi
) : ThreatFeedSignatureVerifier {

    private val envelopeAdapter: JsonAdapter<RawThreatFeedShape> by lazy {
        moshi.adapter(RawThreatFeedShape::class.java).failOnUnknown().lenient()
    }

    override fun verify(rawJson: String): VerifiedThreatFeed {
        // Step 1: parse the wire shape so we can branch on `schema`.
        val shape = try {
            envelopeAdapter.fromJson(rawJson)
                ?: return VerifiedThreatFeed.Rejected("envelope_null")
        } catch (e: Exception) {
            return VerifiedThreatFeed.Rejected("envelope_parse: ${e.javaClass.simpleName}")
        }

        // Step 2: detect signed vs unsigned. The signed envelope is identified strictly by
        // `schema == "v1.signed"` — we don't infer from "payload_b64 is set" because a
        // legacy server that accidentally returns a stray field shouldn't be treated as
        // signed without the schema marker.
        val isSignedShape = shape.schema == SIGNED_SCHEMA_V1

        if (!isSignedShape) {
            // Unsigned shape. Build with a key refuses to consume it (downgrade attack);
            // build without a key accepts the raw JSON as the inner payload.
            return if (signingConfig.isSigningEnforced) {
                VerifiedThreatFeed.Rejected("unsigned_response_in_signed_build")
            } else {
                VerifiedThreatFeed.Unsigned(rawJson)
            }
        }

        // Step 3: signed shape, with or without enforcement.
        val payloadB64 = shape.payloadB64
        val signatureB64 = shape.signatureB64
        if (payloadB64.isNullOrBlank() || signatureB64.isNullOrBlank()) {
            return VerifiedThreatFeed.Rejected("signed_envelope_missing_fields")
        }

        if (!signingConfig.isSigningEnforced) {
            // Build does not enforce verification — accept the inner payload without
            // touching the signature. This is the "dev build pointed at a prod-signing
            // server" case; the legacy/test path stays usable.
            val payloadBytes = decodeBase64(payloadB64)
                ?: return VerifiedThreatFeed.Rejected("payload_b64_decode_failed")
            return VerifiedThreatFeed.Signed(
                keyId = shape.keyId.orEmpty(),
                signedAtMs = shape.signedAtMs ?: 0L,
                payloadJson = payloadBytes.toString(Charsets.UTF_8)
            )
        }

        // Step 4: enforced path — verify signature against the pinned public key.
        val payloadBytes = decodeBase64(payloadB64)
            ?: return VerifiedThreatFeed.Rejected("payload_b64_decode_failed")
        val signatureBytes = decodeBase64(signatureB64)
            ?: return VerifiedThreatFeed.Rejected("signature_b64_decode_failed")
        if (signatureBytes.size != ED25519_SIG_LEN) {
            return VerifiedThreatFeed.Rejected("signature_wrong_length:${signatureBytes.size}")
        }
        val publicKey = pinnedPublicKey
            ?: return VerifiedThreatFeed.Rejected("pinned_public_key_invalid")

        val ok = try {
            val engine = EdDSAEngine()
            engine.initVerify(publicKey)
            engine.update(payloadBytes)
            engine.verify(signatureBytes)
        } catch (e: Exception) {
            // Defence-in-depth — i2p-eddsa documents InvalidKeyException / SignatureException
            // but a buggy proxy could produce other forms of malformed input. Anything that
            // reaches here is still a failed verify, just with extra context for the dashboard.
            return VerifiedThreatFeed.Rejected("verify_threw: ${e.javaClass.simpleName}")
        }

        if (!ok) {
            return VerifiedThreatFeed.Rejected("signature_invalid")
        }

        return VerifiedThreatFeed.Signed(
            keyId = shape.keyId.orEmpty(),
            signedAtMs = shape.signedAtMs ?: 0L,
            payloadJson = payloadBytes.toString(Charsets.UTF_8)
        )
    }

    /**
     * Decode the pinned public-key string lazily. Cached because parsing the spec is
     * cheaper than re-running it every periodic sync, but doing it in `init` would make
     * misconfigured builds fail at DI graph construction (silent crash on launch) instead
     * of at first sync (visible failure on the dashboard tile).
     */
    private val pinnedPublicKey: EdDSAPublicKey? by lazy {
        val raw = decodeBase64(signingConfig.publicKeyB64) ?: return@lazy null
        if (raw.size != ED25519_RAW_PUB_LEN) return@lazy null
        val spec = EdDSAPublicKeySpec(raw, EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519))
        EdDSAPublicKey(spec)
    }

    /**
     * Wire shape DTO. Internal because no other caller should be parsing the raw envelope
     * directly — they should consume the [VerifiedThreatFeed] sealed result instead.
     */
    internal data class RawThreatFeedShape(
        @com.squareup.moshi.Json(name = "schema") val schema: String? = null,
        @com.squareup.moshi.Json(name = "key_id") val keyId: String? = null,
        @com.squareup.moshi.Json(name = "signed_at_ms") val signedAtMs: Long? = null,
        @com.squareup.moshi.Json(name = "payload_b64") val payloadB64: String? = null,
        @com.squareup.moshi.Json(name = "signature_b64") val signatureB64: String? = null,
        // Legacy unsigned fields are tolerated but ignored — Moshi `failOnUnknown` is set
        // so we have to declare them explicitly. Once a deployment has migrated fully to
        // signed envelopes these will always be null.
        @com.squareup.moshi.Json(name = "items") val items: Any? = null,
        @com.squareup.moshi.Json(name = "next_cursor_ms") val nextCursorMs: Long? = null,
        @com.squareup.moshi.Json(name = "has_more") val hasMore: Boolean? = null
    )

    private fun decodeBase64(s: String): ByteArray? = try {
        // `java.util.Base64` (Java 8, available on Android API 26+) keeps the verifier
        // testable on plain JVM without Robolectric / Android stubs. `getMimeDecoder` is
        // tolerant of trailing newlines and stray whitespace that httpx / proxy logs sometimes
        // introduce; the strict `getDecoder` would reject those even though the underlying
        // bytes are valid.
        Base64.getMimeDecoder().decode(s.trim())
    } catch (e: IllegalArgumentException) {
        null
    }

    companion object {
        const val SIGNED_SCHEMA_V1 = "v1.signed"
        const val ED25519_RAW_PUB_LEN = 32
        const val ED25519_SIG_LEN = 64
    }
}

/**
 * Verifier abstraction used by the threat-feed repository. Indirection gives us a unit-test
 * seam (the JVM-side test substitutes a fake `ThreatFeedSignatureVerifier`) without
 * spinning up Robolectric for every signed-envelope assertion.
 */
interface ThreatFeedSignatureVerifier {
    fun verify(rawJson: String): VerifiedThreatFeed
}
