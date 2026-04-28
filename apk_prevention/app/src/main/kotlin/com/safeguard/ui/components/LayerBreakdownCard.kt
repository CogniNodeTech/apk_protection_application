package com.safeguard.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.safeguard.core.domain.model.Verdict
import com.safeguard.ui.theme.DangerRed
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.SafeGreen
import com.safeguard.ui.theme.WarningAmber

@Composable
fun LayerBreakdownCard(
    layerName: String,
    verdict: Verdict,
    evidenceSummary: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val verdictColor = when (verdict) {
        Verdict.SAFE -> SafeGreen
        Verdict.SUSPICIOUS, Verdict.UNKNOWN -> WarningAmber
        Verdict.MALICIOUS -> DangerRed
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.padding(Dimensions.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    layerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    verdict.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = verdictColor
                )
                if (evidenceSummary.isNotBlank()) {
                    Text(
                        evidenceSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
