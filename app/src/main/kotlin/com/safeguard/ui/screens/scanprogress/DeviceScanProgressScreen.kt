package com.safeguard.ui.screens.scanprogress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.core.domain.model.Verdict
import com.safeguard.ui.components.GlassCard
import com.safeguard.ui.theme.DangerRed
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScanProgressScreen(
    onBack: () -> Unit,
    viewModel: DeviceScanProgressViewModel = hiltViewModel()
) {
    val state by viewModel.scanState.collectAsState()

    // Auto-start scan if not already scanning/finished when this screen is opened
    // Usually the Dashboard calls startScan() before navigating.
    // We'll trust the Dashboard to start it to avoid race conditions.

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Device Scan",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.ScreenSidePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Progress Header
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimensions.CardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val progressRatio = if (state.totalApps > 0) state.scannedApps.toFloat() / state.totalApps else 0f
                    
                    if (state.isFinished && state.totalApps > 0 && state.scannedApps >= state.totalApps) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = NeonGreen, modifier = Modifier.size(64.dp))
                        Text("Scan Complete!", style = MaterialTheme.typography.headlineSmall, color = NeonGreen)
                    } else if (state.isFinished) {
                        Icon(Icons.Default.Warning, contentDescription = "Cancelled", tint = DangerRed, modifier = Modifier.size(64.dp))
                        Text("Scan Cancelled", style = MaterialTheme.typography.headlineSmall, color = DangerRed)
                    } else {
                        CircularProgressIndicator(
                            progress = progressRatio,
                            modifier = Modifier.size(80.dp),
                            color = NeonGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 6.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "${(progressRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Scanned ${state.scannedApps} of ${state.totalApps} files",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (state.isScanning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Currently scanning: ${state.currentAppLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Time Elapsed", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val m = state.elapsedTimeSec / 60
                        val s = state.elapsedTimeSec % 60
                        Text("${m}m ${s}s", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Threats Found", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.threatsFound}", style = MaterialTheme.typography.titleLarge, color = if (state.threatsFound > 0) DangerRed else NeonGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            if (state.isScanning) {
                Button(
                    onClick = { viewModel.stopScan() },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Stop Scan", color = androidx.compose.ui.graphics.Color.White)
                }
            } else if (state.isFinished) {
                Button(
                    onClick = { 
                        viewModel.closeScreen() 
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Findings List
            if (state.findings.isNotEmpty()) {
                Text(
                    "Threats Discovered (${state.findings.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.findings) { finding ->
                        GlassCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(finding.appName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        if (finding.quarantined) "Auto-quarantined" else "Threat detected",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DangerRed
                                    )
                                }
                                Icon(Icons.Default.Warning, contentDescription = "Threat", tint = DangerRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
