package com.safeguard.ui.screens.dashboard

import android.content.Context
import com.safeguard.core.domain.model.ScanResult
import com.safeguard.core.domain.model.Verdict
import com.safeguard.core.domain.repository.QuarantineRepository
import com.safeguard.core.domain.repository.ScanRepository
import com.safeguard.core.domain.repository.ThreatFeedRepository
import com.safeguard.core.domain.repository.ThreatFeedStatus
import com.safeguard.core.domain.usecase.ScanAPKUseCase
import com.safeguard.core.domain.usecase.QuarantineAPKUseCase
import com.safeguard.data.local.preferences.SecurePreferencesManager
import com.safeguard.manager.DeviceScanManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.After
import org.junit.Test
import java.io.File

class DashboardViewModelTest {

    private lateinit var viewModel: DashboardViewModel
    private val context: Context = mockk()
    private val scanRepository: ScanRepository = mockk()
    private val quarantineRepository: QuarantineRepository = mockk()
    private val preferences: SecurePreferencesManager = mockk()
    private val scanAPKUseCase: ScanAPKUseCase = mockk()
    private val quarantineAPKUseCase: QuarantineAPKUseCase = mockk()
    private val deviceScanManager: DeviceScanManager = mockk()
    private val threatFeedRepository: ThreatFeedRepository = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        // Mock preferences flows
        every { preferences.realTimeMonitoringEnabledFlow } returns MutableStateFlow(false)
        every { preferences.scheduleScanEnabledFlow } returns MutableStateFlow(false)
        every { preferences.scheduleHourFlow } returns MutableStateFlow(9)
        every { preferences.scheduleMinuteFlow } returns MutableStateFlow(0)
        every { preferences.scheduleFrequencyFlow } returns MutableStateFlow("daily")
        every { preferences.hasCompletedInitialScan } returns true
        every { preferences.realTimeMonitoringEnabled = any() } just runs

        // Mock repository flows
        every { scanRepository.getScanHistory() } returns MutableStateFlow(emptyList())
        every { quarantineRepository.getQuarantineList() } returns MutableStateFlow(emptyList())
        // Default threat-feed status — fresh-install "NEVER" so the dashboard render path
        // exercises the formatter without any of these tests caring about sync messaging.
        every { threatFeedRepository.observeStatus() } returns MutableStateFlow(ThreatFeedStatus())

        viewModel = DashboardViewModel(
            context,
            scanRepository,
            quarantineRepository,
            preferences,
            scanAPKUseCase,
            quarantineAPKUseCase,
            deviceScanManager,
            threatFeedRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `runScan should update state with loading then result`() = runTest {
        // Arrange
        val apkFile = File("/path/to/app.apk")
        val mockResult = createMockScanResult()
        coEvery { scanAPKUseCase.execute(any(), any()) } returns mockResult

        var navigatedId: String? = null
        val onNavigate: (String) -> Unit = { id -> navigatedId = id }

        // Act
        viewModel.runScan(apkFile, "TestApp", onNavigate)

        // Assert
        val state = viewModel.uiState.first()
        assertEquals(mockResult.id, navigatedId)
    }

    @Test
    fun `runScan should handle errors`() = runTest {
        // Arrange
        val apkFile = File("/path/to/app.apk")
        coEvery { scanAPKUseCase.execute(any(), any()) } throws Exception("Scan failed")

        val onNavigate: (String) -> Unit = {}

        // Act
        viewModel.runScan(apkFile, null, onNavigate)

        // Wait for coroutine
        Thread.sleep(100)

        // Assert
        val state = viewModel.uiState.first()
        assertTrue(state.scanError != null)
    }

    @Test
    fun `setMonitoringEnabled should update preferences`() = runTest {
        // Act
        viewModel.setMonitoringEnabled(true)

        // Assert
        verify { preferences.realTimeMonitoringEnabled = true }
    }

    @Test
    fun `clearScanError should clear error state`() = runTest {
        // Arrange
        viewModel.setScanError("Test error")

        // Act
        viewModel.clearScanError()

        // Assert
        val state = viewModel.uiState.first()
        assertEquals(null, state.scanError)
    }

    private fun createMockScanResult(): ScanResult {
        return ScanResult(
            id = "test-id",
            apkPath = "/path/to/app.apk",
            apkName = "TestApp",
            apkSizeBytes = 1024L * 1024L,
            scanTimestamp = System.currentTimeMillis(),
            finalVerdict = Verdict.SAFE,
            overallConfidence = 0.9f,
            overallRiskScore = 10,
            layerResults = emptyList(),
            aggregatedEvidence = emptyList(),
            recommendedAction = com.safeguard.core.domain.model.Action.ALLOW,
            userDecision = null,
            threatInfo = null,
            installerSource = null
        )
    }
}
