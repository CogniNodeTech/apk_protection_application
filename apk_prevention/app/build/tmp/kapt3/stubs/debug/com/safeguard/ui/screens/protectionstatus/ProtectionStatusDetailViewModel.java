package com.safeguard.ui.screens.protectionstatus;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.repository.InstalledAppsRepository;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.repository.ScanRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 \u001b2\u00020\u0001:\u0001\u001bB\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0010\u0010\u0015\u001a\u00020\u000f2\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\u0010\u0010\u0018\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\u001aH\u0002R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\r0\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\r0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014\u00a8\u0006\u001c"}, d2 = {"Lcom/safeguard/ui/screens/protectionstatus/ProtectionStatusDetailViewModel;", "Landroidx/lifecycle/ViewModel;", "savedStateHandle", "Landroidx/lifecycle/SavedStateHandle;", "scanRepository", "Lcom/safeguard/core/domain/repository/ScanRepository;", "quarantineRepository", "Lcom/safeguard/core/domain/repository/QuarantineRepository;", "installedAppsRepository", "Lcom/safeguard/core/domain/repository/InstalledAppsRepository;", "(Landroidx/lifecycle/SavedStateHandle;Lcom/safeguard/core/domain/repository/ScanRepository;Lcom/safeguard/core/domain/repository/QuarantineRepository;Lcom/safeguard/core/domain/repository/InstalledAppsRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/safeguard/ui/screens/protectionstatus/ProtectionStatusDetailUiState;", "category", "", "derived", "Lkotlinx/coroutines/flow/StateFlow;", "uiState", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "formatTimeAgo", "timestamp", "", "verdictTag", "v", "Lcom/safeguard/core/domain/model/Verdict;", "Companion", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class ProtectionStatusDetailViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ScanRepository scanRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.InstalledAppsRepository installedAppsRepository = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String category = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.safeguard.ui.screens.protectionstatus.ProtectionStatusDetailUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.protectionstatus.ProtectionStatusDetailUiState> derived = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.protectionstatus.ProtectionStatusDetailUiState> uiState = null;
    private static final long STALE_SCAN_WINDOW_MS = 2592000000L;
    @org.jetbrains.annotations.NotNull
    private static final com.safeguard.ui.screens.protectionstatus.ProtectionStatusDetailViewModel.Companion Companion = null;
    
    @javax.inject.Inject
    public ProtectionStatusDetailViewModel(@org.jetbrains.annotations.NotNull
    androidx.lifecycle.SavedStateHandle savedStateHandle, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ScanRepository scanRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.InstalledAppsRepository installedAppsRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.protectionstatus.ProtectionStatusDetailUiState> getUiState() {
        return null;
    }
    
    private final java.lang.String formatTimeAgo(long timestamp) {
        return null;
    }
    
    private final java.lang.String verdictTag(com.safeguard.core.domain.model.Verdict v) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\b\u0082\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/safeguard/ui/screens/protectionstatus/ProtectionStatusDetailViewModel$Companion;", "", "()V", "STALE_SCAN_WINDOW_MS", "", "app_debug"})
    static final class Companion {
        
        private Companion() {
            super();
        }
    }
}