package com.safeguard.data.repository;

import android.util.Log;
import com.safeguard.core.domain.feedback.FeedbackPrivacyGate;
import com.safeguard.core.domain.feedback.FeedbackUploadResult;
import com.safeguard.core.domain.feedback.ScanFeedbackEvent;
import com.safeguard.core.domain.repository.ScanFeedbackRepository;
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao;
import com.safeguard.data.local.database.entity.ScanFeedbackEventEntity;
import com.safeguard.data.remote.api.ThreatIntelligenceApi;
import com.safeguard.data.remote.dto.FeedbackEventJson;
import com.safeguard.data.remote.dto.FeedbackUploadRequest;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import retrofit2.HttpException;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Production binding for [ScanFeedbackRepository] (Phase 3.2).
 *
 * The privacy gate lives **here** rather than at every call site. Three things must all
 * be true (see [FeedbackPrivacyGate.isFeedbackAllowed]) before a single byte leaves the
 * device:
 * 1. The explicit feedback opt-in (default OFF).
 * 2. The telemetry master toggle (default ON, but if a user flips it off they expect
 *    *all* outbound analytics to stop — feedback is a strict subset of telemetry).
 * 3. The US-state sharing opt-out kill switch (default OFF; when ON, no analytics
 *    leave the device regardless of other toggles).
 *
 * If any predicate fails, [enqueue] silently returns `false` and [drainOnce] returns
 * [FeedbackUploadResult.Skipped]. We don't *delete* the queue when the toggle flips —
 * that's a separate user action exposed via [clearAll] so the user can confirm "yes,
 * forget what I already shared". Re-enabling the toggle resumes uploads from where we
 * left off.
 *
 * Serialisation note: layer scores and triggered rule names are stored as JSON strings
 * inside the entity rather than as related Room tables. The cardinality is tiny
 * (≤ 7 layer scores, typically 0–3 rules per event) so the indirection isn't worth the
 * migration cost — and storing them inline makes [clearAll] a single `DELETE` instead of
 * needing to walk foreign-key cascades.
 */
@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000~\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u0007\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 12\u00020\u0001:\u00011B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u000e\u0010\u001f\u001a\u00020 H\u0096@\u00a2\u0006\u0002\u0010!J\u0016\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020%H\u0096@\u00a2\u0006\u0002\u0010&J\u0016\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020*H\u0096@\u00a2\u0006\u0002\u0010+J\u000e\u0010,\u001a\u00020%H\u0096@\u00a2\u0006\u0002\u0010!J\f\u0010-\u001a\u00020.*\u00020*H\u0002J\u000e\u0010/\u001a\u0004\u0018\u000100*\u00020.H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R-\u0010\u000b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u000f0\r0\f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0012\u0010\u0013\u001a\u0004\b\u0010\u0010\u0011R \u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00160\u0015X\u0080\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001aR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\'\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\u001c0\f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001e\u0010\u0013\u001a\u0004\b\u001d\u0010\u0011\u00a8\u00062"}, d2 = {"Lcom/safeguard/data/repository/ScanFeedbackRepositoryImpl;", "Lcom/safeguard/core/domain/repository/ScanFeedbackRepository;", "dao", "Lcom/safeguard/data/local/database/dao/ScanFeedbackEventDao;", "api", "Lcom/safeguard/data/remote/api/ThreatIntelligenceApi;", "privacyGate", "Lcom/safeguard/core/domain/feedback/FeedbackPrivacyGate;", "moshi", "Lcom/squareup/moshi/Moshi;", "(Lcom/safeguard/data/local/database/dao/ScanFeedbackEventDao;Lcom/safeguard/data/remote/api/ThreatIntelligenceApi;Lcom/safeguard/core/domain/feedback/FeedbackPrivacyGate;Lcom/squareup/moshi/Moshi;)V", "layerScoresAdapter", "Lcom/squareup/moshi/JsonAdapter;", "", "", "", "getLayerScoresAdapter", "()Lcom/squareup/moshi/JsonAdapter;", "layerScoresAdapter$delegate", "Lkotlin/Lazy;", "nowMs", "Lkotlin/Function0;", "", "getNowMs$data_debug", "()Lkotlin/jvm/functions/Function0;", "setNowMs$data_debug", "(Lkotlin/jvm/functions/Function0;)V", "rulesAdapter", "", "getRulesAdapter", "rulesAdapter$delegate", "clearAll", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "drainOnce", "Lcom/safeguard/core/domain/feedback/FeedbackUploadResult;", "batchLimit", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "enqueue", "", "event", "Lcom/safeguard/core/domain/feedback/ScanFeedbackEvent;", "(Lcom/safeguard/core/domain/feedback/ScanFeedbackEvent;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "queuedCount", "toEntity", "Lcom/safeguard/data/local/database/entity/ScanFeedbackEventEntity;", "toJsonOrNull", "Lcom/safeguard/data/remote/dto/FeedbackEventJson;", "Companion", "data_debug"})
public final class ScanFeedbackRepositoryImpl implements com.safeguard.core.domain.repository.ScanFeedbackRepository {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.database.dao.ScanFeedbackEventDao dao = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.remote.api.ThreatIntelligenceApi api = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.feedback.FeedbackPrivacyGate privacyGate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy layerScoresAdapter$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy rulesAdapter$delegate = null;
    
