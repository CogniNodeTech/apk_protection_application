package com.safeguard.ui.screens.scanresults;

import androidx.lifecycle.ViewModel;
import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0014J\u000e\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u0017J\u000e\u0010\u0018\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0014R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u0019"}, d2 = {"Lcom/safeguard/ui/screens/scanresults/ScanResultsViewModel;", "Landroidx/lifecycle/ViewModel;", "scanRepository", "Lcom/safeguard/core/domain/repository/ScanRepository;", "quarantineUseCase", "Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;", "quarantineRepository", "Lcom/safeguard/core/domain/repository/QuarantineRepository;", "(Lcom/safeguard/core/domain/repository/ScanRepository;Lcom/safeguard/core/domain/usecase/QuarantineAPKUseCase;Lcom/safeguard/core/domain/repository/QuarantineRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/safeguard/ui/screens/scanresults/ScanResultsUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearQuarantineSuccess", "", "deleteScan", "result", "Lcom/safeguard/core/domain/model/ScanResult;", "loadScan", "scanId", "", "quarantine", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class ScanResultsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ScanRepository scanRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.usecase.QuarantineAPKUseCase quarantineUseCase = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.safeguard.ui.screens.scanresults.ScanResultsUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.scanresults.ScanResultsUiState> uiState = null;
    
    @javax.inject.Inject
    public ScanResultsViewModel(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ScanRepository scanRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.usecase.QuarantineAPKUseCase quarantineUseCase, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.scanresults.ScanResultsUiState> getUiState() {
        return null;
    }
    
    public final void loadScan(@org.jetbrains.annotations.NotNull
    java.lang.String scanId) {
    }
    
    public final void quarantine(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.ScanResult result) {
    }
    
    public final void clearQuarantineSuccess() {
    }
    
    public final void deleteScan(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.model.ScanResult result) {
    }
}