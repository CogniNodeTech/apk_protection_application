package com.safeguard.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.safeguard.core.domain.repository.QuarantineRepository
import com.safeguard.core.domain.repository.ScanFeedbackRepository
import com.safeguard.core.domain.repository.ScanRepository
import com.safeguard.data.local.preferences.SecurePreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: SecurePreferencesManager,
    private val scanRepository: ScanRepository,
    private val quarantineRepository: QuarantineRepository,
    private val scanFeedbackRepository: ScanFeedbackRepository,
    private val moshi: Moshi
) : ViewModel() {

    private val exportListAdapter by lazy {
        moshi.adapter<List<ScanExportRow>>(
            Types.newParameterizedType(List::class.java, ScanExportRow::class.java)
        )
    }

    init {
        // Seed the queue-count badge so the settings tile shows the right number on
        // first composition without a manual "pull-to-refresh".
        refreshFeedbackCount()
    }

    fun deleteAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            scanRepository.clearScanHistory()
            quarantineRepository.clearAllQuarantine()
            preferences.hasCompletedInitialScan = false
            onComplete()
        }
    }

    fun exportScanHistoryJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val scans = scanRepository.getAllScanResultsForExport()
            val rows = scans.map { r ->
                ScanExportRow(
                    id = r.id,
                    apkName = r.apkName,
                    apkPath = r.apkPath,
                    scanTimestamp = r.scanTimestamp,
                    finalVerdict = r.finalVerdict.name,
                    overallRiskScore = r.overallRiskScore,
                    recommendedAction = r.recommendedAction.name
                )
            }
            onResult(exportListAdapter.toJson(rows))
        }
    }

    private val _monitoringEnabled = MutableStateFlow(preferences.realTimeMonitoringEnabled)
    val monitoringEnabled: StateFlow<Boolean> = _monitoringEnabled.asStateFlow()

    private val _deepScanEnabled = MutableStateFlow(preferences.deepScanModeEnabled)
    val deepScanEnabled: StateFlow<Boolean> = _deepScanEnabled.asStateFlow()

    private val _notificationLevel = MutableStateFlow(preferences.notificationLevel)
    val notificationLevel: StateFlow<String> = _notificationLevel.asStateFlow()

    private val _cloudVerificationEnabled = MutableStateFlow(preferences.cloudVerificationEnabled)
    val cloudVerificationEnabled: StateFlow<Boolean> = _cloudVerificationEnabled.asStateFlow()

    private val _scanTelemetryEnabled = MutableStateFlow(preferences.scanTelemetryEnabled)
    val scanTelemetryEnabled: StateFlow<Boolean> = _scanTelemetryEnabled.asStateFlow()

    private val _privacySharingOptOut = MutableStateFlow(preferences.privacySharingOptOut)
    val privacySharingOptOut: StateFlow<Boolean> = _privacySharingOptOut.asStateFlow()

    /**
     * Phase 3.2 — opt-in scan feedback toggle. Default false; user must explicitly opt in
     * for any feedback rows to be persisted or uploaded.
     */
    private val _scanFeedbackEnabled = MutableStateFlow(preferences.scanFeedbackEnabled)
    val scanFeedbackEnabled: StateFlow<Boolean> = _scanFeedbackEnabled.asStateFlow()

    /**
     * Live count of queued (un-uploaded) feedback rows. Refreshed after toggle/purge so
     * the UI can show "12 events queued" alongside the toggle without polling.
     */
    private val _queuedFeedbackCount = MutableStateFlow(0)
    val queuedFeedbackCount: StateFlow<Int> = _queuedFeedbackCount.asStateFlow()

    fun setMonitoring(enabled: Boolean) {
        preferences.realTimeMonitoringEnabled = enabled
        _monitoringEnabled.update { enabled }
    }

    fun setDeepScan(enabled: Boolean) {
        preferences.deepScanModeEnabled = enabled
        _deepScanEnabled.update { enabled }
    }

    fun setNotificationLevel(level: String) {
        preferences.notificationLevel = level
        _notificationLevel.update { level }
    }

    fun setCloudVerification(enabled: Boolean) {
        preferences.cloudVerificationEnabled = enabled
        _cloudVerificationEnabled.update { enabled }
    }

    fun setScanTelemetry(enabled: Boolean) {
        preferences.scanTelemetryEnabled = enabled
        _scanTelemetryEnabled.update { enabled }
    }

    fun setPrivacySharingOptOut(enabled: Boolean) {
        preferences.privacySharingOptOut = enabled
        _privacySharingOptOut.update { enabled }
    }

    /**
     * Flip the Phase 3.2 feedback opt-in. Toggling off does NOT purge already-queued
     * rows on its own — that's a separate explicit action ([clearFeedbackQueue]) so
     * users can re-enable later without losing what they previously consented to share.
     */
    fun setScanFeedback(enabled: Boolean) {
        preferences.scanFeedbackEnabled = enabled
        _scanFeedbackEnabled.update { enabled }
        refreshFeedbackCount()
    }

    /**
     * Wipe every queued feedback row. Returns immediately; the actual count update is
     * pushed via [queuedFeedbackCount] once the DB delete completes.
     */
    fun clearFeedbackQueue(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            scanFeedbackRepository.clearAll()
            refreshFeedbackCount()
            onComplete()
        }
    }

    /** Pull the current queue count into [queuedFeedbackCount]. Cheap; safe to call from UI. */
    fun refreshFeedbackCount() {
        viewModelScope.launch {
            val count = runCatching { scanFeedbackRepository.queuedCount() }.getOrDefault(0)
            _queuedFeedbackCount.update { count }
        }
    }

    private data class ScanExportRow(
        val id: String,
        val apkName: String,
        val apkPath: String,
        val scanTimestamp: Long,
        val finalVerdict: String,
        val overallRiskScore: Int,
        val recommendedAction: String
    )
}
