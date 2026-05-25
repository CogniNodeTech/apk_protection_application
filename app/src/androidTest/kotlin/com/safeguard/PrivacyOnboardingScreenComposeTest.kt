package com.safeguard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.safeguard.ui.screens.onboarding.PrivacyOnboardingScreen
import com.safeguard.ui.theme.SafeGuardTheme
import org.junit.Rule
import org.junit.Test

class PrivacyOnboardingScreenComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun privacyOnboardingScreen_rendersCoreContent() {
        composeRule.setContent {
            SafeGuardTheme(darkTheme = true) {
                PrivacyOnboardingScreen(onContinue = {})
            }
        }

        composeRule.onNodeWithText("Welcome to SafeGuard").assertExists()
        composeRule.onNodeWithText("Before you continue, here is how the app uses data:").assertExists()
        composeRule.onNodeWithText("Continue").assertExists()
    }
}

