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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u000e\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B)\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u00a2\u0006\u0002\u0010\tJ\u000f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\bH\u00c6\u0003J-\u0010\u0013\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bH\u00c6\u0001J\u0013\u0010\u0014\u001a\u00020\b2\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0016\u001a\u00020\u0017H\u00d6\u0001J\t\u0010\u0018\u001a\u00020\u0019H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001a"}, d2 = {"Lcom/safeguard/ui/screens/history/HistoryUiState;", "", "items", "", "Lcom/safeguard/ui/screens/history/ScanLogEntry;", "filter", "Lcom/safeguard/ui/screens/history/ScanLogFilter;", "loading", "", "(Ljava/util/List;Lcom/safeguard/ui/screens/history/ScanLogFilter;Z)V", "getFilter", "()Lcom/safeguard/ui/screens/history/ScanLogFilter;", "getItems", "()Ljava/util/List;", "getLoading", "()Z", "component1", "component2", "component3", "copy", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
public final class HistoryUiState {
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.safeguard.ui.screens.history.ScanLogEntry> items = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.ui.screens.history.ScanLogFilter filter = null;
    private final boolean loading = false;
    
    public HistoryUiState(@org.jetbrains.annotations.NotNull
    java.util.List<com.safeguard.ui.screens.history.ScanLogEntry> items, @org.jetbrains.annotations.NotNull
    com.safeguard.ui.screens.history.ScanLogFilter filter, boolean loading) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.safeguard.ui.screens.history.ScanLogEntry> getItems() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.ui.screens.history.ScanLogFilter getFilter() {
        return null;
    }
    
    public final boolean getLoading() {
        return false;
    }
    
    public HistoryUiState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.safeguard.ui.screens.history.ScanLogEntry> component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.ui.screens.history.ScanLogFilter component2() {
        return null;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.ui.screens.history.HistoryUiState copy(@org.jetbrains.annotations.NotNull
    java.util.List<com.safeguard.ui.screens.history.ScanLogEntry> items, @org.jetbrains.annotations.NotNull
    com.safeguard.ui.screens.history.ScanLogFilter filter, boolean loading) {
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