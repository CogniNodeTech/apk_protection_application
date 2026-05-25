package com.safeguard.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.safeguard.ui.theme.NeonGreen
import com.safeguard.ui.theme.NeonGreenBright
import com.safeguard.ui.theme.PrimaryGreen

private val HexagonMinSize = 280.dp

/** Hexagon size as fraction of available space (reference: large, dominant). */
private const val HexagonFillFraction = 0.92f

/**
 * Large hexagon with strong neon glow and "Threat Level" / status text.
 * Design matches reference: fills the space, soft green glow, high-impact.
 */
@Composable
fun HexagonThreatLevel(
    threatLevelText: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.background.luminance() > 0.5f
    val strokeColor = if (isLight) PrimaryGreen else NeonGreen
    val strokeBright = if (isLight) PrimaryGreen else NeonGreenBright
    val infiniteTransition = rememberInfiniteTransition(label = "hexagon")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val maxW = maxWidth
        val maxH = maxHeight
        val minDimPx = with(density) { minOf(maxW.toPx(), maxH.toPx()) }
        val minDimDp = with(density) { minDimPx.toDp() }
        val hexSize = maxOf(HexagonMinSize, minDimDp * HexagonFillFraction)

        Box(
            modifier = Modifier.size(hexSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(hexSize)) {
                val minDim = this.size.minDimension
                val w = (minDim * HexagonFillFraction - 16f).coerceAtLeast(0f)
                val cx = this.size.width / 2
                val cy = this.size.height / 2
                val r = w / 2
                val path = Path().apply {
                    for (i in 0..5) {
                        val angle = Math.PI / 3 * i - Math.PI / 6
                        val x = cx + r * kotlin.math.cos(angle).toFloat()
                        val y = cy + r * kotlin.math.sin(angle).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                // Strong soft glow (reference-style): many layers, wide stroke, bleeds outward
                val glowBaseAlpha = if (isLight) 0.08f else 0.18f
                for (layer in 8 downTo 1) {
                    val alpha = glowBaseAlpha * pulse * (layer / 8f) * (1f - (layer - 1) * 0.06f)
                    drawPath(
                        path,
                        color = strokeColor.copy(alpha = alpha),
                        style = Stroke(width = (12 + layer * 14).toFloat())
                    )
                }
                // Mid glow
                drawPath(
                    path,
                    color = strokeColor.copy(alpha = if (isLight) 0.35f else 0.5f * pulse),
                    style = Stroke(width = 14f)
                )
                // Core stroke
                drawPath(
                    path,
                    color = strokeColor.copy(alpha = if (isLight) 0.6f else 0.85f * pulse),
                    style = Stroke(width = 6f)
                )
                // Bright edge (neon)
                drawPath(
                    path,
                    color = strokeBright,
                    style = Stroke(width = 2.5f)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(maxOf(hexSize * 0.12f, 24.dp))
            ) {
                Text(
                    "Threat Level",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    threatLevelText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = strokeColor
                )
            }
        }
    }
}
