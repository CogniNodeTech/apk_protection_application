package com.safeguard.ui.screens.scanresults;

import androidx.lifecycle.ViewModel;
import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.repository.ScanRepository;
import com.safeguard.core.domain.repository.QuarantineRepository;
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0010\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B/\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\bJ\u000b\u0010\u000e\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0005H\u00c6\u0003J3\u0010\u0012\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u0013\u001a\u00020\u00052\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001R\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\tR\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0019"}, d2 = {"Lcom/safeguard/ui/screens/scanresults/ScanResultsUiState;", "", "scanResult", "Lcom/safeguard/core/domain/model/ScanResult;", "loading", "", "quarantineSuccess", "isDeleted", "(Lcom/safeguard/core/domain/model/ScanResult;ZZZ)V", "()Z", "getLoading", "getQuarantineSuccess", "getScanResult", "()Lcom/safeguard/core/domain/model/ScanResult;", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
public final class ScanResultsUiState {
    @org.jetbrains.annotations.Nullable
    private final com.safeguard.core.domain.model.ScanResult scanResult = null;
    private final boolean loading = false;
    private final boolean quarantineSuccess = false;
    private final boolean isDeleted = false;
    
    public ScanResultsUiState(@org.jetbrains.annotations.Nullable
    com.safeguard.core.domain.model.ScanResult scanResult, boolean loading, boolean quarantineSuccess, boolean isDeleted) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.safeguard.core.domain.model.ScanResult getScanResult() {
        return null;
    }
    
    public final boolean getLoading() {
        return false;
    }
    
    public final boolean getQuarantineSuccess() {
        return false;
    }
    
    public final boolean isDeleted() {
        return false;
    }
    
    public ScanResultsUiState() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.safeguard.core.domain.model.ScanResult component1() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final boolean component3() {
        return false;
    }
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.ui.screens.scanresults.ScanResultsUiState copy(@org.jetbrains.annotations.Nullable
    com.safeguard.core.domain.model.ScanResult scanResult, boolean loading, boolean quarantineSuccess, boolean isDeleted) {
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