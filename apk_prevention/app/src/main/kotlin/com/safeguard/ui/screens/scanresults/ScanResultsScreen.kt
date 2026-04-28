package com.safeguard.ui.screens.scanresults

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.ui.components.LayerBreakdownCard
import com.safeguard.ui.components.RiskGauge
import com.safeguard.ui.components.SecureActionButton
import com.safeguard.ui.components.SecureButtonStyle
import com.safeguard.ui.theme.DangerRed
import com.safeguard.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultsScreen(
    scanId: String,
    onBack: () -> Unit,
    onNavigateToDetailedAnalysis: (() -> Unit)? = null,
    viewModel: ScanResultsViewModel = hiltViewModel()
) {
    LaunchedEffect(scanId) { viewModel.loadScan(scanId) }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.quarantineSuccess) {
        if (state.quarantineSuccess) {
            viewModel.clearQuarantineSuccess()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Result", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* kebab menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(Dimensions.ScreenPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.loading) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(24.dp))
            }
            if (!state.loading && state.scanResult == null) {
                Text(
                    "No scan result. Run a scan from the Dashboard.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                SecureActionButton(text = "Back", onClick = onBack, style = SecureButtonStyle.Primary)
                return@Scaffold
            }
            val result = state.scanResult ?: return@Scaffold

            val riskLabel = when {
                result.overallRiskScore >= 70 -> "High Risk"
                result.overallRiskScore >= 40 -> "Caution"
                else -> "Secure"
            }
            RiskGauge(
                score = result.overallRiskScore,
                modifier = Modifier.fillMaxWidth(),
                size = 140.dp,
                strokeWidth = 10.dp,
                scoreLabel = riskLabel
            )
            Spacer(Modifier.height(8.dp))
            Text(
                result.apkName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            if (result.installerSource != null) {
                Text(
                    "Installed via: ${result.installerSource}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }
            Spacer(Modifier.height(Dimensions.SectionSpacing))

            Text(
                "How we decided",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Dimensions.TightSpacing))
            result.layerResults.forEach { layer ->
                LayerBreakdownCard(
                    layerName = layer.layerName,
                    verdict = layer.verdict,
                    evidenceSummary = layer.evidence.firstOrNull() ?: "",
                    onClick = onNavigateToDetailedAnalysis
                )
                Spacer(Modifier.height(Dimensions.TightSpacing))
            }
            Spacer(Modifier.height(Dimensions.SectionSpacing))

            if (state.isDeleted) {
                Text(
                    "This APK has been permanently deleted and blocked from re-installation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DangerRed,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go Back")
                }
            } else {
                val isHighRiskVerdict = result.finalVerdict != com.safeguard.core.domain.model.Verdict.SAFE
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.quarantine(result) },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Quarantine")
                    }
                    OutlinedButton(
                        onClick = { viewModel.deleteScan(result) },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                    ) {
                        Text("Delete")
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onBack,
                    enabled = !isHighRiskVerdict,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isHighRiskVerdict) "Allow Anyway (Disabled for risky apps)" else "Allow Anyway")
                }
                if (isHighRiskVerdict) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Unsafe override is blocked for suspicious or malicious results. Use quarantine or delete.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DangerRed
                    )
                }
            }
        }
    }
}
