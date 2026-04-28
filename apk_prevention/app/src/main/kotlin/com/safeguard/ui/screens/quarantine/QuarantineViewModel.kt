package com.safeguard.ui.screens.quarantine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.core.domain.repository.QuarantineRecord
import com.safeguard.core.domain.repository.QuarantineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuarantineUiState(
    val message: String? = null
)

@HiltViewModel
class QuarantineViewModel @Inject constructor(
    private val quarantineRepository: QuarantineRepository
) : ViewModel() {

    val quarantineList: StateFlow<List<QuarantineRecord>> = quarantineRepository.getQuarantineList()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow(QuarantineUiState())
    val uiState: StateFlow<QuarantineUiState> = _uiState.asStateFlow()

    fun restore(id: String) {
        viewModelScope.launch {
            val path = quarantineRepository.restoreFromQuarantine(id)
            _uiState.update { it.copy(message = if (path != null) "Restored to $path" else "Restore failed") }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            quarantineRepository.deleteFromQuarantine(id)
            _uiState.update { it.copy(message = "Removed from quarantine") }
        }
    }

    fun permanentlyDelete(id: String) {
        viewModelScope.launch {
            quarantineRepository.permanentlyDelete(id)
            _uiState.update { it.copy(message = "APK permanently deleted and blocked from reinstallation") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
