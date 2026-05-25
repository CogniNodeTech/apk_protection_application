package com.apkprevention.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.apkprevention.auth.navigation.AuthNavigation
import com.apkprevention.auth.ui.theme.APKPreventionTheme
import com.apkprevention.auth.ui.theme.DarkBackground
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val analyzerPackages = listOf("com.safeguard", "com.safeguard.test")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            APKPreventionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    AuthNavigation(
                        onAuthSuccess = {
                            val launched = launchAnalyzerDashboard()
                            if (!launched) {
                                Toast.makeText(
                                    this,
                                    "Authentication successful. Install the APK Analyzer app (com.safeguard) to continue.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun launchAnalyzerDashboard(): Boolean {
        for (pkg in analyzerPackages) {
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg) ?: continue
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
            finish()
            return true
        }
        return false
    }
}
