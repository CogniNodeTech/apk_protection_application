package com.safeguard.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ScanLogEntry(
    val id: String,
    val apkName: String,
    val dateGroup: String,
    val timeLabel: String,
    val isThreat: Boolean,
    val verdict: Verdict,
    val threatScore: Int,
    val explanation: String,
    val evidenceBullets: List<String>
)

data class HistoryUiState(
    val items: List<ScanLogEntry> = emptyList(),
    val filter: ScanLogFilter = ScanLogFilter.ALL,
    val loading: Boolean = false
)

enum class ScanLogFilter { ALL, SAFE_APPS, Q_THREATS }

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = combine(
        _uiState,
        scanRepository.getScanHistory().map { list ->
            list.sortedByDescending { it.scanTimestamp }.take(200).map { toScanLogEntry(it) }
        }
    ) { local, items ->
        local.copy(items = items, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState(loading = true))

    init {
        _uiState.update { it.copy(loading = true) }
    }

    fun setFilter(filter: ScanLogFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    private fun toScanLogEntry(r: ScanResult): ScanLogEntry {
        val isThreat = r.finalVerdict == Verdict.MALICIOUS || r.finalVerdict == Verdict.SUSPICIOUS
        val dateGroup = getDateGroup(r.scanTimestamp)
        val timeLabel = formatTime(r.scanTimestamp)
        val explanation = if (isThreat) {
            "Threats detected. Review details before installing."
        } else {
            "No threats detected. Safe to install."
        }
        val evidenceBullets = r.aggregatedEvidence.take(5).ifEmpty {
            if (isThreat) listOf(
                "Hash match with known malware",
                "High risk permissions",
                "Unverified developer"
            ) else emptyList()
        }
        return ScanLogEntry(
            id = r.id,
            apkName = r.apkName,
            dateGroup = dateGroup,
            timeLabel = timeLabel,
            isThreat = isThreat,
            verdict = r.finalVerdict,
            threatScore = r.overallRiskScore,
            explanation = explanation,
            evidenceBullets = evidenceBullets
        )
    }

    private fun getDateGroup(timestamp: Long): String {
        val cal = Calendar.getInstance()
        val todayStart = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterdayStart = todayStart - 86400_000
        return when {
            timestamp >= todayStart -> "Today"
            timestamp >= yesterdayStart -> "Yesterday"
            else -> "Older"
        }
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
