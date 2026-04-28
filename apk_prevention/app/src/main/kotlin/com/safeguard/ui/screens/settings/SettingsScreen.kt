package com.safeguard.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.BuildConfig
import com.safeguard.data.local.preferences.SecurePreferencesManager
import com.safeguard.service.FileObserverService
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.LocalThemeViewModel
import com.safeguard.ui.theme.NeonGreen
import com.safeguard.ui.theme.ThemeViewModel
import com.safeguard.ui.util.openExternalUrl
import com.safeguard.util.StorageAccessHelper
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import android.widget.Toast

private fun hasRealLegalUrl(url: String): Boolean =
    url.startsWith("https://", ignoreCase = true) &&
        !url.contains("example.com", ignoreCase = true)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: (() -> Unit)? = null,
    onNavigateToOpenSourceLicenses: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val monitoring by viewModel.monitoringEnabled.collectAsState(initial = true)
    val deepScan by viewModel.deepScanEnabled.collectAsState(initial = true)
    val notificationLevel by viewModel.notificationLevel.collectAsState(initial = SecurePreferencesManager.LEVEL_NORMAL)
    val cloudVerification by viewModel.cloudVerificationEnabled.collectAsState(initial = true)
    val scanTelemetry by viewModel.scanTelemetryEnabled.collectAsState(initial = true)
    val privacySharingOptOut by viewModel.privacySharingOptOut.collectAsState(initial = false)
    val scanFeedback by viewModel.scanFeedbackEnabled.collectAsState(initial = false)
    val queuedFeedbackCount by viewModel.queuedFeedbackCount.collectAsState(initial = 0)
    val themeVm = LocalThemeViewModel.current
    val context = LocalContext.current
    val hasPrivacyPolicyUrl = hasRealLegalUrl(BuildConfig.PRIVACY_POLICY_URL)
    val hasTermsUrl = hasRealLegalUrl(BuildConfig.TERMS_OF_SERVICE_URL)

    val storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE
    val hasStoragePermission = ContextCompat.checkSelfPermission(context, storagePermission) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.setMonitoring(true)
            val intent = Intent(context, FileObserverService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            viewModel.setMonitoring(false)
            Toast.makeText(context, "Storage permission is required for real-time APK monitoring", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
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
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = Dimensions.ScreenSidePadding, vertical = Dimensions.ScreenPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Protection",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            ListItem(
                headlineContent = { Text("Real-time monitoring") },
                supportingContent = { Text("Scan new APK files when they appear (Downloads, Telegram, WhatsApp, etc.)") },
                leadingContent = {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = monitoring,
                        onCheckedChange = { enabled ->
                            val intent = Intent(context, FileObserverService::class.java)
                            if (enabled) {
                                if (hasStoragePermission) {
                                    viewModel.setMonitoring(true)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                } else {
                                    permissionLauncher.launch(storagePermission)
                                }
                            } else {
                                viewModel.setMonitoring(false)
                                context.stopService(intent)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = NeonGreen)
                    )
                }
            )
            ListItem(
                headlineContent = { Text("Deep scan mode") },
                supportingContent = { Text("More thorough analysis (slower)") },
                trailingContent = {
                    Switch(
                        checked = deepScan,
                        onCheckedChange = { viewModel.setDeepScan(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = NeonGreen)
                    )
                }
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val allFilesGranted = Environment.isExternalStorageManager()
                ListItem(
                    modifier = Modifier.clickable {
                        try {
                            context.startActivity(StorageAccessHelper.createManageAllFilesIntent(context))
                        } catch (_: Exception) {
                            Toast.makeText(
                                context,
                                "Could not open settings. Try Apps → SafeGuard → All files access.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    headlineContent = { Text("All files access (deep scan)") },
                    supportingContent = {
                        Text(
                            if (allFilesGranted) {
                                "Granted. Tap to review or change before running a deep scan."
                            } else {
                                "Not granted. Tap to open system settings so deep scans can search all folders."
                            }
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                )
            }
            Spacer(Modifier.height(Dimensions.SectionSpacing))
            Divider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.ItemSpacing))

            Text(
                "Notifications",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            ListItem(
                headlineContent = { Text("Notification level") },
                supportingContent = {
                    Text(
                        when (notificationLevel) {
                            SecurePreferencesManager.LEVEL_SILENT -> "Only critical alerts"
                            SecurePreferencesManager.LEVEL_VERBOSE -> "All scan results"
                            else -> "Threats and important events"
                        }
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )
            Spacer(Modifier.height(Dimensions.SectionSpacing))
            Divider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.ItemSpacing))

            themeVm?.let { vm ->
                ThemeSection(themeViewModel = vm)
            }
            Spacer(Modifier.height(Dimensions.SectionSpacing))
            Divider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.ItemSpacing))

            Text(
                "Privacy",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            ListItem(
                headlineContent = { Text("Privacy policy") },
                supportingContent = {
                    Text(
                        if (hasPrivacyPolicyUrl) "How we handle your data (opens in browser)"
                        else "Privacy policy URL is not configured for this build"
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = if (hasPrivacyPolicyUrl) {
                    Modifier.clickable { context.openExternalUrl(BuildConfig.PRIVACY_POLICY_URL) }
                } else {
                    Modifier
                }
            )
            ListItem(
                headlineContent = { Text("Terms of service") },
                supportingContent = {
                    Text(
                        if (hasTermsUrl) "Rules for using SafeGuard"
                        else "Terms URL is not configured for this build"
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = if (hasTermsUrl) {
                    Modifier.clickable { context.openExternalUrl(BuildConfig.TERMS_OF_SERVICE_URL) }
                } else {
                    Modifier
                }
            )
            ListItem(
                headlineContent = { Text("Cloud verification") },
                supportingContent = { Text("Send metadata to threat-intel servers when online (see privacy policy)") },
                leadingContent = {
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = cloudVerification,
                        onCheckedChange = { viewModel.setCloudVerification(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = NeonGreen)
                    )
                }
            )
            ListItem(
                headlineContent = { Text("Scan analytics (telemetry)") },
                supportingContent = { Text("Send privacy-safe scan summaries for product improvement (no file names in telemetry)") },
                leadingContent = {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = scanTelemetry,
                        onCheckedChange = { viewModel.setScanTelemetry(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = NeonGreen)
                    )
                }
            )
            ListItem(
                headlineContent = { Text("Limit sharing / sale (US privacy)") },
                supportingContent = { Text("Opt out of sale or sharing of personal information for analytics where applicable") },
                leadingContent = {
                    Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = privacySharingOptOut,
                        onCheckedChange = { viewModel.setPrivacySharingOptOut(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = NeonGreen)
                    )
                }
            )
            ListItem(
                headlineContent = { Text("Help improve detection (opt-in)") },
                supportingContent = {
                    Text(
                        "Send privacy-safe scan feedback so we can tune detection layers. Hashes and verdicts only — never the APK itself, file path, name, or any personal data."
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Feedback, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = scanFeedback,
                        onCheckedChange = { viewModel.setScanFeedback(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = NeonGreen)
                    )
                }
            )
            ListItem(
                headlineContent = { Text("Clear queued feedback") },
                supportingContent = {
                    Text(
                        if (queuedFeedbackCount == 0) {
                            "No feedback events waiting to upload."
                        } else {
                            "$queuedFeedbackCount event(s) waiting. Tap to delete them locally without sending."
                        }
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = if (queuedFeedbackCount > 0) {
                    Modifier.clickable {
                        viewModel.clearFeedbackQueue {
                            Toast.makeText(
                                context,
                                "Queued feedback events deleted.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Modifier
                }
            )
            ListItem(
                headlineContent = { Text("Export scan history") },
                supportingContent = { Text("Share a JSON copy of your saved scans (access / portability)") },
                leadingContent = {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.clickable {
                    viewModel.exportScanHistoryJson { json ->
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "SafeGuard scan history export")
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(Intent.createChooser(send, "Export scan history"))
                    }
                }
            )
            ListItem(
                headlineContent = { Text("Open source licenses") },
                supportingContent = { Text("Third-party software notices") },
                leadingContent = {
                    Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.clickable { onNavigateToOpenSourceLicenses() }
            )
            ListItem(
                headlineContent = { Text("Delete all data", color = MaterialTheme.colorScheme.error) },
                supportingContent = { Text("Permanently erase scan history and quarantine data") },
                modifier = Modifier.clickable {
                    viewModel.deleteAllData {
                        Toast.makeText(context, "All data has been permanently erased.", Toast.LENGTH_LONG).show()
                    }
                }
            )
            Spacer(Modifier.height(Dimensions.SectionSpacing))
            Divider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.ItemSpacing))

            Text(
                "Advanced",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            ListItem(
                headlineContent = { Text("Logout") },
                supportingContent = { Text("Sign out and return to registration/login") },
                leadingContent = {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.clickable { onLogout() }
            )
            ListItem(
                headlineContent = { Text("Family Guardian") },
                supportingContent = { Text("Coming soon") }
            )
            Spacer(Modifier.height(Dimensions.SectionSpacing))
            Divider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.SectionSpacing))
            Text(
                "App version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ThemeSection(themeViewModel: ThemeViewModel) {
    val useDark by themeViewModel.useDarkTheme.collectAsState(initial = null)
    val isSystemDark = isSystemInDarkTheme()
    val darkOn = when (useDark) {
        true -> true
        false -> false
        null -> isSystemDark
    }
    Text(
        "Appearance",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    ListItem(
        headlineContent = { Text("Theme") },
        supportingContent = { Text("Enable Dark Mode.") },
        leadingContent = {
            Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.LightMode,
                    contentDescription = "Light",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Switch(
                    checked = darkOn,
                    onCheckedChange = { enabled ->
                        themeViewModel.setThemeMode(
                            if (enabled) SecurePreferencesManager.THEME_DARK
                            else SecurePreferencesManager.THEME_LIGHT
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = NeonGreen
                    )
                )
                Icon(
                    Icons.Default.DarkMode,
                    contentDescription = "Dark",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
    Spacer(Modifier.height(Dimensions.SectionSpacing))
    Divider(Modifier.fillMaxWidth())
    Spacer(Modifier.height(Dimensions.ItemSpacing))
}
