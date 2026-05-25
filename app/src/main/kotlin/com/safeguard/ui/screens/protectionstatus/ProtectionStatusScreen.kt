package com.safeguard.ui.screens.protectionstatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.safeguard.ui.components.GlassCard
import com.safeguard.ui.components.RiskGauge
import com.safeguard.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectionStatusScreen(
    onBack: (() -> Unit)? = null,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: ProtectionStatusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Protection Status", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.ScreenSidePadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RiskGauge(
                    score = state.securityScore,
                    modifier = Modifier,
                    size = 180.dp,
                    strokeWidth = 12.dp,
                    scoreLabel = "/100",
                    useNeonStyle = true
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Protection Status",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(Dimensions.SectionSpacing))

            StatusCard(
                icon = Icons.Default.Folder,
                title = "Known APKs",
                subtitle = "Apps scanned and verified",
                count = state.knownApkCount,
                onClick = { onNavigateToDetail("known") }
            )
            Spacer(Modifier.height(Dimensions.TightSpacing))
            StatusCard(
                icon = Icons.Default.Schedule,
                title = "Missed Updates",
                subtitle = "Apps with available updates",
                count = state.missedUpdatesCount,
                onClick = { onNavigateToDetail("missed") }
            )
            Spacer(Modifier.height(Dimensions.TightSpacing))
            StatusCard(
                icon = Icons.Default.Check,
                title = "Unknown Sources",
                subtitle = "Apps from unknown sources",
                count = state.unknownSourcesCount,
                onClick = { onNavigateToDetail("unknown") }
            )
            Spacer(Modifier.height(Dimensions.TightSpacing))
            StatusCard(
                icon = Icons.Default.Check,
                title = "Quarantined APKs",
                subtitle = "Isolated threat files",
                count = state.quarantinedCount,
                onClick = { onNavigateToDetail("quarantined") }
            )
            Spacer(Modifier.height(Dimensions.SectionSpacing))
        }
    }
}

@Composable
private fun StatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    count: Int,
    onClick: () -> Unit = {}
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.ScreenSidePadding, vertical = 4.dp)
            .then(Modifier.clickable(onClick = onClick))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "$count",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
