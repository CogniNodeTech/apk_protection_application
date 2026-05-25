package com.safeguard.ui.screens.protectionstatus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.repository.InstalledAppsRepository
import com.safeguard.core.domain.repository.QuarantineRepository
import com.safeguard.core.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Display row for protection detail list (same design across all categories). */
data class ProtectionDetailItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String?,
    val scanId: String? = null
)

data class ProtectionStatusDetailUiState(
    val screenTitle: String = "",
    val items: List<ProtectionDetailItem> = emptyList(),
    val loading: Boolean = true
)

@HiltViewModel
class ProtectionStatusDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scanRepository: ScanRepository,
    private val quarantineRepository: QuarantineRepository,
    private val installedAppsRepository: InstalledAppsRepository
) : ViewModel() {

    private val category: String? = savedStateHandle.get<String>("category")

    private val _uiState = MutableStateFlow(ProtectionStatusDetailUiState())

    private val derived: StateFlow<ProtectionStatusDetailUiState> = when (category) {
        "known" -> scanRepository.getScanHistory().map { list ->
            val scans = list.sortedByDescending { it.scanTimestamp }.take(500)
            ProtectionStatusDetailUiState(
                screenTitle = "Known APKs",
                items = scans.map { r ->
                    ProtectionDetailItem(
                        id = r.id,
                        title = r.apkName,
                        subtitle = formatTimeAgo(r.scanTimestamp),
                        tag = verdictTag(r.finalVerdict),
                        scanId = r.id
                    )
                },
                loading = false
            )
        }

        "missed" -> installedAppsRepository.observeMissedUpdates().map { apps ->
            ProtectionStatusDetailUiState(
                screenTitle = "Missed Updates",
                items = apps.map { a ->
                    ProtectionDetailItem(
                        id = a.packageName,
                        title = a.appName,
                        subtitle = "Last updated: ${formatTimeAgo(a.lastUpdateTime)}",
                        tag = "Outdated",
                        scanId = null
                    )
                },
                loading = false
            )
        }

        "unknown" -> installedAppsRepository.observeUnknownSourceApps().map { apps ->
            ProtectionStatusDetailUiState(
                screenTitle = "Unknown Sources",
                items = apps.map { a ->
                    ProtectionDetailItem(
                        id = a.packageName,
                        title = a.appName,
                        subtitle = a.versionName ?: a.packageName,
                        tag = a.installerPackage ?: "Unknown installer",
                        scanId = null
                    )
                },
                loading = false
            )
        }

        "quarantined" -> quarantineRepository.getQuarantineList().map { list ->
            ProtectionStatusDetailUiState(
                screenTitle = "Quarantined APKs",
                items = list.map { r ->
                    ProtectionDetailItem(
                        id = r.id,
                        title = r.apkName?.takeIf { it.isNotBlank() }
                            ?: r.originalPath.substringAfterLast('/'),
                        subtitle = formatTimeAgo(r.quarantinedAt),
                        tag = "Quarantined",
                        scanId = null
                    )
                },
                loading = false
            )
        }

        else -> MutableStateFlow(
            ProtectionStatusDetailUiState(
                screenTitle = "Protection Status",
                items = emptyList(),
                loading = false
            )
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), ProtectionStatusDetailUiState(loading = true))

    val uiState: StateFlow<ProtectionStatusDetailUiState> = derived

    init {
        // reactive; no manual load needed
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} mins ago"
            diff < 86400_000 -> "${diff / 3600_000} hr ago"
            else -> "${diff / 86400_000} days ago"
        }
    }

    private fun verdictTag(v: Verdict): String = when (v) {
        Verdict.SAFE -> "CLEAN"
        Verdict.SUSPICIOUS -> "SUSPICIOUS"
        Verdict.MALICIOUS -> "MALICIOUS"
        Verdict.UNKNOWN -> "UNKNOWN"
    }

    private companion object {
        private const val STALE_SCAN_WINDOW_MS: Long = 30L * 24 * 60 * 60 * 1000 // 30 days
    }
}
