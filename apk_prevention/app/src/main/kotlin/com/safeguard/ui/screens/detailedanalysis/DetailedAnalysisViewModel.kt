package com.safeguard.ui.screens.detailedanalysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailedAnalysisUiState(
    val scanResult: ScanResult? = null,
    val loading: Boolean = false
)

@HiltViewModel
class DetailedAnalysisViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailedAnalysisUiState())
    val uiState: StateFlow<DetailedAnalysisUiState> = _uiState.asStateFlow()

    fun loadScan(scanId: String) {
        if (scanId.isBlank()) {
            _uiState.update { it.copy(scanResult = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val result = scanRepository.getScanResult(scanId)
            
            _uiState.update { it.copy(scanResult = result, loading = false) }
        }
    }
}
