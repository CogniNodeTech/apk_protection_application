package com.safeguard.data.remote.signing

import com.safeguard.core.domain.repository.ThreatFeedSigningConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.KeyPairGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

/**
 * Unit-tests for [Ed25519ThreatFeedVerifier]. We generate fresh keypairs in-memory per
 * test (no checked-in keys) and round-trip envelopes through the verifier the same way
 * `server/feed_signer.py` does, so a regression in either side immediately diverges the
 * wire format and one of these tests fails.
 *
 * The Python signer is exercised in `server/test_feed_signer.py`; this Kotlin file is
 * the on-device counterpart. Together they pin the cross-language contract for Phase 3.1.
 */
class Ed25519ThreatFeedVerifierTest {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private fun freshKeypair(): Pair<EdDSAPrivateKey, ByteArray> {
        val gen = KeyPairGenerator()
        val pair = gen.generateKeyPair()
        // `EdDSAPublicKey.abyte` is the 32-byte raw public key we need to feed back through
        // `ThreatFeedSigningConfig` (and that the Python signer emits via `PublicFormat.Raw`).
        val priv = pair.private as EdDSAPrivateKey
        val pubBytes = (pair.public as net.i2p.crypto.eddsa.EdDSAPublicKey).abyte
        return priv to pubBytes
    }

    private fun signEnvelope(priv: EdDSAPrivateKey, innerPayload: String): String {
        val sigEngine = EdDSAEngine()
        sigEngine.initSign(priv)
        sigEngine.update(innerPayload.toByteArray(Charsets.UTF_8))
        val sigBytes = sigEngine.sign()
        val payloadB64 = Base64.getEncoder().encodeToString(innerPayload.toByteArray(Charsets.UTF_8))
        val signatureB64 = Base64.getEncoder().encodeToString(sigBytes)
        return """
            {"schema":"v1.signed","key_id":"feed-test","signed_at_ms":1700000000000,
             "payload_b64":"$payloadB64","signature_b64":"$signatureB64"}
        """.trimIndent()
    }

    private fun configFor(pubBytes: ByteArray): ThreatFeedSigningConfig =
        ThreatFeedSigningConfig(
            keyId = "feed-test",
            publicKeyB64 = Base64.getEncoder().encodeToString(pubBytes)
        )

    @Test
    fun signingDisabled_acceptsLegacyUnsignedBody() {
        // Build with no public key bundled. Verifier should accept any well-formed legacy
        // body without inspecting a signature — that's the default state for dev/mock
        // builds and we don't want to brick them when the production server starts signing.
        val verifier = Ed25519ThreatFeedVerifier(ThreatFeedSigningConfig.DISABLED, moshi)
        val legacyBody = """{"items":[],"next_cursor_ms":1234,"has_more":false}"""

        val result = verifier.verify(legacyBody)
        assertTrue(result is VerifiedThreatFeed.Unsigned)
        assertEquals(legacyBody, (result as VerifiedThreatFeed.Unsigned).payloadJson)
    }

    @Test
    fun signingDisabled_acceptsSignedEnvelopeWithoutVerifying() {
        // Edge case: dev build (no key) talking to a prod server (signs every response).
        // Verifier must still extract the inner payload so the worker can advance.
        val (priv, _) = freshKeypair()
        val innerJson = """{"items":[],"next_cursor_ms":99,"has_more":false}"""
        val envelope = signEnvelope(priv, innerJson)

        val verifier = Ed25519ThreatFeedVerifier(ThreatFeedSigningConfig.DISABLED, moshi)
        val result = verifier.verify(envelope)

        assertTrue(result is VerifiedThreatFeed.Signed)
        assertEquals(innerJson, (result as VerifiedThreatFeed.Signed).payloadJson)
        assertEquals("feed-test", result.keyId)
    }

    @Test
    fun signingEnforced_acceptsValidSignedEnvelope() {
        val (priv, pub) = freshKeypair()
        val innerJson = """{"items":[],"next_cursor_ms":777,"has_more":false}"""
        val envelope = signEnvelope(priv, innerJson)

        val verifier = Ed25519ThreatFeedVerifier(configFor(pub), moshi)
        val result = verifier.verify(envelope)

        assertTrue(result is VerifiedThreatFeed.Signed)
        assertEquals(innerJson, (result as VerifiedThreatFeed.Signed).payloadJson)
    }

