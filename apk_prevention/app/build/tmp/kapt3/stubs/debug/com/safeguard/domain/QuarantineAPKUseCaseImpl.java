package com.safeguard.domain;

import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.repository.QuarantineRecord;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0096@\u00a2\u0006\u0002\u0010\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/safeguard/domain/QuarantineAPKUseCaseImpl;", "Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;", "repository", "Lcom/safeguard/core/domain/repository/QuarantineRepository;", "(Lcom/safeguard/core/domain/repository/QuarantineRepository;)V", "execute", "Lcom/safeguard/core/domain/repository/QuarantineRecord;", "apkPath", "", "result", "Lcom/safeguard/core/domain/model/ScanResult;", "(Ljava/lang/String;Lcom/safeguard/core/domain/model/ScanResult;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class QuarantineAPKUseCaseImpl implements com.safeguard.core.domain.usecase.QuarantineAPKUseCase {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.QuarantineRepository repository = null;
    
    @javax.inject.Inject
    public QuarantineAPKUseCaseImpl(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.QuarantineRepository repository) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object execute(@org.jetbrains.annotations.NotNull
    java.lang.String apkPath, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.ScanResult result, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.safeguard.core.domain.repository.QuarantineRecord> $completion) {
        return null;
    }
}