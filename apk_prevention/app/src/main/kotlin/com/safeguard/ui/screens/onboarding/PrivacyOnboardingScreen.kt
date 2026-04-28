package com.safeguard.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
fun PrivacyOnboardingScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasPrivacyPolicyUrl = hasRealLegalUrl(BuildConfig.PRIVACY_POLICY_URL)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.ScreenSidePadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to SafeGuard",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Dimensions.SectionSpacing))
        Text(
            text = "Before you continue, here is how the app uses data:",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Dimensions.ItemSpacing))
        val bullets = listOf(
            "Scans run on your device. Scan history and quarantine are stored locally in an encrypted database.",
            "When online, cloud verification may send non-file identifiers to our servers (for example file hashes, app package name, permission list, and device locale) so we can check them against threat intelligence. We do not upload your full APK file.",
            "You can turn off cloud verification anytime in Settings. You can delete all local data or export your scan history from Settings."
        )
        bullets.forEach { line ->
            Text(
                text = "• $line",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            )
        }
        if (hasPrivacyPolicyUrl) {
            Spacer(Modifier.height(Dimensions.SectionSpacing))
            TextButton(
                onClick = { context.openExternalUrl(BuildConfig.PRIVACY_POLICY_URL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Read full privacy policy",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(Dimensions.ItemSpacing))
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.ButtonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Text("Continue", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
        }
    }
}
