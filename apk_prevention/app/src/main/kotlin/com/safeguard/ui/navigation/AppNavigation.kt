package com.safeguard.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.safeguard.ui.components.BottomNavBar
import com.safeguard.ui.screens.dashboard.DashboardScreen
import com.safeguard.ui.screens.detailedanalysis.DetailedAnalysisScreen
import com.safeguard.ui.screens.history.HistoryScreen
import com.safeguard.ui.screens.protectionstatus.ProtectionStatusDetailScreen
import com.safeguard.ui.screens.protectionstatus.ProtectionStatusScreen
import com.safeguard.ui.screens.quarantine.QuarantineScreen
import com.safeguard.ui.screens.scanresults.ScanResultsScreen
import com.safeguard.ui.screens.settings.SettingsScreen
import com.safeguard.ui.screens.scanprogress.DeviceScanProgressScreen
import com.safeguard.ui.screens.scanprogress.DeviceScanProgressViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.safeguard.ui.MainViewModel
import com.safeguard.ui.screens.auth.AuthFlow
import com.safeguard.ui.screens.onboarding.PermissionOnboardingScreen
import com.safeguard.ui.screens.onboarding.PrivacyOnboardingScreen
import com.safeguard.ui.screens.onboarding.TermsAndConditionsScreen
import com.safeguard.ui.screens.legal.OpenSourceLicensesScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.unit.dp
import com.safeguard.ui.theme.NeonGreen

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_SCAN_LOGS = "scan_logs"
const val ROUTE_VAULT = "vault"
const val ROUTE_REPORTS = "reports"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_SCAN_RESULTS = "scan_results/{scanId}"
const val ROUTE_DETAILED_ANALYSIS = "detailed_analysis/{scanId}"
const val ROUTE_PROTECTION_DETAIL = "protection_detail/{category}"
const val ROUTE_DEVICE_SCAN_PROGRESS = "device_scan_progress"
const val ROUTE_OSS_LICENSES = "open_source_licenses"

private val tabRoutes = setOf(ROUTE_DASHBOARD, ROUTE_SCAN_LOGS, ROUTE_VAULT, ROUTE_REPORTS)

/**
 * Bottom-tab navigation: single back stack anchored at the graph start destination so tabs stay
 * independent and switching always works (string-only [popUpTo] can mis-resolve in Navigation Compose).
 */
private fun NavController.navigateToTopLevelTab(route: String) {
    navigate(route) {
        // Do not save/restore nested screens (e.g. Reports → detail); switching tabs always shows each tab’s root.
        popUpTo(graph.findStartDestination().id) {
            saveState = false
        }
        launchSingleTop = true
        restoreState = false
    }
}

