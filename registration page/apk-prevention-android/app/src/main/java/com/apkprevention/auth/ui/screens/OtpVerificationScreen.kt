package com.apkprevention.auth.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkprevention.auth.ui.theme.DarkBackground
import com.apkprevention.auth.ui.theme.ErrorRed
import com.apkprevention.auth.ui.theme.GreenDark
import com.apkprevention.auth.ui.theme.GreenLight
import com.apkprevention.auth.ui.theme.GreenPrimary
import com.apkprevention.auth.ui.theme.InputBackground
import com.apkprevention.auth.ui.theme.InputBorder
import com.apkprevention.auth.ui.theme.TextPrimary
import com.apkprevention.auth.ui.theme.TextSecondary
import com.apkprevention.auth.ui.theme.TextTertiary
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    devOtpHint: String? = null,
    onVerify: (String) -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    BackHandler(onBack = onBack)
    var otpValue by remember { mutableStateOf("") }
    var timer by remember { mutableIntStateOf(30) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer--
        }
    }

    val maskedPhone = if (phoneNumber.length > 6)
        phoneNumber.take(3) + "****" + phoneNumber.takeLast(3)
    else phoneNumber

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Verify Your Phone",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We've sent a 6-digit code to $maskedPhone",
                color = TextTertiary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            if (!devOtpHint.isNullOrBlank()) {
                Text(
                    text = "Dev / test code: $devOtpHint",
                    color = GreenLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // OTP input boxes
            BasicTextField(
                value = otpValue,
                onValueChange = { value ->
                    if (value.length <= 6 && value.all { it.isDigit() }) {
                        otpValue = value
                    }
                },
                modifier = Modifier.focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                cursorBrush = SolidColor(Color.Transparent),
                textStyle = TextStyle(color = Color.Transparent)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    repeat(6) { index ->
                        val char = otpValue.getOrNull(index)
                        val isFocused = otpValue.length == index

                        Box(
                            modifier = Modifier
                                .size(width = 48.dp, height = 56.dp)
                                .background(
                                    if (char != null) GreenPrimary.copy(alpha = 0.05f)
                                    else InputBackground,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = when {
                                        error != null -> ErrorRed.copy(alpha = 0.5f)
                                        isFocused -> GreenPrimary
                                        char != null -> GreenPrimary.copy(alpha = 0.4f)
                                        else -> InputBorder
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char?.toString() ?: "",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            if (error != null) {
                Text(
                    text = error,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Verify button
            Button(
                onClick = {
                    if (otpValue.length == 6) {
                        onVerify(otpValue)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                enabled = otpValue.length == 6 && !isLoading
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
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Verify & Continue",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Resend
            if (timer > 0) {
                Text(
                    text = "Resend code in ${timer}s",
                    color = TextTertiary,
                    fontSize = 13.sp
                )
            } else {
                TextButton(onClick = {
                    timer = 30
                    otpValue = ""
                    onResend()
                }) {
                    Text(
                        text = "Resend Code",
                        color = GreenLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
