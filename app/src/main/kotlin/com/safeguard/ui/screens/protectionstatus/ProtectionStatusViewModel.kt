package com.safeguard.ui.screens.protectionstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.repository.InstalledAppsRepository
import com.safeguard.core.domain.repository.QuarantineRepository
import com.safeguard.core.domain.repository.ScanRepository
import com.safeguard.data.local.preferences.SecurePreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ProtectionStatusUiState(
    val securityScore: Int = 0,
    val knownApkCount: Int = 0,
    val missedUpdatesCount: Int = 0,
    val unknownSourcesCount: Int = 0,
    val quarantinedCount: Int = 0
)

@HiltViewModel
class ProtectionStatusViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val quarantineRepository: QuarantineRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val preferences: SecurePreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProtectionStatusUiState())

    val uiState: StateFlow<ProtectionStatusUiState> = combine(
        scanRepository.getScanHistory().map { list -> list.sortedByDescending { it.scanTimestamp }.take(500) },
        quarantineRepository.getQuarantineList(),
        installedAppsRepository.observeUnknownSourceApps()
    ) { scans, quarantine, unknownApps ->
        val knownCount = scans.size
        val staleThreshold = System.currentTimeMillis() - STALE_SCAN_WINDOW_MS
        val missedUpdatesCount = scans.count { it.scanTimestamp < staleThreshold }
        val quarantineCount = quarantine.size
        val threatCount = scans.count { it.finalVerdict == Verdict.MALICIOUS || it.finalVerdict == Verdict.SUSPICIOUS }
        val score = when {
            knownCount == 0 -> 80
            threatCount > 0 || quarantineCount > 0 -> (100 - (threatCount * 5 + quarantineCount * 3)).coerceIn(50, 95)
            else -> 90
        }.coerceIn(50, 100)

        ProtectionStatusUiState(
            securityScore = score,
            knownApkCount = knownCount,
            missedUpdatesCount = missedUpdatesCount,
            unknownSourcesCount = unknownApps.size,
            quarantinedCount = quarantineCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProtectionStatusUiState())

    init {
        // reactive; no manual load needed
    }

    private companion object {
        private const val STALE_SCAN_WINDOW_MS: Long = 30L * 24 * 60 * 60 * 1000 // 30 days
    }
}