@Composable
fun AppNavigation(
    initialScanIdFromIntent: String? = null,
    onInitialScanIdConsumed: () -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val isAuthenticated by mainViewModel.isAuthenticated.collectAsState()
    val termsAccepted by mainViewModel.termsAcceptedComplete.collectAsState()
    val onboardingComplete by mainViewModel.privacyOnboardingComplete.collectAsState()
    val requireOnboarding by mainViewModel.requireOnboardingAfterRegistration.collectAsState()
    val permissionOnboardingComplete by mainViewModel.permissionOnboardingComplete.collectAsState()
    if (!isAuthenticated) {
        AuthFlow(
            onAuthenticated = { mainViewModel.markAuthenticated() }
        )
        return
    }
    if (requireOnboarding && !termsAccepted) {
        TermsAndConditionsScreen(
            onAgreedContinue = { mainViewModel.acknowledgeTermsAndConditions() },
            modifier = Modifier.fillMaxSize()
        )
        return
    }
    if (requireOnboarding && !onboardingComplete) {
        PrivacyOnboardingScreen(
            onContinue = { mainViewModel.acknowledgePrivacyOnboarding() },
            modifier = Modifier.fillMaxSize()
        )
        return
    }
    // Permission onboarding gate: shown once per device, independent of [requireOnboarding] so
    // users who registered before this build also see it once. The screen short-circuits on
    // Android <11 (no MANAGE_EXTERNAL_STORAGE there).
    if (!permissionOnboardingComplete) {
        PermissionOnboardingScreen(
            onContinue = { mainViewModel.acknowledgePermissionOnboarding() },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    val navController = rememberNavController()
    LaunchedEffect(initialScanIdFromIntent) {
        if (!initialScanIdFromIntent.isNullOrBlank()) {
            navController.navigate("scan_results/$initialScanIdFromIntent") {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                launchSingleTop = true
            }
            onInitialScanIdConsumed()
        }
    }
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route?.let { route ->
        when {
            route.startsWith("scan_results/") -> ROUTE_DASHBOARD
            route.startsWith("detailed_analysis/") -> ROUTE_DASHBOARD
            route.startsWith("protection_detail/") -> ROUTE_REPORTS
            else -> route
        }
    }
    val showBottomBar = currentRoute in tabRoutes

    val scanViewModel: DeviceScanProgressViewModel = hiltViewModel()
    val scanState by scanViewModel.scanState.collectAsState()

    androidx.compose.material3.Scaffold(
        bottomBar = {
            Column {
                if (scanState.isScanning && currentRoute != ROUTE_DEVICE_SCAN_PROGRESS) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { navController.navigate(ROUTE_DEVICE_SCAN_PROGRESS) { launchSingleTop = true } }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                                Text("Device Scan in progress...", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("${scanState.scannedApps}/${scanState.totalApps}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            val progress = if (scanState.totalApps > 0) scanState.scannedApps.toFloat() / scanState.totalApps else 0f
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                color = NeonGreen,
                                trackColor = MaterialTheme.colorScheme.surface.copy(alpha=0.5f)
                            )
                        }
                    }
                }

            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = when (currentRoute) {
                        ROUTE_DASHBOARD -> "dashboard"
                        ROUTE_SCAN_LOGS -> "scan_logs"
                        ROUTE_VAULT -> "vault"
                        ROUTE_REPORTS -> "reports"
                        else -> null
                    },
                    onItemClick = { route -> navController.navigateToTopLevelTab(route) }
                )
            }
            } // end Column
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_DASHBOARD,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(ROUTE_DASHBOARD) {
                DashboardScreen(
                    onNavigateToScanResults = { id -> navController.navigate("scan_results/$id") },
                    onNavigateToVault = { navController.navigateToTopLevelTab(ROUTE_VAULT) },
                    onNavigateToSettings = { navController.navigate(ROUTE_SETTINGS) { launchSingleTop = true } },
                    onNavigateToReports = { navController.navigateToTopLevelTab(ROUTE_REPORTS) },
                    onNavigateToDeviceScan = { navController.navigate(ROUTE_DEVICE_SCAN_PROGRESS) { launchSingleTop = true } }
                )
            }
            composable(ROUTE_SCAN_LOGS) {
                HistoryScreen(
                    onScanClick = { id -> navController.navigate("scan_results/$id") },
                )
            }
            composable(ROUTE_VAULT) {
                QuarantineScreen()
            }
            composable(ROUTE_REPORTS) {
                ProtectionStatusScreen(
                    onBack = null,
                    onNavigateToSettings = { navController.navigate(ROUTE_SETTINGS) { launchSingleTop = true } },
                    onNavigateToDetail = { category -> navController.navigate("protection_detail/$category") }
                )
            }
            composable(
                route = ROUTE_PROTECTION_DETAIL,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { _ ->
                ProtectionStatusDetailScreen(
                    onBack = { navController.popBackStack() },
                    onScanClick = { scanId -> navController.navigate("scan_results/$scanId") }
                )
            }
            composable(ROUTE_SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToOpenSourceLicenses = { navController.navigate(ROUTE_OSS_LICENSES) },
                    onLogout = { mainViewModel.logout() }
                )
            }
            composable(ROUTE_OSS_LICENSES) {
                OpenSourceLicensesScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = ROUTE_SCAN_RESULTS,
                arguments = listOf(navArgument("scanId") { type = NavType.StringType })
            ) { entry ->
                val scanId = entry.arguments?.getString("scanId") ?: ""
                ScanResultsScreen(
                    scanId = scanId,
                    onBack = { navController.popBackStack() },
                    onNavigateToDetailedAnalysis = { navController.navigate("detailed_analysis/$scanId") }
                )
            }
            composable(
                route = ROUTE_DETAILED_ANALYSIS,
                arguments = listOf(navArgument("scanId") { type = NavType.StringType })
            ) { entry ->
                val scanId = entry.arguments?.getString("scanId") ?: ""
                DetailedAnalysisScreen(
                    scanId = scanId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(ROUTE_DEVICE_SCAN_PROGRESS) {
                DeviceScanProgressScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
