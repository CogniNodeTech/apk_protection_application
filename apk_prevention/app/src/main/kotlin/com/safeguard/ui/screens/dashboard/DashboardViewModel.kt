package com.safeguard.ui.screens.dashboard

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.repository.QuarantineRepository
import com.safeguard.core.domain.repository.ScanRepository
import com.safeguard.core.domain.repository.ThreatFeedRepository
import com.safeguard.core.domain.repository.ThreatFeedStatus
import com.safeguard.core.domain.usecase.ScanAPKUseCase
import com.safeguard.data.local.preferences.SecurePreferencesManager
import com.safeguard.security.integrity.AntiDebugChecker
import com.safeguard.security.integrity.RuntimeEnvironmentChecker
import com.safeguard.notification.SafeGuardNotificationManager
import com.safeguard.worker.ScheduledScanWorker
import com.safeguard.core.domain.model.Action
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class RecentScanItem(
    val id: String,
    val apkName: String,
    val timeAgo: String,
    val isThreat: Boolean,
    val verdict: Verdict
)

data class DashboardUiState(
    val protectionStatus: String = "Loading",
    val securityScore: Int = 0,
    val recentScansSummary: String = "Loading",
    val quarantineCount: Int = 0,
    val monitoringEnabled: Boolean = false,
    val scanInProgress: Boolean = false,
    val scanError: String? = null,
    val environmentWarning: String? = null,
    val lastScanAgo: String? = null,
    val appsScannedToday: Int = 0,
    val threatsBlockedToday: Int = 0,
    val recentScans: List<RecentScanItem> = emptyList(),
    val scheduleEnabled: Boolean = false,
    val scheduleHour: Int = 9,
    val scheduleMinute: Int = 0,
    val scheduleFrequency: String = SecurePreferencesManager.FREQ_DAILY,
    val showInitialScanPrompt: Boolean = false,
    val navigateToScanProgress: Boolean = false,
    /**
     * Formatted threat-feed sync status. `null` until the first emission from
     * [ThreatFeedRepository.observeStatus] arrives — the dashboard should hide the tile
     * during that brief window rather than render with placeholder strings.
     */
    val threatFeedStatus: ThreatFeedStatusFormatter.Display? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanRepository: ScanRepository,
    private val quarantineRepository: QuarantineRepository,
    private val preferences: SecurePreferencesManager,
    private val scanAPKUseCase: ScanAPKUseCase,
    private val quarantineAPKUseCase: com.safeguard.core.domain.usecase.QuarantineAPKUseCase,
    private val deviceScanManager: com.safeguard.manager.DeviceScanManager,
    private val threatFeedRepository: ThreatFeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())

    private data class SchedulePrefs(
        val monitoring: Boolean,
        val scheduleEnabled: Boolean,
        val scheduleHour: Int,
        val scheduleMinute: Int,
        val scheduleFrequency: String
    )

    private val prefsState = combine(
        preferences.realTimeMonitoringEnabledFlow,
        preferences.scheduleScanEnabledFlow,
        preferences.scheduleHourFlow,
        preferences.scheduleMinuteFlow,
        preferences.scheduleFrequencyFlow
    ) { monitoring, schedEnabled, hour, minute, freq ->
        SchedulePrefs(
            monitoring = monitoring,
            scheduleEnabled = schedEnabled,
            scheduleHour = hour,
            scheduleMinute = minute,
            scheduleFrequency = freq
        )
    }

    private val derivedState: StateFlow<DashboardUiState> = combine(
        scanRepository.getScanHistory(),
        quarantineRepository.getQuarantineList(),
        prefsState,
        threatFeedRepository.observeStatus()
    ) { scans, quarantine, prefs, feedStatus ->
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val scansToday = scans.count { it.scanTimestamp >= todayStart }
        val blockedToday = scans.count {
            it.scanTimestamp >= todayStart &&
                (it.finalVerdict == Verdict.MALICIOUS || it.finalVerdict == Verdict.SUSPICIOUS)
        }
        val recent = scans.sortedByDescending { it.scanTimestamp }.take(10)
        val recentItems = recent.map { toRecentScanItem(it) }
        val lastScanAgo = recent.firstOrNull()?.let { formatTimeAgo(it.scanTimestamp) }

        DashboardUiState(
            protectionStatus = if (prefs.monitoring) "Active" else "Paused",
            securityScore = computeSecurityScore(scans, quarantine.size),
            recentScansSummary = "$scansToday scans today, $blockedToday threats blocked",
            quarantineCount = quarantine.size,
            monitoringEnabled = prefs.monitoring,
            scanInProgress = _uiState.value.scanInProgress,
            scanError = _uiState.value.scanError,
            environmentWarning = _uiState.value.environmentWarning,
            lastScanAgo = lastScanAgo,
            appsScannedToday = scansToday,
            threatsBlockedToday = blockedToday,
            recentScans = recentItems,
            scheduleEnabled = prefs.scheduleEnabled,
            scheduleHour = prefs.scheduleHour,
            scheduleMinute = prefs.scheduleMinute,
            scheduleFrequency = prefs.scheduleFrequency,
            showInitialScanPrompt = _uiState.value.showInitialScanPrompt,
            threatFeedStatus = formatFeedStatus(feedStatus)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    /**
     * Snapshot the wall clock once per emission so the UI's "Updated 4 hr ago" string
     * doesn't mutate while the dashboard is sitting still — the formatter is otherwise
     * pure, but consumers will keep observing the same `Display` for as long as the
     * underlying status row doesn't change. (Refresh on dashboard re-entry is handled by
     * [SharingStarted.WhileSubscribed]'s replay.)
     */
    private fun formatFeedStatus(status: ThreatFeedStatus): ThreatFeedStatusFormatter.Display =
        ThreatFeedStatusFormatter.format(status, System.currentTimeMillis())

    val uiState: StateFlow<DashboardUiState> = combine(_uiState, derivedState) { local, derived ->
        // `threatFeedStatus` lives on `derived` (it tracks repository emissions, not local
        // ephemeral UI state), so the copy intentionally forwards it from `derived`. Local
        // state only owns transient flags the user can flip without touching the network.
        derived.copy(
            scanInProgress = local.scanInProgress,
            scanError = local.scanError,
            environmentWarning = local.environmentWarning,
            showInitialScanPrompt = local.showInitialScanPrompt
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    init {
        val warnings = mutableListOf<String>()
        if (runCatching { RuntimeEnvironmentChecker.isRiskyEnvironment() }.getOrDefault(false)) {
            warnings.add("Device may be rooted or running in an emulator. Use with caution.")
        }
        if (runCatching { AntiDebugChecker.isDebuggerAttached() }.getOrDefault(false)) {
            warnings.add("Debugger is attached. Do not use for sensitive operations.")
        }
        if (warnings.isNotEmpty()) {
            _uiState.update { it.copy(environmentWarning = warnings.joinToString(" ")) }
            runCatching { Log.w(TAG, "Environment integrity warning triggered") }
        }
        
        if (!preferences.hasCompletedInitialScan) {
            _uiState.update { it.copy(showInitialScanPrompt = true) }
        }
        
        runCatching { Log.d(TAG, "Dashboard init — reactive dashboard") }
    }

    private fun toRecentScanItem(r: ScanResult): RecentScanItem {
        val isThreat = r.finalVerdict == Verdict.MALICIOUS || r.finalVerdict == Verdict.SUSPICIOUS
        return RecentScanItem(
            id = r.id,
            apkName = r.apkName,
            timeAgo = formatTimeAgo(r.scanTimestamp),
            isThreat = isThreat,
            verdict = r.finalVerdict
        )
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

    /** Runs full scan on the given APK file; on success navigates with scan ID, on error sets scanError. */
    fun runScan(apkFile: File, displayName: String? = null, onNavigateToScanResults: (String) -> Unit) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            runCatching { Log.i(TAG, "Manual scan started: ${apkFile.absolutePath}") }
            _uiState.update { it.copy(scanInProgress = true, scanError = null) }
            try {
                val result = scanAPKUseCase.execute(apkFile, displayName)
                val duration = System.currentTimeMillis() - startTime
                runCatching { Log.i(TAG, "Manual scan completed verdict=${result.finalVerdict} in ${duration}ms") }

                val shouldQuarantine = result.finalVerdict == Verdict.MALICIOUS ||
                    (result.finalVerdict == Verdict.SUSPICIOUS && (result.recommendedAction == Action.QUARANTINE || result.recommendedAction == Action.BLOCK))

                if (shouldQuarantine) {
                    try {
                        quarantineAPKUseCase.execute(apkFile.absolutePath, result)
                        SafeGuardNotificationManager.showScanResult(
                            context,
                            result.apkName,
                            result.finalVerdict,
                            result.overallRiskScore,
                            result.id
                        )
                    } catch (e: Exception) {
                        runCatching { Log.e(TAG, "Quarantine failed during manual scan", e) }
                    }
                }

                _uiState.update { it.copy(scanInProgress = false) }
                onNavigateToScanResults(result.id)
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                runCatching { Log.e(TAG, "Manual scan failed after ${duration}ms", e) }
                _uiState.update {
                    it.copy(
                        scanInProgress = false,
                        scanError = e.message ?: "Scan failed"
                    )
                }
            }
        }
    }

    fun clearScanError() {
        _uiState.update { it.copy(scanError = null) }
    }

    fun setScanError(message: String) {
        _uiState.update { it.copy(scanError = message) }
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        runCatching { Log.d(TAG, "Real-Time Shield: ${if (enabled) "ON" else "OFF"}") }
        preferences.realTimeMonitoringEnabled = enabled
    }

    fun saveSchedule(enabled: Boolean, hour: Int, minute: Int, frequency: String) {
        runCatching { Log.i(TAG, "Schedule updated") }
        preferences.scheduleScanEnabled = enabled
        preferences.scheduleHour = hour.coerceIn(0, 23)
        preferences.scheduleMinute = minute.coerceIn(0, 59)
        preferences.scheduleFrequency = if (frequency == SecurePreferencesManager.FREQ_WEEKLY) SecurePreferencesManager.FREQ_WEEKLY else SecurePreferencesManager.FREQ_DAILY
        _uiState.update {
            it.copy(
                scheduleEnabled = preferences.scheduleScanEnabled,
                scheduleHour = preferences.scheduleHour,
                scheduleMinute = preferences.scheduleMinute,
                scheduleFrequency = preferences.scheduleFrequency
            )
        }
        val wm = WorkManager.getInstance(context)
        wm.cancelUniqueWork(ScheduledScanWorker.UNIQUE_NAME)
        if (enabled) {
            val delayMillis = delayUntilNext(preferences.scheduleHour, preferences.scheduleMinute, preferences.scheduleFrequency)
            val request = OneTimeWorkRequestBuilder<ScheduledScanWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putInt(ScheduledScanWorker.KEY_HOUR, preferences.scheduleHour)
                        .putInt(ScheduledScanWorker.KEY_MINUTE, preferences.scheduleMinute)
                        .putString(ScheduledScanWorker.KEY_FREQUENCY, preferences.scheduleFrequency)
                        .build()
                )
                .addTag(ScheduledScanWorker.TAG)
                .build()
            wm.enqueueUniqueWork(ScheduledScanWorker.UNIQUE_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }

    private fun delayUntilNext(hour: Int, minute: Int, frequency: String): Long {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        var next = cal.timeInMillis
        if (next <= now) {
            if (frequency == SecurePreferencesManager.FREQ_WEEKLY) cal.add(Calendar.DAY_OF_YEAR, 7)
            else cal.add(Calendar.DAY_OF_YEAR, 1)
            next = cal.timeInMillis
        }
        return next - now
    }
    
    fun dismissInitialScanPrompt() {
        preferences.hasCompletedInitialScan = true
        _uiState.update { it.copy(showInitialScanPrompt = false) }
    }

    fun runInitialDeviceScan() {
        preferences.hasCompletedInitialScan = true
        _uiState.update { it.copy(showInitialScanPrompt = false, navigateToScanProgress = true) }
        deviceScanManager.startFullDeviceScan()
    }

    fun onScanProgressNavigated() {
        _uiState.update { it.copy(navigateToScanProgress = false) }
    }

    companion object {
        private const val TAG = "SafeGuard"
    }

    private fun computeSecurityScore(scans: List<ScanResult>, quarantineCount: Int): Int {
        val knownCount = scans.size
        val threatCount = scans.count { it.finalVerdict == Verdict.MALICIOUS || it.finalVerdict == Verdict.SUSPICIOUS }
        return when {
            knownCount == 0 -> 80
            threatCount > 0 || quarantineCount > 0 -> (100 - (threatCount * 5 + quarantineCount * 3)).coerceIn(50, 95)
            else -> 90
        }.coerceIn(50, 100)
    }
}
