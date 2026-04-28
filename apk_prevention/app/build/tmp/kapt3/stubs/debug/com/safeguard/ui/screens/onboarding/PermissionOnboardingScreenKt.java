package com.safeguard.ui.screens.onboarding;

import android.os.Build;
import android.os.Environment;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material3.ButtonDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import com.safeguard.ui.theme.Dimensions;
import com.safeguard.util.StorageAccessHelper;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0014\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a \u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u00a8\u0006\u0006"}, d2 = {"PermissionOnboardingScreen", "", "onContinue", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "app_debug"})
public final class PermissionOnboardingScreenKt {
    
    /**
     * First-run permission onboarding for the deep storage / messenger-folder scan.
     *
     * Asks the user to grant **All files access** (`MANAGE_EXTERNAL_STORAGE`) on Android 11+.
     * Without it, scoped storage hides everything under `Android/data/<pkg>/`,
     * `Android/media/<pkg>/`, and many third-party folders — which is exactly where messenger
     * apps (WhatsApp, Telegram, etc.) drop received APK files. Real-time monitoring still works
     * without this permission, but the deep scan and chat-folder coverage do not.
     *
     * The user can:
     * - Grant: opens system Settings via [StorageAccessHelper.createManageAllFilesIntent].
     *          When they return with the permission granted, this screen advances automatically.
     * - Continue without it: user choice is respected; we keep their preference and they can
     *          re-enable later from Settings → All files access.
     *
     * On Android &lt; 11 this screen short-circuits because there is no equivalent permission;
     * the regular runtime READ_EXTERNAL_STORAGE prompt is sufficient.
     */
    @androidx.compose.runtime.Composable
    public static final void PermissionOnboardingScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onContinue, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
}