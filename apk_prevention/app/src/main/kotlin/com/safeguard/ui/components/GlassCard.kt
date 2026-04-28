package com.safeguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.safeguard.ui.theme.GlassSurface
import com.safeguard.ui.theme.GlassSurfaceBorder

/**
 * Glassmorphism-style card in dark theme; solid surface in light theme for clear alignment.
 * Soft shadow, rounded corners, subtle border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    elevation: Dp = 8.dp,
    backgroundColor: Color = GlassSurface,
    borderColor: Color = GlassSurfaceBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val resolvedBackground = if (isLight) MaterialTheme.colorScheme.surface else backgroundColor
    val resolvedBorder = if (isLight) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else borderColor
    Box(
        modifier = modifier
            .shadow(elevation, shape, ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.2f))
            .clip(shape)
            .background(resolvedBackground)
            .then(if (borderWidth > 0.dp) Modifier.border(borderWidth, resolvedBorder, shape) else Modifier)
    ) {
        content()
    }
}
