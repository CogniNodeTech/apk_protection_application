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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/safeguard/ui/screens/history/ScanLogFilter;", "", "(Ljava/lang/String;I)V", "ALL", "SAFE_APPS", "Q_THREATS", "app_debug"})
public enum ScanLogFilter {
    /*public static final*/ ALL /* = new ALL() */,
    /*public static final*/ SAFE_APPS /* = new SAFE_APPS() */,
    /*public static final*/ Q_THREATS /* = new Q_THREATS() */;
    
    ScanLogFilter() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.safeguard.ui.screens.history.ScanLogFilter> getEntries() {
        return null;
    }
}