package com.safeguard.ui.screens.dashboard;

import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.CardDefaults;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.SnackbarHostState;
import androidx.compose.material3.SwitchDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.ui.education.CyberSecurityAwarenessFacts;
import com.safeguard.ui.theme.Dimensions;
import java.io.File;
import android.Manifest;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.ContextCompat;
import com.safeguard.util.StorageAccessHelper;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000^\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0012\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0003\u001a^\u0010\u0006\u001a\u00020\u00032\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00030\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\b\b\u0002\u0010\u000e\u001a\u00020\u000fH\u0007\u001a&\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00012\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00030\nH\u0003\u001a6\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u00012\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00030\nH\u0003\u001a\u0098\u0001\u0010\u001c\u001a\u00020\u00032\u0006\u0010\u001d\u001a\u00020\u001a2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u001f2\u0006\u0010!\u001a\u00020\u00012\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00030\n2`\u0010#\u001a\\\u0012\u0013\u0012\u00110\u001a\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u001f\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b((\u0012\u0013\u0012\u00110\u001f\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0013\u0012\u00110\u0001\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(*\u0012\u0004\u0012\u00020\u00030$H\u0003\u001a\u0010\u0010+\u001a\u00020\u00032\u0006\u0010,\u001a\u00020-H\u0003\u001a\u0018\u0010.\u001a\u00020\u00012\u0006\u0010(\u001a\u00020\u001f2\u0006\u0010)\u001a\u00020\u001fH\u0002\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006/"}, d2 = {"APK_MIME", "", "DashboardBackground", "", "modifier", "Landroidx/compose/ui/Modifier;", "DashboardScreen", "onNavigateToScanResults", "Lkotlin/Function1;", "onNavigateToVault", "Lkotlin/Function0;", "onNavigateToSettings", "onNavigateToReports", "onNavigateToDeviceScan", "viewModel", "Lcom/safeguard/ui/screens/dashboard/DashboardViewModel;", "QuickActionCircle", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "label", "onClick", "RecentActivityCard", "apkName", "verdict", "Lcom/safeguard/core/domain/model/Verdict;", "isThreat", "", "codeSuffix", "ScheduleScanDialog", "scheduleEnabled", "scheduleHour", "", "scheduleMinute", "scheduleFrequency", "onDismiss", "onSave", "Lkotlin/Function4;", "Lkotlin/ParameterName;", "name", "enabled", "hour", "minute", "frequency", "ThreatFeedStatusCard", "display", "Lcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter$Display;", "formatTime", "app_debug"})
public final class DashboardScreenKt {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String APK_MIME = "application/vnd.android.package-archive";
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void DashboardScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToScanResults, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToVault, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToSettings, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToReports, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToDeviceScan, @org.jetbrains.annotations.NotNull
    com.safeguard.ui.screens.dashboard.DashboardViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void DashboardBackground(androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void QuickActionCircle(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String label, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RecentActivityCard(java.lang.String apkName, com.safeguard.core.domain.model.Verdict verdict, boolean isThreat, java.lang.String codeSuffix, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    /**
     * Threat database freshness tile. Three colour variants drive at-a-glance readability:
     * - OK (green) — sync ran cleanly inside the freshness window;
     * - WARNING (amber) — recent attempt failed, or last success is past the staleness threshold;
     * - ERROR (red) — never synced or failed without any prior success to fall back on.
     *
     * The card is intentionally non-interactive: the user can't trigger an ad-hoc sync from
     * here because a manual sync button would invite hammering the threat-intel server (and
     * WorkManager already retries on its own backoff). Surfacing the *state* is the goal, not
     * exposing a control surface.
     */
    @androidx.compose.runtime.Composable
    private static final void ThreatFeedStatusCard(com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Display display) {
    }
    
    private static final java.lang.String formatTime(int hour, int minute) {
        return null;
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    private static final void ScheduleScanDialog(boolean scheduleEnabled, int scheduleHour, int scheduleMinute, java.lang.String scheduleFrequency, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function4<? super java.lang.Boolean, ? super java.lang.Integer, ? super java.lang.Integer, ? super java.lang.String, kotlin.Unit> onSave) {
    }
}