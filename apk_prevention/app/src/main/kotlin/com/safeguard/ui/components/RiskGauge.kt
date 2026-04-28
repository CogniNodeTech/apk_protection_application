package com.safeguard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.safeguard.ui.theme.DangerRed
import com.safeguard.ui.theme.NeonGreen
import com.safeguard.ui.theme.SafeGreen
import com.safeguard.ui.theme.WarningAmber

@Composable
fun RiskGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 8.dp,
    scoreLabel: String = "Risk score",
    useNeonStyle: Boolean = false
) {
    val color = when {
        useNeonStyle -> NeonGreen
        score >= 70 -> DangerRed
        score >= 40 -> WarningAmber
        else -> SafeGreen
    }
    val density = LocalDensity.current
    val strokePx = with(density) { strokeWidth.toPx() }
    val sizePx = with(density) { size.toPx() }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val sweepAngle = (score / 100f) * 360f
    val startAngle = 270f
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(size)) {
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(strokePx / 2, strokePx / 2),
                    size = Size(sizePx - strokePx, sizePx - strokePx),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokePx / 2, strokePx / 2),
                    size = Size(sizePx - strokePx, sizePx - strokePx),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineLarge,
                    color = color
                )
                Text(
                    text = scoreLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
