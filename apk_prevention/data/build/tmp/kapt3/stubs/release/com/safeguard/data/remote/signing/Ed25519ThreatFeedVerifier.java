package com.safeguard.data.remote.signing;

import com.safeguard.core.domain.repository.ThreatFeedSigningConfig;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import java.util.Base64;
import javax.inject.Inject;
import javax.inject.Singleton;

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
 * - `EdDSANamedCurveTable.ED_25519_CURVE_SPEC` is the RFC 8032 PureEdDSA-Ed25519 curve.
 * - We re-create the engine per call. EdDSAEngine state isn't documented as thread-safe
 *   and the throughput requirement is one verify per ~12h periodic worker run, so the
 *   object-allocation cost is negligible compared to the ~0.5 ms verify itself.
 */
@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u0000 \u001a2\u00020\u0001:\u0002\u001a\u001bB\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0012\u0010\u0013\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u0016H\u0016R!\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\f\u0010\r\u001a\u0004\b\n\u0010\u000bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000e\u001a\u0004\u0018\u00010\u000f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0012\u0010\r\u001a\u0004\b\u0010\u0010\u0011R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/safeguard/data/remote/signing/Ed25519ThreatFeedVerifier;", "Lcom/safeguard/data/remote/signing/ThreatFeedSignatureVerifier;", "signingConfig", "Lcom/safeguard/core/domain/repository/ThreatFeedSigningConfig;", "moshi", "Lcom/squareup/moshi/Moshi;", "(Lcom/safeguard/core/domain/repository/ThreatFeedSigningConfig;Lcom/squareup/moshi/Moshi;)V", "envelopeAdapter", "Lcom/squareup/moshi/JsonAdapter;", "Lcom/safeguard/data/remote/signing/Ed25519ThreatFeedVerifier$RawThreatFeedShape;", "getEnvelopeAdapter", "()Lcom/squareup/moshi/JsonAdapter;", "envelopeAdapter$delegate", "Lkotlin/Lazy;", "pinnedPublicKey", "Lnet/i2p/crypto/eddsa/EdDSAPublicKey;", "getPinnedPublicKey", "()Lnet/i2p/crypto/eddsa/EdDSAPublicKey;", "pinnedPublicKey$delegate", "decodeBase64", "", "s", "", "verify", "Lcom/safeguard/data/remote/signing/VerifiedThreatFeed;", "rawJson", "Companion", "RawThreatFeedShape", "data_release"})
public final class Ed25519ThreatFeedVerifier implements com.safeguard.data.remote.signing.ThreatFeedSignatureVerifier {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ThreatFeedSigningConfig signingConfig = null;
    @org.jetbrains.annotations.NotNull
    private final com.squareup.moshi.Moshi moshi = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy envelopeAdapter$delegate = null;
    
    /**
     * Decode the pinned public-key string lazily. Cached because parsing the spec is
     * cheaper than re-running it every periodic sync, but doing it in `init` would make
     * misconfigured builds fail at DI graph construction (silent crash on launch) instead
     * of at first sync (visible failure on the dashboard tile).
     */
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy pinnedPublicKey$delegate = null;
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String SIGNED_SCHEMA_V1 = "v1.signed";
    public static final int ED25519_RAW_PUB_LEN = 32;
    public static final int ED25519_SIG_LEN = 64;
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.data.remote.signing.Ed25519ThreatFeedVerifier.Companion Companion = null;
    
    @javax.inject.Inject
    public Ed25519ThreatFeedVerifier(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ThreatFeedSigningConfig signingConfig, @org.jetbrains.annotations.NotNull
    com.squareup.moshi.Moshi moshi) {
        super();
    }
    
    private final com.squareup.moshi.JsonAdapter<com.safeguard.data.remote.signing.Ed25519ThreatFeedVerifier.RawThreatFeedShape> getEnvelopeAdapter() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public com.safeguard.data.remote.signing.VerifiedThreatFeed verify(@org.jetbrains.annotations.NotNull
    java.lang.String rawJson) {
        return null;
    }
    
    /**
     * Decode the pinned public-key string lazily. Cached because parsing the spec is
     * cheaper than re-running it every periodic sync, but doing it in `init` would make
     * misconfigured builds fail at DI graph construction (silent crash on launch) instead
     * of at first sync (visible failure on the dashboard tile).
     */
    private final net.i2p.crypto.eddsa.EdDSAPublicKey getPinnedPublicKey() {
        return null;
    }
    
