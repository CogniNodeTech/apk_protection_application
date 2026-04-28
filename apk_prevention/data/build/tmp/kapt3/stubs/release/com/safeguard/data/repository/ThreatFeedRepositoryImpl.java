package com.safeguard.data.repository;

import android.util.Log;
import com.safeguard.core.domain.repository.ThreatFeedCursorStore;
import com.safeguard.core.domain.repository.ThreatFeedRepository;
import com.safeguard.core.domain.repository.ThreatFeedStatus;
import com.safeguard.core.domain.repository.ThreatFeedStatusStore;
import com.safeguard.core.domain.repository.ThreatFeedSyncResult;
import com.safeguard.data.local.database.dao.MalwareSignatureDao;
import com.safeguard.data.local.database.entity.MalwareSignatureEntity;
import com.safeguard.data.remote.api.ThreatIntelligenceApi;
import com.safeguard.data.remote.dto.ThreatFeedItemJson;
import com.safeguard.data.remote.dto.ThreatFeedResponseJson;
import com.safeguard.data.remote.signing.ThreatFeedSignatureVerifier;
import com.safeguard.data.remote.signing.VerifiedThreatFeed;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import kotlinx.coroutines.flow.Flow;
import retrofit2.HttpException;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Pulls fresh malware signatures from the SafeGuard threat-intel server (`GET /v1/threat-feed`)
 * and upserts them into the local Room DB so the on-device Layer 2 (HashValidator) actually
 * has a population to compare against. Without this sync, the device's malware table would
 * stay at whatever was bundled at install time and the new TLSH path from Phase 2.1 would
 * have nothing to score against.
 *
 * Cursor semantics (defensive against the failure modes I've seen in field deployments):
 * - The cursor is read once at the start of [sync] and only persisted if the **whole**
 *   chain of batches finishes cleanly. If batch 3 of 5 fails, we still keep batches 1-2
 *   in the DB (they're idempotent upserts) but don't move the cursor — the next periodic
 *   tick re-fetches the same window, which is cheap because abuse.ch returns rows
 *   newest-first and the dedupe is on the SHA-256 PK.
 * - The server's `next_cursor_ms` is the source of truth even for empty responses (so
 *   a quiet hour on abuse.ch advances us past the dead window, instead of looping over it
 *   forever once the server's `since` filter starts dropping all rows).
 */
@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u008a\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\f\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 52\u00020\u0001:\u00015B7\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0002\u0010\u000eJ(\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\u00112\u0006\u0010$\u001a\u00020%H\u0002J\u000e\u0010&\u001a\b\u0012\u0004\u0012\u00020%0\'H\u0016J\u001e\u0010(\u001a\u00020)2\u0006\u0010*\u001a\u00020\"2\u0006\u0010+\u001a\u00020\"H\u0096@\u00a2\u0006\u0002\u0010,J\f\u0010-\u001a\u00020.*\u00020\u0018H\u0002J\f\u0010/\u001a\u00020.*\u000200H\u0002J\f\u00101\u001a\u00020.*\u000200H\u0002J\u000e\u00102\u001a\u0004\u0018\u000103*\u000204H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u0010X\u0080\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R!\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00180\u00178BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001b\u0010\u001c\u001a\u0004\b\u0019\u0010\u001aR\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00066"}, d2 = {"Lcom/safeguard/data/repository/ThreatFeedRepositoryImpl;", "Lcom/safeguard/core/domain/repository/ThreatFeedRepository;", "api", "Lcom/safeguard/data/remote/api/ThreatIntelligenceApi;", "malwareDao", "Lcom/safeguard/data/local/database/dao/MalwareSignatureDao;", "cursorStore", "Lcom/safeguard/core/domain/repository/ThreatFeedCursorStore;", "statusStore", "Lcom/safeguard/core/domain/repository/ThreatFeedStatusStore;", "signatureVerifier", "Lcom/safeguard/data/remote/signing/ThreatFeedSignatureVerifier;", "moshi", "Lcom/squareup/moshi/Moshi;", "(Lcom/safeguard/data/remote/api/ThreatIntelligenceApi;Lcom/safeguard/data/local/database/dao/MalwareSignatureDao;Lcom/safeguard/core/domain/repository/ThreatFeedCursorStore;Lcom/safeguard/core/domain/repository/ThreatFeedStatusStore;Lcom/safeguard/data/remote/signing/ThreatFeedSignatureVerifier;Lcom/squareup/moshi/Moshi;)V", "nowMs", "Lkotlin/Function0;", "", "getNowMs$data_release", "()Lkotlin/jvm/functions/Function0;", "setNowMs$data_release", "(Lkotlin/jvm/functions/Function0;)V", "payloadAdapter", "Lcom/squareup/moshi/JsonAdapter;", "Lcom/safeguard/data/remote/dto/ThreatFeedResponseJson;", "getPayloadAdapter", "()Lcom/squareup/moshi/JsonAdapter;", "payloadAdapter$delegate", "Lkotlin/Lazy;", "failed", "Lcom/safeguard/core/domain/repository/ThreatFeedSyncResult$Failed;", "reason", "", "insertedSoFar", "", "attemptMs", "previous", "Lcom/safeguard/core/domain/repository/ThreatFeedStatus;", "observeStatus", "Lkotlinx/coroutines/flow/Flow;", "sync", "Lcom/safeguard/core/domain/repository/ThreatFeedSyncResult;", "batchLimit", "maxBatches", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "isEmpty", "", "isHexLower", "", "isHexUpper", "toEntityOrNull", "Lcom/safeguard/data/local/database/entity/MalwareSignatureEntity;", "Lcom/safeguard/data/remote/dto/ThreatFeedItemJson;", "Companion", "data_release"})
public final class ThreatFeedRepositoryImpl implements com.safeguard.core.domain.repository.ThreatFeedRepository {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.remote.api.ThreatIntelligenceApi api = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.database.dao.MalwareSignatureDao malwareDao = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ThreatFeedCursorStore cursorStore = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ThreatFeedStatusStore statusStore = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.remote.signing.ThreatFeedSignatureVerifier signatureVerifier = null;
    @org.jetbrains.annotations.NotNull
    private final com.squareup.moshi.Moshi moshi = null;
    
    /**
     * Adapter for the *inner* payload (post-verification). Moshi will reject unexpected
     * top-level fields silently by default, but we don't enable `failOnUnknown` here so a
     * server adding new optional fields (e.g. `provider_metadata`) doesn't brick the sync.
     */
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy payloadAdapter$delegate = null;
    
    /**
     * Wall-clock seam. Production reads `System.currentTimeMillis()`; tests override the
     * field reflectively (or via the test-only [withFixedNow] helper) so assertions on
     * persisted `lastAttemptMs` aren't flaky against the real clock.
     */
    @org.jetbrains.annotations.NotNull
    private kotlin.jvm.functions.Function0<java.lang.Long> nowMs;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "ThreatFeedRepository";
    private static final int SHA256_HEX_LEN = 64;
    private static final int SHA512_HEX_LEN = 128;
    private static final int TLSH_HEX_LEN = 70;
    private static final int MAX_BATCH_LIMIT = 1000;
    private static final int ABSOLUTE_BATCH_CAP = 20;
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.data.repository.ThreatFeedRepositoryImpl.Companion Companion = null;
    
    @javax.inject.Inject
    public ThreatFeedRepositoryImpl(@org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.api.ThreatIntelligenceApi api, @org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.dao.MalwareSignatureDao malwareDao, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ThreatFeedCursorStore cursorStore, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ThreatFeedStatusStore statusStore, @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.signing.ThreatFeedSignatureVerifier signatureVerifier, @org.jetbrains.annotations.NotNull
    com.squareup.moshi.Moshi moshi) {
        super();
    }
    
    /**
     * Adapter for the *inner* payload (post-verification). Moshi will reject unexpected
     * top-level fields silently by default, but we don't enable `failOnUnknown` here so a
     * server adding new optional fields (e.g. `provider_metadata`) doesn't brick the sync.
     */
    private final com.squareup.moshi.JsonAdapter<com.safeguard.data.remote.dto.ThreatFeedResponseJson> getPayloadAdapter() {
        return null;
    }
    
    /**
     * Wall-clock seam. Production reads `System.currentTimeMillis()`; tests override the
     * field reflectively (or via the test-only [withFixedNow] helper) so assertions on
     * persisted `lastAttemptMs` aren't flaky against the real clock.
     */
    @org.jetbrains.annotations.NotNull
    public final kotlin.jvm.functions.Function0<java.lang.Long> getNowMs$data_release() {
        return null;
    }
    
    /**
     * Wall-clock seam. Production reads `System.currentTimeMillis()`; tests override the
     * field reflectively (or via the test-only [withFixedNow] helper) so assertions on
     * persisted `lastAttemptMs` aren't flaky against the real clock.
     */
    public final void setNowMs$data_release(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<java.lang.Long> p0) {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object sync(int batchLimit, int maxBatches, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.safeguard.core.domain.repository.ThreatFeedSyncResult> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.flow.Flow<com.safeguard.core.domain.repository.ThreatFeedStatus> observeStatus() {
        return null;
    }
    
    /**
     * Persists a [ThreatFeedStatus.Outcome.FAILED] row that *preserves* the previous
     * [ThreatFeedStatus.lastSuccessMs] / [ThreatFeedStatus.lastInsertedCount]. The dashboard
     * uses those carried-forward values to render messaging like "Last refreshed 2 days ago
     * — last attempt failed (network)" instead of regressing the success counter to zero
     * the moment a single sync errors.
     */
    private final com.safeguard.core.domain.repository.ThreatFeedSyncResult.Failed failed(java.lang.String reason, int insertedSoFar, long attemptMs, com.safeguard.core.domain.repository.ThreatFeedStatus previous) {
        return null;
    }
    
    /**
     * Server-side validation already enforces the SHA-256 length / TLSH length contracts,
     * but we re-check defensively so a misbehaving proxy or cached MITM response can't
     * inject malformed rows that later poison TLSH similarity scoring.
     */
    private final com.safeguard.data.local.database.entity.MalwareSignatureEntity toEntityOrNull(com.safeguard.data.remote.dto.ThreatFeedItemJson $this$toEntityOrNull) {
        return null;
    }
    
    /**
     * Custom hex predicates instead of `Char.isDigit() || it in 'a'..'f'` to side-step a
     * name collision with Kotlin stdlib's `Char.isHexDigit()` (added in 2.0). The collision
     * triggers `'isHexDigit' is a member and an extension at the same time` on this module's
     * Kotlin compiler.
     */
    private final boolean isHexLower(char $this$isHexLower) {
        return false;
    }
    
    private final boolean isHexUpper(char $this$isHexUpper) {
        return false;
    }
    
    @kotlin.Suppress(names = {"unused"})
    private final boolean isEmpty(com.safeguard.data.remote.dto.ThreatFeedResponseJson $this$isEmpty) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/safeguard/data/repository/ThreatFeedRepositoryImpl$Companion;", "", "()V", "ABSOLUTE_BATCH_CAP", "", "MAX_BATCH_LIMIT", "SHA256_HEX_LEN", "SHA512_HEX_LEN", "TAG", "", "TLSH_HEX_LEN", "data_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}