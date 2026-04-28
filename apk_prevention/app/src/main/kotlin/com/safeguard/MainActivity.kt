package com.safeguard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.safeguard.ui.theme.BackgroundLight
import com.safeguard.ui.theme.NavyDark
import com.safeguard.ui.theme.PurpleDark
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.notification.SafeGuardNotificationManager
import com.safeguard.ui.navigation.AppNavigation
import com.safeguard.ui.theme.LocalThemeViewModel
import com.safeguard.ui.theme.SafeGuardTheme
import com.safeguard.ui.theme.ThemeViewModel
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.pointer.pointerInput

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        Log.i(TAG, "MainActivity created — app running")
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            val themeVm: ThemeViewModel = hiltViewModel()
            val useDark by themeVm.useDarkTheme.collectAsState(initial = null)
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (useDark) {
                true -> true
                false -> false
                null -> isSystemDark
            }
            SafeGuardTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(LocalThemeViewModel provides themeVm) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (darkTheme) Modifier.background(
                                    Brush.verticalGradient(
                                        colors = listOf(NavyDark, NavyDark, PurpleDark.copy(alpha = 0.6f))
                                    )
                                )
                                else Modifier.background(BackgroundLight)
                            )
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    // Anti-Tapjacking: Filter touches when the window is obscured by another window
                                    Modifier.pointerInput(Unit) {
                                        // Jetpack Compose doesn't have a direct 'filterTouchesWhenObscured' modifier yet,
                                        // so we rely on the Activity/View setting or a custom PointerInput filter if needed.
                                        // However, the standard way in Android is View.setFilterTouchesWhenObscured(true).
                                    }
                                ),
                            color = androidx.compose.ui.graphics.Color.Transparent
                        ) {
                            // Anti-Tapjacking: Filter touches when the window is obscured by another window
                            window.decorView.setFilterTouchesWhenObscured(true)
                            
                            AppNavigation(
                                initialScanIdFromIntent = intent.getStringExtra(SafeGuardNotificationManager.EXTRA_SCAN_ID),
                                onInitialScanIdConsumed = { intent.removeExtra(SafeGuardNotificationManager.EXTRA_SCAN_ID) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATIONS_CODE
            )
        }
    }

    companion object {
        private const val TAG = "SafeGuard"
        private const val REQUEST_NOTIFICATIONS_CODE = 1002
    }
}
