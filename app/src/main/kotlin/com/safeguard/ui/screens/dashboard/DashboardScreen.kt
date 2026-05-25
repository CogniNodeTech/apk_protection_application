package com.safeguard.ui.screens.dashboard

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.core.domain.model.Verdict
import com.safeguard.data.local.preferences.SecurePreferencesManager
import com.safeguard.ui.components.DailySecurityTipCard
import com.safeguard.ui.components.GlassCard
import com.safeguard.ui.components.HexagonThreatLevel
import com.safeguard.ui.education.CyberSecurityAwarenessFacts
import com.safeguard.ui.theme.DangerRed
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.NeonGreen
import com.safeguard.ui.theme.SafeGreen
import java.io.File
import android.Manifest
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.safeguard.util.StorageAccessHelper

private const val APK_MIME = "application/vnd.android.package-archive"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToScanResults: (String) -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToDeviceScan: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val securityTipIndex = remember { CyberSecurityAwarenessFacts.indexForToday() }
    val securityTipText = remember(securityTipIndex) { CyberSecurityAwarenessFacts.facts[securityTipIndex] }

    val readStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.runInitialDeviceScan()
            scope.launch { snackbarHostState.showSnackbar("Initial scan started in background") }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Storage permission is needed to find APK files on the device") }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val displayName = try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex)?.takeIf { it.isNotBlank() } else null
            }
        } catch (_: Exception) { null }
        val cacheFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.apk")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                cacheFile.outputStream().use { output -> input.copyTo(output) }
            }
            if (cacheFile.exists()) {
                viewModel.runScan(cacheFile, displayName, onNavigateToScanResults)
            } else {
                viewModel.setScanError("Could not read selected file")
            }
        } catch (e: Exception) {
            viewModel.setScanError(e.message ?: "Could not read selected file")
        }
    }

    LaunchedEffect(uiState.scanError) {
        uiState.scanError?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearScanError()
        }
    }

    LaunchedEffect(uiState.navigateToScanProgress) {
        if (uiState.navigateToScanProgress) {
            viewModel.onScanProgressNavigated()
            onNavigateToDeviceScan()
        }
    }

    var showScheduleDialog by remember { mutableStateOf(false) }
    if (showScheduleDialog) {
        ScheduleScanDialog(
            scheduleEnabled = uiState.scheduleEnabled,
            scheduleHour = uiState.scheduleHour,
            scheduleMinute = uiState.scheduleMinute,
            scheduleFrequency = uiState.scheduleFrequency,
            onDismiss = { showScheduleDialog = false },
            onSave = { enabled, hour, minute, frequency ->
                viewModel.saveSchedule(enabled, hour, minute, frequency)
                showScheduleDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (enabled) "Scheduled scan set for ${formatTime(hour, minute)} (${if (frequency == SecurePreferencesManager.FREQ_WEEKLY) "weekly" else "daily"})"
                        else "Scheduled scan turned off"
                    )
                }
            }
        )
    }

    if (uiState.showInitialScanPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissInitialScanPrompt() },
            title = { Text("Initial Device Scan") },
            text = {
                Text(
                    "Scans all non-system installed apps and performs a deep search for .apk files in every folder on shared storage " +
                        "(including nested and hidden directories). Malicious files are quarantined into SafeGuard and removed from their original location.\n\n" +
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            "On Android 11+, SafeGuard needs All files access so this deep scan can see APKs outside standard folders. " +
                                "If a settings screen opens, enable it for SafeGuard, return here, and tap Scan Now again."
                        } else {
                            "Storage permission is required to search for APK files on this device."
                        }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                            if (!Environment.isExternalStorageManager()) {
                                try {
                                    context.startActivity(StorageAccessHelper.createManageAllFilesIntent(context))
                                } catch (_: Exception) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Open Settings → Apps → SafeGuard → allow All files access")
                                    }
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar("After granting All files access, tap Scan Now again to start.")
                                }
                            } else {
                                viewModel.runInitialDeviceScan()
                                scope.launch { snackbarHostState.showSnackbar("Initial scan started in background") }
                            }
                        }
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                            viewModel.runInitialDeviceScan()
                            scope.launch { snackbarHostState.showSnackbar("Initial scan started in background") }
                        }
                        else -> readStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }) {
                    Text("Scan Now", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissInitialScanPrompt() }) {
                    Text("Maybe Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            DashboardBackground(Modifier.fillMaxSize())
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                val threatLevel = when {
                    uiState.securityScore >= 70 -> "LOW - SECURE"
                    uiState.securityScore >= 40 -> "MEDIUM - CAUTION"
                    else -> "HIGH - RISK"
                }
                HexagonThreatLevel(
                    threatLevelText = threatLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.TightSpacing)
                        .heightIn(min = 340.dp)
                )

                DailySecurityTipCard(
                    factText = securityTipText,
                    factNumber = securityTipIndex + 1
                )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.ScreenSidePadding, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionCircle(icon = Icons.Default.Search, label = "Scan Now", onClick = { if (!uiState.scanInProgress) filePickerLauncher.launch(APK_MIME) })
                QuickActionCircle(icon = Icons.Default.Schedule, label = "Schedule Scan", onClick = { showScheduleDialog = true })
                QuickActionCircle(icon = Icons.Default.Lock, label = "Secure Vault", onClick = onNavigateToVault)
                QuickActionCircle(icon = Icons.Default.Assessment, label = "Reports", onClick = onNavigateToReports)
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.ScreenSidePadding, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.CardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Real-Time Shield",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = uiState.monitoringEnabled,
                        onCheckedChange = { viewModel.setMonitoringEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            uiState.threatFeedStatus?.let { feedDisplay ->
                ThreatFeedStatusCard(display = feedDisplay)
            }

            Text(
                "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = Dimensions.ScreenSidePadding).padding(top = 8.dp, bottom = 8.dp)
            )
            uiState.recentScans.take(5).forEach { item ->
                RecentActivityCard(
                    apkName = item.apkName,
                    verdict = item.verdict,
                    isThreat = item.isThreat,
                    codeSuffix = "-BL-${item.id.takeLast(3).uppercase()}",
                    onClick = { onNavigateToScanResults(item.id) }
                )
            }
            if (uiState.recentScans.isEmpty()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.ScreenSidePadding, vertical = 4.dp)
                ) {
                    Text(
                        "No recent scans. Use Scan Now to scan an APK.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Dimensions.CardPadding)
                    )
                }
            }

            Spacer(Modifier.height(Dimensions.SectionSpacing))
            }
        }
    }
}

