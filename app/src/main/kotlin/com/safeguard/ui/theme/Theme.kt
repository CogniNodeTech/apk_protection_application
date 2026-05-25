package com.safeguard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.safeguard.ui.theme.NavyDark

val LocalThemeViewModel = compositionLocalOf<ThemeViewModel?> { null }

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimary,
    primaryContainer = SafeGreen,
    onPrimaryContainer = OnPrimary,
    secondary = PrimaryDark,
    onSecondary = OnPrimary,
    tertiary = SafeGreen,
    onTertiary = OnPrimary,
    background = BackgroundLight,
    onBackground = OnBackground,
    surface = SurfaceLight,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    error = Error,
    onError = OnPrimary
)

/** Premium dark: deep navy + purple gradient feel, neon green accents */
private val PremiumDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = NavyDark,
    primaryContainer = NeonGreen.copy(alpha = 0.2f),
    onPrimaryContainer = NeonGreenBright,
    secondary = NeonGreen,
    onSecondary = NavyDark,
    tertiary = NeonGreenBright,
    onTertiary = NavyDark,
    background = NavyDark,
    onBackground = Color.White,
    surface = NavyMid,
    onSurface = Color.White,
    surfaceVariant = PurpleDark,
    onSurfaceVariant = OnDarkSecondary,
    outline = GlassSurfaceBorder,
    error = DangerRedDark,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SafeGreenDark,
    onPrimary = Color.Black,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = OnBackgroundDark,
    secondary = SafeGreenDark,
    onSecondary = Color.Black,
    tertiary = WarningAmberDark,
    onTertiary = Color.Black,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = DangerRedDark,
    onError = Color.White
)

@Composable
fun SafeGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PremiumDarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) NavyDark.toArgb() else colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
