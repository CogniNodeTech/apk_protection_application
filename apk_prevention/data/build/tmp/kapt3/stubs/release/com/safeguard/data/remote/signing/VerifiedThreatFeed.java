package com.safeguard.data.remote.signing;

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
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\bv\u0018\u00002\u00020\u0001:\u0003\u0002\u0003\u0004\u0082\u0001\u0003\u0005\u0006\u0007\u00a8\u0006\b"}, d2 = {"Lcom/safeguard/data/remote/signing/VerifiedThreatFeed;", "", "Rejected", "Signed", "Unsigned", "Lcom/safeguard/data/remote/signing/VerifiedThreatFeed$Rejected;", "Lcom/safeguard/data/remote/signing/VerifiedThreatFeed$Signed;", "Lcom/safeguard/data/remote/signing/VerifiedThreatFeed$Unsigned;", "data_release"})
public abstract interface VerifiedThreatFeed {
    
    /**
     * Verification refused this response. The repository propagates [reason] up to the
     * status store so the dashboard can render a precise failure (e.g. `signature_invalid`
     * vs `unsigned_response_in_signed_build`) instead of the generic `parse_error`.
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/safeguard/data/remote/signing/VerifiedThreatFeed$Rejected;", "Lcom/safeguard/data/remote/signing/VerifiedThreatFeed;", "reason", "", "(Ljava/lang/String;)V", "getReason", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "data_release"})
    public static final class Rejected implements com.safeguard.data.remote.signing.VerifiedThreatFeed {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String reason = null;
        
        public Rejected(@org.jetbrains.annotations.NotNull
        java.lang.String reason) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getReason() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.safeguard.data.remote.signing.VerifiedThreatFeed.Rejected copy(@org.jetbrains.annotations.NotNull
        java.lang.String reason) {
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
    
    /**
     * The response was the Phase 3.1 signed envelope and the signature matched the
     * pinned public key. [keyId] is the server-claimed key ID (informational; the
     * verifier already trusted the bundled key, not this field). [signedAtMs] is the
     * server-claimed signing timestamp — useful for diagnosing replay-of-stale-feed
     * attacks if we ever decide to enforce a max-age window in a later phase.
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\f\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0007J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\'\u0010\u0010\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0018"}, d2 = {"Lcom/safeguard/data/remote/signing/VerifiedThreatFeed$Signed;", "Lcom/safeguard/data/remote/signing/VerifiedThreatFeed;", "keyId", "", "signedAtMs", "", "payloadJson", "(Ljava/lang/String;JLjava/lang/String;)V", "getKeyId", "()Ljava/lang/String;", "getPayloadJson", "getSignedAtMs", "()J", "component1", "component2", "component3", "copy", "equals", "", "other", "", "hashCode", "", "toString", "data_release"})
    public static final class Signed implements com.safeguard.data.remote.signing.VerifiedThreatFeed {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String keyId = null;
        private final long signedAtMs = 0L;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String payloadJson = null;
        
        public Signed(@org.jetbrains.annotations.NotNull
        java.lang.String keyId, long signedAtMs, @org.jetbrains.annotations.NotNull
        java.lang.String payloadJson) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getKeyId() {
            return null;
        }
        
        public final long getSignedAtMs() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getPayloadJson() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        public final long component2() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.safeguard.data.remote.signing.VerifiedThreatFeed.Signed copy(@org.jetbrains.annotations.NotNull
        java.lang.String keyId, long signedAtMs, @org.jetbrains.annotations.NotNull
        java.lang.String payloadJson) {
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
    
    /**
     * The response was the legacy unsigned shape and verification is *not* enforced in
     * this build. Carries the same payload fields the legacy code path used so callers can
     * stay agnostic of which mode they're in.
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/safeguard/data/remote/signing/VerifiedThreatFeed$Unsigned;", "Lcom/safeguard/data/remote/signing/VerifiedThreatFeed;", "payloadJson", "", "(Ljava/lang/String;)V", "getPayloadJson", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "data_release"})
    public static final class Unsigned implements com.safeguard.data.remote.signing.VerifiedThreatFeed {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String payloadJson = null;
        
        public Unsigned(@org.jetbrains.annotations.NotNull
        java.lang.String payloadJson) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getPayloadJson() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.safeguard.data.remote.signing.VerifiedThreatFeed.Unsigned copy(@org.jetbrains.annotations.NotNull
        java.lang.String payloadJson) {
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