    /**
     * Wall-clock seam — production reads `System.currentTimeMillis()`, tests override the
     * field reflectively for deterministic `uploaded_at_ms` assertions.
     */
    @org.jetbrains.annotations.NotNull
    private kotlin.jvm.functions.Function0<java.lang.Long> nowMs;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "ScanFeedbackRepo";
    private static final int MAX_BATCH = 200;
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.data.repository.ScanFeedbackRepositoryImpl.Companion Companion = null;
    
    @javax.inject.Inject
    public ScanFeedbackRepositoryImpl(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.dao.ScanFeedbackEventDao dao, @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.api.ThreatIntelligenceApi api, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.feedback.FeedbackPrivacyGate privacyGate, @org.jetbrains.annotations.NotNull
    com.squareup.moshi.Moshi moshi) {
        super();
    }
    
    private final com.squareup.moshi.JsonAdapter<java.util.Map<java.lang.String, java.lang.Float>> getLayerScoresAdapter() {
        return null;
    }
    
    private final com.squareup.moshi.JsonAdapter<java.util.List<java.lang.String>> getRulesAdapter() {
        return null;
    }
    
    /**
     * Wall-clock seam — production reads `System.currentTimeMillis()`, tests override the
     * field reflectively for deterministic `uploaded_at_ms` assertions.
     */
    @org.jetbrains.annotations.NotNull
    public final kotlin.jvm.functions.Function0<java.lang.Long> getNowMs$data_debug() {
        return null;
    }
    
    /**
     * Wall-clock seam — production reads `System.currentTimeMillis()`, tests override the
     * field reflectively for deterministic `uploaded_at_ms` assertions.
     */
    public final void setNowMs$data_debug(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<java.lang.Long> p0) {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object enqueue(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.feedback.ScanFeedbackEvent event, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object drainOnce(int batchLimit, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.safeguard.core.domain.feedback.FeedbackUploadResult> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object queuedCount(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object clearAll(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final com.safeguard.data.local.database.entity.ScanFeedbackEventEntity toEntity(com.safeguard.core.domain.feedback.ScanFeedbackEvent $this$toEntity) {
        return null;
    }
    
    private final com.safeguard.data.remote.dto.FeedbackEventJson toJsonOrNull(com.safeguard.data.local.database.entity.ScanFeedbackEventEntity $this$toJsonOrNull) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/safeguard/data/repository/ScanFeedbackRepositoryImpl$Companion;", "", "()V", "MAX_BATCH", "", "TAG", "", "data_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}