@Composable
private fun DashboardBackground(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    if (!isDark) return
    val accent = Color(0xFF00E676).copy(alpha = 0.045f)
    val accentSecondary = Color(0xFF7C4DFF).copy(alpha = 0.03f)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val step = 72f
        for (x in 0..(w / step).toInt()) {
            val px = x * step
            drawLine(accent, Offset(px, 0f), Offset(px, h), strokeWidth = 1f)
        }
        for (y in 0..(h / step).toInt()) {
            val py = y * step
            drawLine(accent, Offset(0f, py), Offset(w, py), strokeWidth = 1f)
        }
        drawLine(accentSecondary, Offset(0f, h * 0.35f), Offset(w * 0.5f, h * 0.2f), strokeWidth = 1.5f)
        drawLine(accentSecondary, Offset(w * 0.5f, h * 0.2f), Offset(w, h * 0.4f), strokeWidth = 1.5f)
    }
}

@Composable
private fun QuickActionCircle(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GlassCard(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RecentActivityCard(
    apkName: String,
    verdict: Verdict,
    isThreat: Boolean,
    codeSuffix: String,
    onClick: () -> Unit
) {
    val verdictLabel = when (verdict) {
        Verdict.SAFE -> "Clean"
        Verdict.MALICIOUS -> "QUARANTINED"
        Verdict.SUSPICIOUS -> "SUSPICIOUS"
        Verdict.UNKNOWN -> "UNKNOWN"
    }
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.ScreenSidePadding, vertical = 4.dp)
            .clickable(onClick = onClick)
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
                imageVector = if (isThreat) Icons.Default.Warning else Icons.Default.Android,
                contentDescription = null,
                tint = if (isThreat) DangerRed else NeonGreen,
                modifier = Modifier.size(40.dp)
            )
            Column(Modifier.padding(start = 12.dp).fillMaxWidth()) {
                Text(
                    apkName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isThreat) DangerRed.copy(alpha = 0.2f) else NeonGreen.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            verdictLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isThreat) DangerRed else NeonGreen
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isThreat) DangerRed else NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        codeSuffix,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Threat database freshness tile. Three colour variants drive at-a-glance readability:
 *  - OK (green) — sync ran cleanly inside the freshness window;
 *  - WARNING (amber) — recent attempt failed, or last success is past the staleness threshold;
 *  - ERROR (red) — never synced or failed without any prior success to fall back on.
 *
 * The card is intentionally non-interactive: the user can't trigger an ad-hoc sync from
 * here because a manual sync button would invite hammering the threat-intel server (and
 * WorkManager already retries on its own backoff). Surfacing the *state* is the goal, not
 * exposing a control surface.
 */
