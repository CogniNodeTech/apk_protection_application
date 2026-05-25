package com.safeguard.ui.screens.protectionstatus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
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
import com.safeguard.ui.theme.DangerRed
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectionStatusDetailScreen(
    onBack: () -> Unit,
    onScanClick: (String) -> Unit,
    viewModel: ProtectionStatusDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.screenTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
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
        } else if (state.items.isEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.ScreenSidePadding)
                ) {
                    Text(
                        when (state.screenTitle) {
                            "Known APKs" -> "No scanned APKs yet. Run a scan from the Dashboard."
                            "Missed Updates" -> "No missed updates."
                            "Unknown Sources" -> "No apps from unknown sources."
                            "Quarantined APKs" -> "No quarantined files."
                            else -> "No items."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Dimensions.CardPadding)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = Dimensions.ScreenSidePadding,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.TightSpacing)
            ) {
                items(state.items, key = { it.id }) { item ->
                    DetailItemCard(
                        item = item,
                        onClick = {
                            item.scanId?.let { onScanClick(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItemCard(
    item: ProtectionDetailItem,
    onClick: () -> Unit
) {
    val clickable = item.scanId != null
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (clickable) Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        Row(
            Modifier.padding(Dimensions.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.tag != null) {
                val tagColor = when (item.tag.uppercase()) {
                    "CLEAN" -> NeonGreen
                    "MALICIOUS", "QUARANTINED", "SUSPICIOUS" -> DangerRed
                    else -> MaterialTheme.colorScheme.primary
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = tagColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            item.tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = tagColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (clickable) {
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
    }
}
