package com.safeguard.ui.screens.settings;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.SwitchDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import androidx.core.content.ContextCompat;
import com.safeguard.BuildConfig;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.service.FileObserverService;
import com.safeguard.ui.theme.Dimensions;
import com.safeguard.ui.theme.ThemeViewModel;
import com.safeguard.util.StorageAccessHelper;
import androidx.compose.ui.Alignment;
import android.widget.Toast;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000*\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\u001aD\u0010\u0000\u001a\u00020\u00012\u0010\b\u0002\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u0007\u001a\u0010\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\nH\u0003\u001a\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0002\u00a8\u0006\u000f"}, d2 = {"SettingsScreen", "", "onBack", "Lkotlin/Function0;", "onNavigateToOpenSourceLicenses", "onLogout", "viewModel", "Lcom/safeguard/ui/screens/settings/SettingsViewModel;", "ThemeSection", "themeViewModel", "Lcom/safeguard/ui/theme/ThemeViewModel;", "hasRealLegalUrl", "", "url", "", "app_debug"})
public final class SettingsScreenKt {
    
    private static final boolean hasRealLegalUrl(java.lang.String url) {
        return false;
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void SettingsScreen(@org.jetbrains.annotations.Nullable
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToOpenSourceLicenses, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onLogout, @org.jetbrains.annotations.NotNull
    com.safeguard.ui.screens.settings.SettingsViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ThemeSection(com.safeguard.ui.theme.ThemeViewModel themeViewModel) {
    }
}