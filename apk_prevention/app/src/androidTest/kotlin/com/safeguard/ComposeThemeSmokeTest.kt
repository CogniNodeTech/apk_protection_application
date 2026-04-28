package com.safeguard

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.safeguard.ui.theme.SafeGuardTheme
import org.junit.Rule
import org.junit.Test

/**
 * Validates the Compose + UI test pipeline (Phase 3). Expand with navigation tests behind Hilt test doubles.
 */
class ComposeThemeSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun safeGuardTheme_rendersContent() {
        composeRule.setContent {
            SafeGuardTheme(darkTheme = true) {
                Text("SafeGuardComposeTest")
            }
        }
        composeRule.onNodeWithText("SafeGuardComposeTest").assertExists()
    }
}
