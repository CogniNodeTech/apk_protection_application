package com.safeguard.ui.screens.quarantine;

import androidx.lifecycle.ViewModel;
import com.safeguard.core.domain.repository.QuarantineRecord;
import com.safeguard.core.domain.repository.QuarantineRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0014J\u000e\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0014J\u000e\u0010\u0016\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0014R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\r\u00a8\u0006\u0017"}, d2 = {"Lcom/safeguard/ui/screens/quarantine/QuarantineViewModel;", "Landroidx/lifecycle/ViewModel;", "quarantineRepository", "Lcom/safeguard/core/domain/repository/QuarantineRepository;", "(Lcom/safeguard/core/domain/repository/QuarantineRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/safeguard/ui/screens/quarantine/QuarantineUiState;", "quarantineList", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/safeguard/core/domain/repository/QuarantineRecord;", "getQuarantineList", "()Lkotlinx/coroutines/flow/StateFlow;", "uiState", "getUiState", "clearMessage", "", "delete", "id", "", "permanentlyDelete", "restore", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class QuarantineViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.safeguard.core.domain.repository.QuarantineRecord>> quarantineList = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.safeguard.ui.screens.quarantine.QuarantineUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.quarantine.QuarantineUiState> uiState = null;
    
    @javax.inject.Inject
    public QuarantineViewModel(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.safeguard.core.domain.repository.QuarantineRecord>> getQuarantineList() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.quarantine.QuarantineUiState> getUiState() {
        return null;
    }
    
    public final void restore(@org.jetbrains.annotations.NotNull
    java.lang.String id) {
    }
    
    public final void delete(@org.jetbrains.annotations.NotNull
    java.lang.String id) {
    }
    
    public final void permanentlyDelete(@org.jetbrains.annotations.NotNull
    java.lang.String id) {
    }
    
    public final void clearMessage() {
    }
}