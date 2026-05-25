package com.safeguard.ui.screens.onboarding

import android.os.Build
import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.NeonGreen
import com.safeguard.util.StorageAccessHelper

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
 *  - Grant: opens system Settings via [StorageAccessHelper.createManageAllFilesIntent].
 *           When they return with the permission granted, this screen advances automatically.
 *  - Continue without it: user choice is respected; we keep their preference and they can
 *           re-enable later from Settings → All files access.
 *
 * On Android &lt; 11 this screen short-circuits because there is no equivalent permission;
 * the regular runtime READ_EXTERNAL_STORAGE prompt is sufficient.
 */
@Composable
fun PermissionOnboardingScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Skip the screen entirely on Android <11; there is no MANAGE_EXTERNAL_STORAGE there.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        LaunchedEffect(Unit) { onContinue() }
        return
    }

    var allFilesGranted by remember { mutableStateOf(Environment.isExternalStorageManager()) }

    // Re-check the permission whenever this screen comes back to the foreground (e.g. after the
    // user toggles the permission in system Settings and presses Back).
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                allFilesGranted = Environment.isExternalStorageManager()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(allFilesGranted) {
        if (allFilesGranted) onContinue()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.ScreenSidePadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Allow deep scanning",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Dimensions.SectionSpacing))
        Text(
            text = "SafeGuard works best when it can read every folder on your phone for hidden APK files.",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 24.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Dimensions.ItemSpacing))
        val bullets = listOf(
            "Without this permission, Android hides chat-app folders such as WhatsApp and Telegram from us. Malicious APKs sent through chat apps would be missed.",
            "We only look for installer files (APKs). Your photos, messages, and documents are never read or sent anywhere.",
            "You can change this any time in Settings \u2192 All files access."
        )
        bullets.forEach { line ->
            Text(
                text = "\u2022 $line",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            )
        }
        Spacer(Modifier.height(Dimensions.SectionSpacing))
        Button(
            onClick = {
                try {
                    context.startActivity(StorageAccessHelper.createManageAllFilesIntent(context))
                } catch (_: Exception) {
                    // Some OEMs do not surface the system screen; fall through to skip.
                    onContinue()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.ButtonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                "Allow all files access",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
            )
        }
        Spacer(Modifier.height(Dimensions.ItemSpacing))
        TextButton(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.ButtonHeight)
        ) {
            Text(
                "Continue without it",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
