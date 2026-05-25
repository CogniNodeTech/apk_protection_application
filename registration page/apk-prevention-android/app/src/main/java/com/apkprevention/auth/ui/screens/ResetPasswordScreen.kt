package com.apkprevention.auth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkprevention.auth.ui.components.AuthTextField
import com.apkprevention.auth.ui.theme.DarkBackground
import com.apkprevention.auth.ui.theme.ErrorRed
import com.apkprevention.auth.ui.theme.GreenDark
import com.apkprevention.auth.ui.theme.GreenLight
import com.apkprevention.auth.ui.theme.GreenPrimary
import com.apkprevention.auth.ui.theme.TextPrimary
import com.apkprevention.auth.ui.theme.TextSecondary
import com.apkprevention.auth.ui.viewmodel.AuthViewModel

@Composable
fun ResetPasswordScreen(
    viewModel: AuthViewModel,
    initialEmail: String,
    onBack: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Reset Password",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Request a reset link, then enter the token and your new password.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
        )

        AuthTextField(
            value = email,
            onValueChange = {
                email = it
                localError = null
            },
            placeholder = "Email Address",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            maxLength = 254
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                localError = null
                viewModel.sendPasswordReset(email)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !authState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp)
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
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send Reset Link", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (authState.resetEmailSent) {
            Text(
                text = "Reset link request accepted. Check email (or dev logs) for token.",
                color = GreenLight,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AuthTextField(
            value = token,
            onValueChange = {
                token = it
                localError = null
            },
            placeholder = "Reset Token",
            leadingIcon = Icons.Outlined.VpnKey,
            maxLength = 128
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                localError = null
            },
            placeholder = "New Password",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            maxLength = 128
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                localError = null
            },
            placeholder = "Confirm New Password",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            maxLength = 128
        )

        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = {
                localError = when {
                    token.isBlank() -> "Reset token is required"
                    newPassword.length < 8 -> "Password must be at least 8 characters"
                    newPassword != confirmPassword -> "Passwords do not match"
                    else -> null
                }
                if (localError == null) {
                    viewModel.confirmResetPassword(token, newPassword)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !authState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp)
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
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Set New Password", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        val errorToShow = localError ?: authState.error
        if (!errorToShow.isNullOrBlank()) {
            Text(
                text = errorToShow,
                color = ErrorRed,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (authState.resetPasswordDone) {
            Text(
                text = "Password reset successful. Tap here to return to Sign In.",
                color = GreenLight,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable { onBack() }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
