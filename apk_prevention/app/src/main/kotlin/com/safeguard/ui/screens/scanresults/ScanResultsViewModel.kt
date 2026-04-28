package com.safeguard.ui.screens.scanresults

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.repository.ScanRepository
import com.safeguard.core.domain.repository.QuarantineRepository
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanResultsUiState(
    val scanResult: ScanResult? = null,
    val loading: Boolean = false,
    val quarantineSuccess: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class ScanResultsViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val quarantineUseCase: QuarantineAPKUseCase,
    private val quarantineRepository: QuarantineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanResultsUiState())
    val uiState: StateFlow<ScanResultsUiState> = _uiState.asStateFlow()

    fun loadScan(scanId: String) {
        if (scanId.isBlank()) {
            _uiState.update { it.copy(scanResult = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val result = scanRepository.getScanResult(scanId)
            val isDeleted = if (result != null) {
                quarantineRepository.isApkBlocked(result.apkName)
            } else false
            _uiState.update { it.copy(scanResult = result, isDeleted = isDeleted, loading = false) }
        }
    }

    fun quarantine(result: ScanResult) {
        viewModelScope.launch {
            try {
                quarantineUseCase.execute(result.apkPath, result)
                _uiState.update { it.copy(quarantineSuccess = true) }
            } catch (_: Exception) {
                // keep on screen; could set error message
            }
        }
    }

    fun clearQuarantineSuccess() {
        _uiState.update { it.copy(quarantineSuccess = false) }
    }

    fun deleteScan(result: ScanResult) {
        viewModelScope.launch {
            try {
                quarantineRepository.deleteAndBlockApk(result.apkPath, result)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
