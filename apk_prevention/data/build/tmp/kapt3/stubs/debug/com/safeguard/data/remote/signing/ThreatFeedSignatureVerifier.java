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
 * Verifier abstraction used by the threat-feed repository. Indirection gives us a unit-test
 * seam (the JVM-side test substitutes a fake `ThreatFeedSignatureVerifier`) without
 * spinning up Robolectric for every signed-envelope assertion.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u0006"}, d2 = {"Lcom/safeguard/data/remote/signing/ThreatFeedSignatureVerifier;", "", "verify", "Lcom/safeguard/data/remote/signing/VerifiedThreatFeed;", "rawJson", "", "data_debug"})
public abstract interface ThreatFeedSignatureVerifier {
    
    @org.jetbrains.annotations.NotNull
    public abstract com.safeguard.data.remote.signing.VerifiedThreatFeed verify(@org.jetbrains.annotations.NotNull
    java.lang.String rawJson);
}