    @Test
    fun signingEnforced_rejectsTamperedPayload() {
        val (priv, pub) = freshKeypair()
        val innerJson = """{"items":[],"next_cursor_ms":1,"has_more":false}"""
        val originalEnvelope = signEnvelope(priv, innerJson)
        // Swap the payload_b64 for a different (forged) inner JSON; the signature no longer
        // matches the new bytes. Re-encoding the new body keeps everything else valid so we
        // know the rejection is signature-driven, not parse-driven.
        val forgedInner = """{"items":[{"sha256":"00"}],"next_cursor_ms":1,"has_more":false}"""
        val forgedB64 = Base64.getEncoder().encodeToString(forgedInner.toByteArray(Charsets.UTF_8))
        val tampered = originalEnvelope.replace(
            Regex(""""payload_b64":"[^"]+""""),
            "\"payload_b64\":\"$forgedB64\""
        )

        val verifier = Ed25519ThreatFeedVerifier(configFor(pub), moshi)
        val result = verifier.verify(tampered)

        assertTrue(result is VerifiedThreatFeed.Rejected)
        assertEquals("signature_invalid", (result as VerifiedThreatFeed.Rejected).reason)
    }

    @Test
    fun signingEnforced_rejectsWrongPublicKey() {
        val (priv, _) = freshKeypair()
        val (_, otherPub) = freshKeypair()  // unrelated keypair
        val envelope = signEnvelope(priv, """{"items":[],"next_cursor_ms":1,"has_more":false}""")

        val verifier = Ed25519ThreatFeedVerifier(configFor(otherPub), moshi)
        val result = verifier.verify(envelope)

        assertTrue(result is VerifiedThreatFeed.Rejected)
        assertEquals("signature_invalid", (result as VerifiedThreatFeed.Rejected).reason)
    }

    @Test
    fun signingEnforced_rejectsLegacyUnsignedDowngrade() {
        // The "downgrade attack" case: a build that should require signed envelopes is
        // handed a legacy unsigned body. Must refuse — distinct rejection reason so ops
        // can tell this apart from a genuine signature mismatch.
        val (_, pub) = freshKeypair()
        val verifier = Ed25519ThreatFeedVerifier(configFor(pub), moshi)
        val legacyBody = """{"items":[],"next_cursor_ms":1234,"has_more":false}"""

        val result = verifier.verify(legacyBody)

        assertTrue(result is VerifiedThreatFeed.Rejected)
        assertEquals(
            "unsigned_response_in_signed_build",
            (result as VerifiedThreatFeed.Rejected).reason
        )
    }

    @Test
    fun signingEnforced_rejectsMissingSignatureField() {
        val (_, pub) = freshKeypair()
        val verifier = Ed25519ThreatFeedVerifier(configFor(pub), moshi)
        val payloadB64 = Base64.getEncoder().encodeToString(
            """{"items":[],"next_cursor_ms":1,"has_more":false}""".toByteArray(Charsets.UTF_8)
        )
        // Envelope claims to be signed (schema=v1.signed) but the signature_b64 is empty.
        val envelopeNoSig = """
            {"schema":"v1.signed","key_id":"feed-test","signed_at_ms":1700000000000,
             "payload_b64":"$payloadB64","signature_b64":""}
        """.trimIndent()

        val result = verifier.verify(envelopeNoSig)

        assertTrue(result is VerifiedThreatFeed.Rejected)
        assertEquals(
            "signed_envelope_missing_fields",
            (result as VerifiedThreatFeed.Rejected).reason
        )
    }

    @Test
    fun signingEnforced_rejectsMalformedBase64() {
        val (_, pub) = freshKeypair()
        val verifier = Ed25519ThreatFeedVerifier(configFor(pub), moshi)
        val envelopeBadB64 = """
            {"schema":"v1.signed","key_id":"feed-test","signed_at_ms":1700000000000,
             "payload_b64":"@@@not-base64@@@","signature_b64":"AAAA"}
        """.trimIndent()

        val result = verifier.verify(envelopeBadB64)

        assertTrue(result is VerifiedThreatFeed.Rejected)
        assertTrue(
            "Reason should identify the decode stage that failed",
            (result as VerifiedThreatFeed.Rejected).reason.contains("decode_failed")
        )
    }

    @Test
    fun signingEnforced_rejectsWrongSignatureLength() {
        // A 32-byte signature would never be a valid Ed25519 signature (must be exactly 64
        // bytes per RFC 8032). Catch this before the JCE engine wastes cycles attempting
        // to verify it, and surface a precise reason for the dashboard.
        val (_, pub) = freshKeypair()
        val verifier = Ed25519ThreatFeedVerifier(configFor(pub), moshi)
        val payloadB64 = Base64.getEncoder().encodeToString(
            """{"items":[],"next_cursor_ms":1,"has_more":false}""".toByteArray(Charsets.UTF_8)
        )
        val tooShortSigB64 = Base64.getEncoder().encodeToString(ByteArray(32) { 0 })
        val envelope = """
            {"schema":"v1.signed","key_id":"feed-test","signed_at_ms":1700000000000,
             "payload_b64":"$payloadB64","signature_b64":"$tooShortSigB64"}
        """.trimIndent()

        val result = verifier.verify(envelope)
        assertTrue(result is VerifiedThreatFeed.Rejected)
        assertTrue(
            "Reason should encode the unexpected signature length",
            (result as VerifiedThreatFeed.Rejected).reason.startsWith("signature_wrong_length")
        )
    }

    @Test
    fun envelopeParseFailure_isClassifiedDistinctly() {
        val (_, pub) = freshKeypair()
        val verifier = Ed25519ThreatFeedVerifier(configFor(pub), moshi)

        val result = verifier.verify("{not valid json")
        assertTrue(result is VerifiedThreatFeed.Rejected)
        assertTrue(
            "Reason should namespace envelope-parse failures",
            (result as VerifiedThreatFeed.Rejected).reason.startsWith("envelope_parse")
        )
    }
}