@Composable
private fun ThreatFeedStatusCard(display: ThreatFeedStatusFormatter.Display) {
    val (accentColor, _) = when (display.severity) {
        ThreatFeedStatusFormatter.Severity.OK -> NeonGreen to Icons.Default.Storage
        ThreatFeedStatusFormatter.Severity.WARNING -> Color(0xFFFFB300) to Icons.Default.Warning
        ThreatFeedStatusFormatter.Severity.ERROR -> DangerRed to Icons.Default.Warning
    }
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.ScreenSidePadding, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Dimensions.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Threat database",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    display.headline,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                display.detail?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor
                    )
                }
                display.insertedSummary?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val h = hour.coerceIn(0, 23)
    val m = minute.coerceIn(0, 59)
    val am = h < 12
    val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h
    return String.format("%d:%02d %s", hour12, m, if (am) "AM" else "PM")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScanDialog(
    scheduleEnabled: Boolean,
    scheduleHour: Int,
    scheduleMinute: Int,
    scheduleFrequency: String,
    onDismiss: () -> Unit,
    onSave: (enabled: Boolean, hour: Int, minute: Int, frequency: String) -> Unit
) {
    var enabled by remember { mutableStateOf(scheduleEnabled) }
    var hour by remember { mutableIntStateOf(scheduleHour.coerceIn(0, 23)) }
    var minute by remember { mutableIntStateOf(scheduleMinute.coerceIn(0, 59)) }
    var frequency by remember { mutableStateOf(scheduleFrequency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule scan") },
        text = {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Remind me to scan", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = SafeGreen)
                    )
                }
                if (enabled) {
                    Spacer(Modifier.height(12.dp))
                    Text("Frequency", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = frequency == SecurePreferencesManager.FREQ_DAILY,
                            onClick = { frequency = SecurePreferencesManager.FREQ_DAILY },
                            label = { Text("Daily") }
                        )
                        FilterChip(
                            selected = frequency == SecurePreferencesManager.FREQ_WEEKLY,
                            onClick = { frequency = SecurePreferencesManager.FREQ_WEEKLY },
                            label = { Text("Weekly") }
                        )
                    }
                    Text("Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                    Row(
                        Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { hour = (hour - 1).coerceIn(0, 23) }) { Text("−", style = MaterialTheme.typography.titleLarge) }
                            Text("${hour.coerceIn(0, 23)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(32.dp))
                            IconButton(onClick = { hour = (hour + 1).coerceIn(0, 23) }) { Text("+", style = MaterialTheme.typography.titleLarge) }
                        }
                        Text(":", style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { minute = (minute - 15).coerceIn(0, 59) }) { Text("−", style = MaterialTheme.typography.titleLarge) }
                            Text("%02d".format(minute.coerceIn(0, 59)), style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(32.dp))
                            IconButton(onClick = { minute = (minute + 15) % 60 }) { Text("+", style = MaterialTheme.typography.titleLarge) }
                        }
                        Text("${if (hour < 12) "AM" else "PM"}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(enabled, hour.coerceIn(0, 23), minute.coerceIn(0, 59), frequency) }) {
                Text("Save", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