    private final byte[] decodeBase64(java.lang.String s) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/safeguard/data/remote/signing/Ed25519ThreatFeedVerifier$Companion;", "", "()V", "ED25519_RAW_PUB_LEN", "", "ED25519_SIG_LEN", "SIGNED_SCHEMA_V1", "", "data_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    /**
     * Wire shape DTO. Internal because no other caller should be parsing the raw envelope
     * directly — they should consume the [VerifiedThreatFeed] sealed result instead.
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u001c\n\u0002\u0010\b\n\u0002\b\u0002\b\u0080\b\u0018\u00002\u00020\u0001Be\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u0001\u0012\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\u0002\u0010\rJ\u000b\u0010\u001c\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0016J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010!\u001a\u0004\u0018\u00010\u0001H\u00c6\u0003J\u0010\u0010\"\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0016J\u0010\u0010#\u001a\u0004\u0018\u00010\fH\u00c6\u0003\u00a2\u0006\u0002\u0010\u000fJn\u0010$\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u00062\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u00012\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u00062\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00c6\u0001\u00a2\u0006\u0002\u0010%J\u0013\u0010&\u001a\u00020\f2\b\u0010\'\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010(\u001a\u00020)H\u00d6\u0001J\t\u0010*\u001a\u00020\u0003H\u00d6\u0001R\u0015\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\n\n\u0002\u0010\u0010\u001a\u0004\b\u000e\u0010\u000fR\u0013\u0010\t\u001a\u0004\u0018\u00010\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0015\u0010\n\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\n\n\u0002\u0010\u0017\u001a\u0004\b\u0015\u0010\u0016R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0014R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0014R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0014R\u0015\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\n\n\u0002\u0010\u0017\u001a\u0004\b\u001b\u0010\u0016\u00a8\u0006+"}, d2 = {"Lcom/safeguard/data/remote/signing/Ed25519ThreatFeedVerifier$RawThreatFeedShape;", "", "schema", "", "keyId", "signedAtMs", "", "payloadB64", "signatureB64", "items", "nextCursorMs", "hasMore", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Long;Ljava/lang/Boolean;)V", "getHasMore", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getItems", "()Ljava/lang/Object;", "getKeyId", "()Ljava/lang/String;", "getNextCursorMs", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getPayloadB64", "getSchema", "getSignatureB64", "getSignedAtMs", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Long;Ljava/lang/Boolean;)Lcom/safeguard/data/remote/signing/Ed25519ThreatFeedVerifier$RawThreatFeedShape;", "equals", "other", "hashCode", "", "toString", "data_release"})
    public static final class RawThreatFeedShape {
        @org.jetbrains.annotations.Nullable
        private final java.lang.String schema = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String keyId = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.Long signedAtMs = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String payloadB64 = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String signatureB64 = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.Object items = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.Long nextCursorMs = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.Boolean hasMore = null;
        
        public RawThreatFeedShape(@com.squareup.moshi.Json(name = "schema")
        @org.jetbrains.annotations.Nullable
        java.lang.String schema, @com.squareup.moshi.Json(name = "key_id")
        @org.jetbrains.annotations.Nullable
        java.lang.String keyId, @com.squareup.moshi.Json(name = "signed_at_ms")
        @org.jetbrains.annotations.Nullable
        java.lang.Long signedAtMs, @com.squareup.moshi.Json(name = "payload_b64")
        @org.jetbrains.annotations.Nullable
        java.lang.String payloadB64, @com.squareup.moshi.Json(name = "signature_b64")
        @org.jetbrains.annotations.Nullable
        java.lang.String signatureB64, @com.squareup.moshi.Json(name = "items")
        @org.jetbrains.annotations.Nullable
        java.lang.Object items, @com.squareup.moshi.Json(name = "next_cursor_ms")
        @org.jetbrains.annotations.Nullable
        java.lang.Long nextCursorMs, @com.squareup.moshi.Json(name = "has_more")
        @org.jetbrains.annotations.Nullable
        java.lang.Boolean hasMore) {
            super();
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getSchema() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getKeyId() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.Long getSignedAtMs() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getPayloadB64() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getSignatureB64() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.Object getItems() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.Long getNextCursorMs() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.Boolean getHasMore() {
            return null;
        }
        
        public RawThreatFeedShape() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.Long component3() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component4() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component5() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.Object component6() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.Long component7() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.Boolean component8() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.safeguard.data.remote.signing.Ed25519ThreatFeedVerifier.RawThreatFeedShape copy(@com.squareup.moshi.Json(name = "schema")
        @org.jetbrains.annotations.Nullable
        java.lang.String schema, @com.squareup.moshi.Json(name = "key_id")
        @org.jetbrains.annotations.Nullable
        java.lang.String keyId, @com.squareup.moshi.Json(name = "signed_at_ms")
        @org.jetbrains.annotations.Nullable
        java.lang.Long signedAtMs, @com.squareup.moshi.Json(name = "payload_b64")
        @org.jetbrains.annotations.Nullable
        java.lang.String payloadB64, @com.squareup.moshi.Json(name = "signature_b64")
        @org.jetbrains.annotations.Nullable
        java.lang.String signatureB64, @com.squareup.moshi.Json(name = "items")
        @org.jetbrains.annotations.Nullable
        java.lang.Object items, @com.squareup.moshi.Json(name = "next_cursor_ms")
        @org.jetbrains.annotations.Nullable
        java.lang.Long nextCursorMs, @com.squareup.moshi.Json(name = "has_more")
        @org.jetbrains.annotations.Nullable
        java.lang.Boolean hasMore) {
            return null;
        }
        
        @java.lang.Override
        public boolean equals(@org.jetbrains.annotations.Nullable
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public java.lang.String toString() {
            return null;
        }
    }
}