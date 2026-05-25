package com.safeguard.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safeguard.BuildConfig
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.NeonGreen
import com.safeguard.ui.util.openExternalUrl

private fun hasRealLegalUrl(url: String): Boolean =
    url.startsWith("https://", ignoreCase = true) &&
        !url.contains("example.com", ignoreCase = true)

@Composable
fun TermsAndConditionsScreen(
    onAgreedContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var agreed by remember { mutableStateOf(false) }
    val hasTermsUrl = remember { hasRealLegalUrl(BuildConfig.TERMS_OF_SERVICE_URL) }

    val termsPoints = remember {
        listOf(
            "SafeGuard scans APK files and helps detect risky behavior on your device.",
            "Scan results are based on layered analysis and threat-intel checks, and should be used as safety guidance.",
            "You are responsible for files and apps you choose to install. Download only from trusted sources.",
            "You can control privacy settings anytime, including cloud verification and telemetry options.",
            "We may update app features and policies over time. Continued use means you accept current terms.",
            "If you do not agree with these terms, you can close the app and uninstall it."
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.ScreenSidePadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Gavel,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "Terms & Conditions",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Please review these key points and accept to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                termsPoints.forEach { point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonGreen,
                            modifier = Modifier
                                .padding(top = 1.dp, end = 10.dp)
                                .background(
                                    color = NeonGreen.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp, fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (hasTermsUrl) {
            Spacer(Modifier.height(14.dp))
            TextButton(
                onClick = { context.openExternalUrl(BuildConfig.TERMS_OF_SERVICE_URL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Read complete terms",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = agreed,
                onCheckedChange = { agreed = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonGreen,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Text(
                text = "I have read and agree to these Terms & Conditions",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onAgreedContinue,
            enabled = agreed,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.ButtonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text("Continue", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
        }
    }
}
