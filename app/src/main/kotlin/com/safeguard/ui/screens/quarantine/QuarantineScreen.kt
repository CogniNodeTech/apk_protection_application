package com.safeguard.ui.screens.quarantine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.core.domain.repository.QuarantineRecord
import com.safeguard.ui.components.GlassCard
import com.safeguard.ui.components.WarningDialog
import androidx.compose.ui.graphics.Color
import com.safeguard.ui.theme.DangerRed
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.InstallAnywayPurple
import java.io.File

// Function to generate risk factors based on quarantine record data
private fun generateRiskFactors(record: QuarantineRecord): List<String> {
    val risks = mutableListOf<String>()
    
    // Add risk based on threat name
    if (!record.threatName.isNullOrBlank()) {
        risks.add("Detected: ${record.threatName}")
    }
    
    // Add risk based on risk score
    when {
        record.riskScore >= 80 -> risks.add("Critical risk level detected")
        record.riskScore >= 60 -> risks.add("High risk level detected")
        record.riskScore >= 40 -> risks.add("Medium risk level detected")
        else -> risks.add("Suspicious activity detected")
    }
    
    // Add file size risk if applicable
    if (record.sizeBytes > 50 * 1024 * 1024) { // > 50MB
        risks.add("Large file size - potential for hidden payloads")
    }
    
    // Ensure we have at least some risk factors
    if (risks.isEmpty()) {
        risks.add("Threat detected by security analysis")
        risks.add("Quarantined for user safety")
    }
    
    return risks
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuarantineScreen(
    onBack: (() -> Unit)? = null,
    viewModel: QuarantineViewModel = hiltViewModel()
) {
    val list by viewModel.quarantineList.collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var restoreId by remember { mutableStateOf<String?>(null) }
    var deleteId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbar.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    if (deleteId != null) {
        WarningDialog(
            title = "Delete Permanently",
            message = "This will permanently delete the APK from your device and block it from being reinstalled through this application. This action cannot be undone.",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirm = {
                viewModel.permanentlyDelete(deleteId!!)
                deleteId = null
                selectedId = null
            },
            onDismiss = { deleteId = null }
        )
    }

    if (restoreId != null) {
        WarningDialog(
            title = "Install Anyway",
            message = "Restore this file to your Downloads folder? It was quarantined as a potential threat.",
            confirmText = "Install Anyway",
            dismissText = "Cancel",
            onConfirm = {
                viewModel.restore(restoreId!!)
                restoreId = null
                selectedId = null
            },
            onDismiss = { restoreId = null }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Secure Vault", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
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
                    IconButton(onClick = { /* filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter pill: Malware
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.ScreenSidePadding, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("Malware") }
                )
            }

            if (list.isEmpty()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.ScreenSidePadding)
                ) {
                    Text(
                        "No files in Secure Vault. Blocked apps will appear here.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Dimensions.CardPadding)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Dimensions.ScreenSidePadding, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list) { record ->
                        VaultHorizontalCard(
                            record = record,
                            isExpanded = selectedId == record.id,
                            onClick = { selectedId = if (selectedId == record.id) null else record.id },
                            onDelete = { deleteId = record.id },
                            onInstallAnyway = { restoreId = record.id }
                        )
                    }
                }
            }
        }
    }
}

private fun QuarantineRecord.displayName(): String =
    apkName?.takeIf { it.isNotBlank() } ?: File(originalPath).name.ifBlank { "Quarantined file" }

@Composable
private fun VaultHorizontalCard(
    record: QuarantineRecord,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onInstallAnyway: () -> Unit
) {
    val fileName = record.displayName()
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            // Horizontal accent bar (full width)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFF7C4DFF))
            )
            Column(
                Modifier.padding(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                fileName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Threat score: ${record.riskScore}/100",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .background(DangerRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        record.threatName ?: "Malware",
                        style = MaterialTheme.typography.labelMedium,
                        color = DangerRed
                    )
                }
                if (isExpanded) {
                    Spacer(Modifier.height(12.dp))
                    val riskFactors = generateRiskFactors(record)
                    riskFactors.forEach { risk ->
                        Text(
                            "• $risk",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Delete", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = onInstallAnyway,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = InstallAnywayPurple),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text("Install Anyway", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

