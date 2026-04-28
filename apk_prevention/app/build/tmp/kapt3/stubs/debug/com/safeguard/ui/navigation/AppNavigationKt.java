package com.safeguard.ui.navigation;

import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import androidx.navigation.NavController;
import androidx.navigation.NavType;
import com.safeguard.ui.screens.scanprogress.DeviceScanProgressViewModel;
import com.safeguard.ui.MainViewModel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000,\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\"\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a.\u0010\r\u001a\u00020\u000e2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00012\u000e\b\u0002\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000e0\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u0013H\u0007\u001a\u0014\u0010\u0014\u001a\u00020\u000e*\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0001H\u0002\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0002\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0003\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0004\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0005\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0006\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0007\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\b\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\t\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\n\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"ROUTE_DASHBOARD", "", "ROUTE_DETAILED_ANALYSIS", "ROUTE_DEVICE_SCAN_PROGRESS", "ROUTE_OSS_LICENSES", "ROUTE_PROTECTION_DETAIL", "ROUTE_REPORTS", "ROUTE_SCAN_LOGS", "ROUTE_SCAN_RESULTS", "ROUTE_SETTINGS", "ROUTE_VAULT", "tabRoutes", "", "AppNavigation", "", "initialScanIdFromIntent", "onInitialScanIdConsumed", "Lkotlin/Function0;", "mainViewModel", "Lcom/safeguard/ui/MainViewModel;", "navigateToTopLevelTab", "Landroidx/navigation/NavController;", "route", "app_debug"})
public final class AppNavigationKt {
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String ROUTE_DASHBOARD = "dashboard";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String ROUTE_SCAN_LOGS = "scan_logs";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String ROUTE_VAULT = "vault";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String ROUTE_REPORTS = "reports";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String ROUTE_SETTINGS = "settings";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String ROUTE_SCAN_RESULTS = "scan_results/{scanId}";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String ROUTE_DETAILED_ANALYSIS = "detailed_analysis/{scanId}";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String ROUTE_PROTECTION_DETAIL = "protection_detail/{category}";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String ROUTE_DEVICE_SCAN_PROGRESS = "device_scan_progress";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String ROUTE_OSS_LICENSES = "open_source_licenses";
    @org.jetbrains.annotations.NotNull
    private static final java.util.Set<java.lang.String> tabRoutes = null;
    
    /**
     * Bottom-tab navigation: single back stack anchored at the graph start destination so tabs stay
     * independent and switching always works (string-only [popUpTo] can mis-resolve in Navigation Compose).
     */
    private static final void navigateToTopLevelTab(androidx.navigation.NavController $this$navigateToTopLevelTab, java.lang.String route) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void AppNavigation(@org.jetbrains.annotations.Nullable
    java.lang.String initialScanIdFromIntent, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onInitialScanIdConsumed, @org.jetbrains.annotations.NotNull
    com.safeguard.ui.MainViewModel mainViewModel) {
    }
}