package com.safeguard.ui.screens.protectionstatus;

import androidx.lifecycle.ViewModel;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.repository.InstalledAppsRepository;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u0000 \u00122\u00020\u0001:\u0001\u0012B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nR\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006\u0013"}, d2 = {"Lcom/safeguard/ui/screens/protectionstatus/ProtectionStatusViewModel;", "Landroidx/lifecycle/ViewModel;", "scanRepository", "Lcom/safeguard/core/domain/repository/ScanRepository;", "quarantineRepository", "Lcom/safeguard/core/domain/repository/QuarantineRepository;", "installedAppsRepository", "Lcom/safeguard/core/domain/repository/InstalledAppsRepository;", "preferences", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "(Lcom/safeguard/core/domain/repository/ScanRepository;Lcom/safeguard/core/domain/repository/QuarantineRepository;Lcom/safeguard/core/domain/repository/InstalledAppsRepository;Lcom/safeguard/data/local/preferences/SecurePreferencesManager;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/safeguard/ui/screens/protectionstatus/ProtectionStatusUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "Companion", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class ProtectionStatusViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ScanRepository scanRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.InstalledAppsRepository installedAppsRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.preferences.SecurePreferencesManager preferences = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.safeguard.ui.screens.protectionstatus.ProtectionStatusUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.protectionstatus.ProtectionStatusUiState> uiState = null;
    private static final long STALE_SCAN_WINDOW_MS = 2592000000L;
    @org.jetbrains.annotations.NotNull
    private static final com.safeguard.ui.screens.protectionstatus.ProtectionStatusViewModel.Companion Companion = null;
    
    @javax.inject.Inject
    public ProtectionStatusViewModel(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ScanRepository scanRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.QuarantineRepository quarantineRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.InstalledAppsRepository installedAppsRepository, @org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager preferences) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.protectionstatus.ProtectionStatusUiState> getUiState() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\b\u0082\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/safeguard/ui/screens/protectionstatus/ProtectionStatusViewModel$Companion;", "", "()V", "STALE_SCAN_WINDOW_MS", "", "app_debug"})
    static final class Companion {
        
        private Companion() {
            super();
        }
    }
}