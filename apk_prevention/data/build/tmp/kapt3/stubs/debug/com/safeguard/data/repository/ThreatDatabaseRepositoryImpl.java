package com.safeguard.data.repository;

import com.safeguard.core.domain.repository.FuzzyMatch;
import com.safeguard.core.domain.repository.MalwareSignature;
import com.safeguard.core.domain.repository.ThreatDatabaseRepository;
import com.safeguard.core.domain.repository.TrustedApp;
import com.safeguard.core.util.FuzzyHasher;
import com.safeguard.data.local.database.dao.MalwareSignatureDao;
import com.safeguard.data.local.database.dao.TrustedAppDao;
import com.safeguard.data.local.database.entity.MalwareSignatureEntity;
import com.safeguard.data.local.database.entity.TrustedAppEntity;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0006\u0010\t\u001a\u00020\nH\u0096@\u00a2\u0006\u0002\u0010\u000bJ$\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\u0011H\u0096@\u00a2\u0006\u0002\u0010\u0012J\u0018\u0010\u0013\u001a\u0004\u0018\u00010\u00142\u0006\u0010\t\u001a\u00020\nH\u0096@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0014H\u0096@\u00a2\u0006\u0002\u0010\u0018R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/safeguard/data/repository/ThreatDatabaseRepositoryImpl;", "Lcom/safeguard/core/domain/repository/ThreatDatabaseRepository;", "malwareDao", "Lcom/safeguard/data/local/database/dao/MalwareSignatureDao;", "trustedAppDao", "Lcom/safeguard/data/local/database/dao/TrustedAppDao;", "(Lcom/safeguard/data/local/database/dao/MalwareSignatureDao;Lcom/safeguard/data/local/database/dao/TrustedAppDao;)V", "findMalwareBySha256", "Lcom/safeguard/core/domain/repository/MalwareSignature;", "sha256", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "findSimilarByFuzzyHash", "", "Lcom/safeguard/core/domain/repository/FuzzyMatch;", "fuzzyHash", "minSimilarity", "", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "findTrustedBySha256", "Lcom/safeguard/core/domain/repository/TrustedApp;", "isTrustedExpired", "", "trustedApp", "(Lcom/safeguard/core/domain/repository/TrustedApp;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class ThreatDatabaseRepositoryImpl implements com.safeguard.core.domain.repository.ThreatDatabaseRepository {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.database.dao.MalwareSignatureDao malwareDao = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.database.dao.TrustedAppDao trustedAppDao = null;
    
    @javax.inject.Inject
    public ThreatDatabaseRepositoryImpl(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.dao.MalwareSignatureDao malwareDao, @org.jetbrains.annotations.NotNull
    com.safeguard.data.local.database.dao.TrustedAppDao trustedAppDao) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object findMalwareBySha256(@org.jetbrains.annotations.NotNull
    java.lang.String sha256, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.safeguard.core.domain.repository.MalwareSignature> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object findTrustedBySha256(@org.jetbrains.annotations.NotNull
    java.lang.String sha256, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.safeguard.core.domain.repository.TrustedApp> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object findSimilarByFuzzyHash(@org.jetbrains.annotations.NotNull
    java.lang.String fuzzyHash, int minSimilarity, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.safeguard.core.domain.repository.FuzzyMatch>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object isTrustedExpired(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.TrustedApp trustedApp, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
}