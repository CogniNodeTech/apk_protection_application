package com.safeguard.ui.screens.scanprogress;

import androidx.lifecycle.ViewModel;
import com.safeguard.manager.DeviceScanManager;
import com.safeguard.manager.ScanProgressState;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\n\u001a\u00020\u000bJ\u0006\u0010\f\u001a\u00020\u000bJ\u0006\u0010\r\u001a\u00020\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006\u000e"}, d2 = {"Lcom/safeguard/ui/screens/scanprogress/DeviceScanProgressViewModel;", "Landroidx/lifecycle/ViewModel;", "deviceScanManager", "Lcom/safeguard/manager/DeviceScanManager;", "(Lcom/safeguard/manager/DeviceScanManager;)V", "scanState", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/safeguard/manager/ScanProgressState;", "getScanState", "()Lkotlinx/coroutines/flow/StateFlow;", "closeScreen", "", "startScan", "stopScan", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class DeviceScanProgressViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.manager.DeviceScanManager deviceScanManager = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.safeguard.manager.ScanProgressState> scanState = null;
    
    @javax.inject.Inject
    public DeviceScanProgressViewModel(@org.jetbrains.annotations.NotNull
    com.safeguard.manager.DeviceScanManager deviceScanManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.safeguard.manager.ScanProgressState> getScanState() {
        return null;
    }
    
    public final void startScan() {
    }
    
    public final void stopScan() {
    }
    
    public final void closeScreen() {
    }
}