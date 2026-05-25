package com.safeguard.ui.screens.scanprogress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.manager.DeviceScanManager
import com.safeguard.manager.ScanProgressState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DeviceScanProgressViewModel @Inject constructor(
    private val deviceScanManager: DeviceScanManager
) : ViewModel() {

    val scanState: StateFlow<ScanProgressState> = deviceScanManager.scanState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ScanProgressState()
        )

    fun startScan() {
        deviceScanManager.startFullDeviceScan()
    }

    fun stopScan() {
        deviceScanManager.stopScan()
    }

    fun closeScreen() {
        deviceScanManager.resetState()
    }
}
