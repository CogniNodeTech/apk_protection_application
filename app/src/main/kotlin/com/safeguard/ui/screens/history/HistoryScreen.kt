package com.safeguard.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.core.domain.model.Verdict
import com.safeguard.ui.components.GlassCard
import com.safeguard.ui.theme.DangerRed
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.SafeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onScanClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // "Details"-only view: no extra filtering tabs/pills.
    val filtered = state.items
    val grouped = filtered.groupBy { it.dateGroup }
    val order = listOf("Today", "Yesterday", "Older").filter { grouped.containsKey(it) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Details", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = { /* reactive; no manual refresh needed */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh scan logs", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }
    ) { padding ->
        if (state.loading) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(Modifier.padding(32.dp))
            }
        } else {
            Column(Modifier.padding(padding)) {
                if (filtered.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimensions.ScreenSidePadding, vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No previous scans yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Scan an APK to see details here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Dimensions.ScreenSidePadding, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        order.forEach { groupKey ->
                            val entries = grouped[groupKey] ?: return@forEach
                            item(key = "header_$groupKey") {
                                Text(
                                    groupKey,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }
                            items(entries, key = { it.id }) { entry ->
                                ScanLogEntryCard(
                                    entry = entry,
                                    onDetails = { onScanClick(entry.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanLogEntryCard(
    entry: ScanLogEntry,
    onDetails: () -> Unit
) {
    val verdictLabel = when (entry.verdict) {
        Verdict.SAFE -> "CLEAN"
        Verdict.MALICIOUS -> "QUARANTINED"
        Verdict.SUSPICIOUS -> "SUSPICIOUS"
        Verdict.UNKNOWN -> "UNKNOWN"
    }
    val isThreat = entry.isThreat
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Dimensions.CardPadding)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    entry.timeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (isThreat) Icons.Default.Warning else Icons.Default.Android,
                    contentDescription = null,
                    tint = if (isThreat) DangerRed else SafeGreen,
                    modifier = Modifier.size(32.dp)
                )
                Column(Modifier.padding(start = 12.dp)) {
                    Text(
                        entry.apkName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isThreat) DangerRed.copy(alpha = 0.2f) else SafeGreen.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                verdictLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isThreat) DangerRed else SafeGreen
                            )
                        }
                        Text(
                            "Threat score: ${entry.threatScore}/100",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                entry.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (entry.evidenceBullets.isNotEmpty()) {
                Column(Modifier.padding(top = 8.dp)) {
                    for (bullet in entry.evidenceBullets) {
                        Row(
                            Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "• ",
                                style = MaterialTheme.typography.bodySmall,
                                color = DangerRed
                            )
                            Text(
                                bullet,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDetails,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Details")
                }
            }
        }
    }
}
