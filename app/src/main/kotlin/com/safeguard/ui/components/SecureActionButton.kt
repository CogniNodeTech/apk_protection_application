package com.safeguard.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.safeguard.ui.theme.DangerRed
import com.safeguard.ui.theme.Dimensions

enum class SecureButtonStyle { Primary, Secondary, Danger, Text }

@Composable
fun SecureActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SecureButtonStyle = SecureButtonStyle.Primary,
    enabled: Boolean = true
) {
    val minHeight = Dimensions.MinTouchTarget
    when (style) {
        SecureButtonStyle.Primary -> Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = minHeight),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
        SecureButtonStyle.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = minHeight),
            enabled = enabled
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
        SecureButtonStyle.Danger -> Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = minHeight),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
        SecureButtonStyle.Text -> TextButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = minHeight),
            enabled = enabled
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
