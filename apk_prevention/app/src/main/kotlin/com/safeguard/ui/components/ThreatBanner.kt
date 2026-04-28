package com.safeguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.safeguard.core.domain.model.Verdict
import com.safeguard.ui.theme.DangerRed
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.SafeGreen
import com.safeguard.ui.theme.WarningAmber

@Composable
fun ThreatBanner(
    verdict: Verdict,
    explanation: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (verdict) {
        Verdict.SAFE -> SafeGreen.copy(alpha = 0.2f) to Color(0xFF1B5E20)
        Verdict.SUSPICIOUS, Verdict.UNKNOWN -> WarningAmber.copy(alpha = 0.2f) to Color(0xFFE65100)
        Verdict.MALICIOUS -> DangerRed.copy(alpha = 0.2f) to DangerRed
    }
    val label = when (verdict) {
        Verdict.SAFE -> "Safe"
        Verdict.SUSPICIOUS -> "Suspicious"
        Verdict.UNKNOWN -> "Needs review"
        Verdict.MALICIOUS -> "Threat detected"
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(Dimensions.CardPadding)
    ) {
        androidx.compose.foundation.layout.Column {
            Text(
                label,
                style = MaterialTheme.typography.titleLarge,
                color = textColor
            )
            Text(
                explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
