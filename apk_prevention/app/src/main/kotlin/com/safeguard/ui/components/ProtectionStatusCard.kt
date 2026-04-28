package com.safeguard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.SafeGreen
import com.safeguard.ui.theme.WarningAmber

@Composable
fun ProtectionStatusCard(
    status: String,
    monitoringEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val tint = if (monitoringEnabled) SafeGreen else WarningAmber
    val containerColor = tint.copy(alpha = 0.12f)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier.padding(Dimensions.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                Modifier.size(48.dp),
                tint = tint
            )
            Column {
                Text(
                    "Protection status",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    status,
                    style = MaterialTheme.typography.titleLarge,
                    color = tint
                )
            }
        }
    }
}
