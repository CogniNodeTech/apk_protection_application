package com.safeguard.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import android.util.Log
import com.safeguard.ui.theme.NeonGreen

private const val TAG = "SafeGuard"

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.background.luminance() > 0.5f
    val indicatorColor = when {
        isLight -> colorScheme.surfaceVariant.copy(alpha = 0.8f)
        else -> Color.Transparent
    }
    val items = listOf(
        BottomNavItem("dashboard", "Dashboard", Icons.Default.Home),
        BottomNavItem("scan_logs", "Details", Icons.Default.History),
        BottomNavItem("vault", "Vault", Icons.Default.Lock),
        BottomNavItem("reports", "Reports", Icons.Default.Assessment)
    )
    NavigationBar(
        modifier = modifier,
        containerColor = colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    Log.d(TAG, "Tab: ${item.label} ($item.route)")
                    onItemClick(item.route)
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        Modifier.size(24.dp)
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (isLight) colorScheme.primary else NeonGreen,
                    selectedTextColor = if (isLight) colorScheme.primary else NeonGreen,
                    indicatorColor = indicatorColor,
                    unselectedIconColor = colorScheme.onSurfaceVariant,
                    unselectedTextColor = colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
