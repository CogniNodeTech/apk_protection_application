package com.apkprevention.auth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkprevention.auth.ui.components.AuthTextField
import com.apkprevention.auth.ui.components.DividerWithText
import com.apkprevention.auth.ui.components.SocialLoginButtons
import com.apkprevention.auth.ui.theme.DarkBackground
import com.apkprevention.auth.ui.theme.ErrorRed
import com.apkprevention.auth.ui.theme.GreenDark
import com.apkprevention.auth.ui.theme.GreenLight
import com.apkprevention.auth.ui.theme.GreenPrimary
import com.apkprevention.auth.ui.theme.TextPrimary
import com.apkprevention.auth.ui.theme.TextSecondary
import com.apkprevention.auth.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: (String) -> Unit,
    onGoogleSignIn: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var valid = true
        emailError = when {
            email.isBlank() -> { valid = false; "Email is required" }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                { valid = false; "Please enter a valid email" }
            else -> null
        }
        passwordError = when {
            password.isEmpty() -> { valid = false; "Password is required" }
            else -> null
        }
        return valid
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .background(DarkBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Welcome Back",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Sign in to continue your protection",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 36.dp)
            )

            // Social login
            SocialLoginButtons(
                onGoogleClick = onGoogleSignIn
            )

            Spacer(modifier = Modifier.height(24.dp))

            DividerWithText(text = "or sign in with email")

            Spacer(modifier = Modifier.height(24.dp))

            // Email
            AuthTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                    if (authState.resetEmailSent) viewModel.clearResetEmailSent()
                },
                placeholder = "Email Address",
                leadingIcon = Icons.Outlined.Email,
                error = emailError,
                keyboardType = KeyboardType.Email,
                maxLength = 254
            )
            Spacer(modifier = Modifier.height(14.dp))

            AuthTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                    if (authState.resetEmailSent) viewModel.clearResetEmailSent()
                },
                placeholder = "Password",
                leadingIcon = Icons.Outlined.Lock,
                error = passwordError,
                isPassword = true,
                imeAction = ImeAction.Done,
                maxLength = 128
            )

            // Forgot password
            Text(
                text = "Forgot Password?",
                color = GreenLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
                    .clickable { onForgotPassword(email) }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Login button
            Button(
                onClick = {
                    if (validate()) {
                        viewModel.signInWithEmail(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                enabled = !authState.isLoading
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(GreenPrimary, GreenDark)),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Sign In",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Error
            if (authState.error != null) {
                Text(
                    text = authState.error!!,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = "Register",
                    color = GreenLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
