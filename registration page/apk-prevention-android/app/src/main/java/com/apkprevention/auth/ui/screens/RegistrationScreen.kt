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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkprevention.auth.ui.components.AuthTextField
import com.apkprevention.auth.ui.components.DividerWithText
import com.apkprevention.auth.ui.components.PasswordStrengthIndicator
import com.apkprevention.auth.ui.components.SocialLoginButtons
import com.apkprevention.auth.ui.theme.DarkBackground
import com.apkprevention.auth.ui.theme.ErrorRed
import com.apkprevention.auth.ui.theme.GreenDark
import com.apkprevention.auth.ui.theme.GreenLight
import com.apkprevention.auth.ui.theme.GreenPrimary
import com.apkprevention.auth.ui.theme.InputBorder
import com.apkprevention.auth.ui.theme.TextPrimary
import com.apkprevention.auth.ui.theme.TextSecondary
import com.apkprevention.auth.ui.theme.TextTertiary
import com.apkprevention.auth.ui.viewmodel.AuthViewModel

@Composable
fun RegistrationScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegistrationSuccess: (phone: String) -> Unit,
    onGoogleSignIn: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    var phoneForOtp by remember { mutableStateOf("") }

    LaunchedEffect(authState.registrationComplete) {
        if (authState.registrationComplete) {
            viewModel.clearRegistrationComplete()
            onRegistrationSuccess(phoneForOtp)
        }
    }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeTerms by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var valid = true

        nameError = when {
            fullName.isBlank() -> { valid = false; "Full name is required" }
            fullName.trim().length < 2 -> { valid = false; "Name must be at least 2 characters" }
            else -> null
        }
        emailError = when {
            email.isBlank() -> { valid = false; "Email is required" }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                { valid = false; "Please enter a valid email" }
            else -> null
        }
        phoneError = when {
            phone.isBlank() -> { valid = false; "Phone number is required" }
            phone.replace(Regex("[\\s-]"), "").length < 10 ->
                { valid = false; "Please enter a valid phone number" }
            else -> null
        }
        passwordError = when {
            password.isEmpty() -> { valid = false; "Password is required" }
            password.length < 8 -> { valid = false; "Password must be at least 8 characters" }
            else -> null
        }
        confirmError = when {
            confirmPassword.isEmpty() -> { valid = false; "Please confirm your password" }
            password != confirmPassword -> { valid = false; "Passwords do not match" }
            else -> null
        }
        termsError = if (!agreeTerms) { valid = false; "You must agree to the Terms" } else null

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
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "APK Prevention",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Create your secure account",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Social login
            SocialLoginButtons(
                onGoogleClick = onGoogleSignIn
            )

            Spacer(modifier = Modifier.height(20.dp))

            DividerWithText(text = "or register with email")

            Spacer(modifier = Modifier.height(20.dp))

            // Form fields
            AuthTextField(
                value = fullName,
                onValueChange = { fullName = it; nameError = null },
                placeholder = "Full Name",
                leadingIcon = Icons.Outlined.Person,
                error = nameError,
                maxLength = 100
            )
            Spacer(modifier = Modifier.height(12.dp))

            AuthTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                placeholder = "Email Address",
                leadingIcon = Icons.Outlined.Email,
                error = emailError,
                keyboardType = KeyboardType.Email,
                maxLength = 254
            )
            Spacer(modifier = Modifier.height(12.dp))

            AuthTextField(
                value = phone,
                onValueChange = { phone = it; phoneError = null },
                placeholder = "Phone Number",
                leadingIcon = Icons.Outlined.Phone,
                error = phoneError,
                keyboardType = KeyboardType.Phone,
                maxLength = 20
            )
            Spacer(modifier = Modifier.height(12.dp))

            AuthTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                placeholder = "Password",
                leadingIcon = Icons.Outlined.Lock,
                error = passwordError,
                isPassword = true,
                maxLength = 128
            )
            Spacer(modifier = Modifier.height(6.dp))

            PasswordStrengthIndicator(password = password)

            Spacer(modifier = Modifier.height(6.dp))

            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmError = null },
                placeholder = "Confirm Password",
                leadingIcon = Icons.Outlined.Lock,
                error = confirmError,
                isPassword = true,
                imeAction = ImeAction.Done,
                maxLength = 128
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Terms checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = agreeTerms,
                    onCheckedChange = { agreeTerms = it; termsError = null },
                    colors = CheckboxDefaults.colors(
                        checkedColor = GreenPrimary,
                        uncheckedColor = if (termsError != null) ErrorRed else InputBorder,
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextTertiary)) {
                            append("I agree to the ")
                        }
                        withStyle(SpanStyle(color = GreenLight, fontWeight = FontWeight.Medium)) {
                            append("Terms of Service")
                        }
                        withStyle(SpanStyle(color = TextTertiary)) {
                            append(" and ")
                        }
                        withStyle(SpanStyle(color = GreenLight, fontWeight = FontWeight.Medium)) {
                            append("Privacy Policy")
                        }
                    },
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }

            if (termsError != null) {
                Text(
                    text = termsError!!,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Register button
            Button(
                onClick = {
                    if (validate()) {
                        phoneForOtp = phone
                        viewModel.registerWithEmail(email, password, fullName, phone)
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
                                text = "Create Secure Account",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Error message from Firebase
            if (authState.error != null) {
                Text(
                    text = authState.error!!,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = "Sign In",
                    color = GreenLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
