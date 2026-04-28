package com.safeguard.ui.screens.history;

import androidx.lifecycle.ViewModel;
import com.safeguard.core.domain.model.ScanResult;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.core.domain.repository.ScanRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\u0010\u0010\u0010\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0014J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018H\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0019"}, d2 = {"Lcom/safeguard/ui/screens/history/HistoryViewModel;", "Landroidx/lifecycle/ViewModel;", "scanRepository", "Lcom/safeguard/core/domain/repository/ScanRepository;", "(Lcom/safeguard/core/domain/repository/ScanRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/safeguard/ui/screens/history/HistoryUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "formatTime", "", "timestamp", "", "getDateGroup", "setFilter", "", "filter", "Lcom/safeguard/ui/screens/history/ScanLogFilter;", "toScanLogEntry", "Lcom/safeguard/ui/screens/history/ScanLogEntry;", "r", "Lcom/safeguard/core/domain/model/ScanResult;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class HistoryViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.core.domain.repository.ScanRepository scanRepository = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.safeguard.ui.screens.history.HistoryUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.history.HistoryUiState> uiState = null;
    
    @javax.inject.Inject
    public HistoryViewModel(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ScanRepository scanRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.history.HistoryUiState> getUiState() {
        return null;
    }
    
    public final void setFilter(@org.jetbrains.annotations.NotNull
    com.safeguard.ui.screens.history.ScanLogFilter filter) {
    }
    
    private final com.safeguard.ui.screens.history.ScanLogEntry toScanLogEntry(com.safeguard.core.domain.model.ScanResult r) {
        return null;
    }
    
    private final java.lang.String getDateGroup(long timestamp) {
        return null;
    }
    
    private final java.lang.String formatTime(long timestamp) {
        return null;
    }
}