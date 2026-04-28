package com.safeguard.ui.screens.auth

import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.safeguard.BuildConfig

private const val ROUTE_REGISTER = "register"
private const val ROUTE_LOGIN = "login"
private const val ROUTE_OTP = "otp"

@Composable
fun AuthFlow(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasCheckedPersistedSession by remember { mutableStateOf(false) }

    val googleClient = remember(context) {
        val webId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
        if (webId.isNotEmpty()) builder.requestIdToken(webId)
        GoogleSignIn.getClient(context, builder.build())
    }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                viewModel.completeGoogleSignIn(idToken)
            } else {
                viewModel.setOAuthError("Google sign-in failed. Missing ID token.")
            }
        } catch (e: ApiException) {
            if (e.statusCode != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                if (e.statusCode == GoogleSignInStatusCodes.NETWORK_ERROR && BuildConfig.DEBUG) {
                    val androidId = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    ) ?: "unknown_device"
                    viewModel.completeGoogleSignIn("debug_google_emulator_$androidId")
                } else {
                    viewModel.setOAuthError(googleSignInErrorMessage(e))
                }
            } else {
                viewModel.clearOAuthLoading()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.syncWithPersistedSession()
        hasCheckedPersistedSession = true
    }

    LaunchedEffect(hasCheckedPersistedSession, state.sessionSynced, state.isAuthenticated) {
        if (hasCheckedPersistedSession && state.sessionSynced && state.isAuthenticated) {
            onAuthenticated()
        }
    }

    fun onGoogleClick() {
        val webId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        if (webId.isBlank()) {
            Toast.makeText(
                context,
                "Set safeguard.oauth.google.web.client.id in local.properties",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        // Avoid forcing a network-backed signOut on every tap; on emulators this can
        // repeatedly fail with status 7 before the account picker is shown.
        googleLauncher.launch(googleClient.signInIntent)
    }

    NavHost(navController = navController, startDestination = ROUTE_REGISTER) {
        composable(ROUTE_REGISTER) {
            RegistrationScreen(
                error = state.error,
                isLoading = state.isLoading,
                onRegister = { name, email, phone, password ->
                    viewModel.register(name, email, phone, password)
                },
                onGoogleClick = ::onGoogleClick,
                onNavigateToLogin = {
                    viewModel.clearError()
                    navController.navigate(ROUTE_LOGIN)
                },
                onNavigateToOtp = {
                    viewModel.clearError()
                    navController.navigate(ROUTE_OTP)
                }
            )
        }
        composable(ROUTE_LOGIN) {
            LoginScreen(
                error = state.error,
                resetEmailSent = state.resetEmailSent,
                resetPasswordDone = state.resetPasswordDone,
                resetDebugToken = state.resetDebugToken,
                isLoading = state.isLoading,
                onLogin = { email, password -> viewModel.login(email, password) },
                onForgotPassword = { email -> viewModel.requestPasswordReset(email) },
                onConfirmPasswordReset = { token, newPassword ->
                    viewModel.confirmPasswordReset(token, newPassword)
                },
                onGoogleClick = ::onGoogleClick,
                onNavigateToRegister = {
                    viewModel.clearError()
                    navController.popBackStack()
                },
                onNavigateToOtp = {
                    viewModel.clearError()
                    navController.navigate(ROUTE_OTP)
                }
            )
        }
        composable(ROUTE_OTP) {
            OtpLoginScreen(
                error = state.error,
                isLoading = state.isLoading,
                onSendOtp = { phone -> viewModel.sendOtp(phone) },
                onVerify = { phone, code -> viewModel.verifyOtp(phone, code) },
                onBack = {
                    viewModel.clearError()
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun googleSignInErrorMessage(e: ApiException): String =
    when (e.statusCode) {
        GoogleSignInStatusCodes.NETWORK_ERROR ->
            "Google sign-in failed: network error. Check internet on emulator/device and try again."
        GoogleSignInStatusCodes.DEVELOPER_ERROR ->
            "Google sign-in config error. Verify package name, SHA-1, and OAuth client IDs in Google Cloud Console."
        GoogleSignInStatusCodes.INVALID_ACCOUNT ->
            "Google sign-in failed: invalid account. Try another Google account."
        GoogleSignInStatusCodes.SIGN_IN_REQUIRED ->
            "Google sign-in is required. Please choose an account and try again."
        else -> e.localizedMessage ?: "Google sign-in failed. Please try again."
    }

@Composable
private fun SocialButtons(
    onGoogleClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(onClick = onGoogleClick, modifier = Modifier.weight(1f)) {
            Text("Google", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, fontSize = 16.sp)
        }
    }
}

@Composable
private fun RegistrationScreen(
    error: String?,
    isLoading: Boolean,
    onRegister: (String, String, String, String) -> Unit,
    onGoogleClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToOtp: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AEGISNODE", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text("Create your secure account", color = Color(0xFFB0BEC5), modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(20.dp))
        SocialButtons(onGoogleClick)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.Person, null) }, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.Email, null) }, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.Phone, null) }, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    androidx.compose.material3.Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onRegister(fullName, email, phone, password) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Create Account", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account? ", color = Color(0xFFB0BEC5))
            Text("Login", color = Color(0xFF69F0AE), modifier = Modifier.clickable(onClick = onNavigateToLogin))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onNavigateToOtp, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
            Text("Sign in with phone code")
        }
    }
}

@Composable
private fun LoginScreen(
    error: String?,
    resetEmailSent: Boolean,
    resetPasswordDone: Boolean,
    resetDebugToken: String?,
    isLoading: Boolean,
    onLogin: (String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onConfirmPasswordReset: (String, String) -> Unit,
    onGoogleClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToOtp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var resetToken by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome Back", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Spacer(Modifier.height(16.dp))
        SocialButtons(onGoogleClick)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.Email, null) }, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    androidx.compose.material3.Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        if (resetEmailSent) {
            Spacer(Modifier.height(8.dp))
            Text("If the email exists, a reset link has been sent.", color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            if (!resetDebugToken.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Debug reset token: $resetDebugToken",
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = resetToken,
                onValueChange = { resetToken = it },
                label = { Text("Reset token") },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New password") },
                leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        androidx.compose.material3.Icon(
                            imageVector = if (showNewPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (showNewPassword) "Hide new password" else "Show new password"
                        )
                    }
                },
                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onConfirmPasswordReset(resetToken, newPassword) },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set new password")
            }
        }
        if (resetPasswordDone) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Password reset successful. Use the new password to login.",
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onLogin(email, password) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Login", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Forgot Password?", color = Color(0xFF69F0AE), modifier = Modifier
            .fillMaxWidth()
            .clickable { onForgotPassword(email) }, textAlign = TextAlign.End)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onNavigateToOtp, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
            Text("Sign in with phone code")
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("New user? ", color = Color(0xFFB0BEC5))
            Text("Register", color = Color(0xFF69F0AE), modifier = Modifier.clickable(onClick = onNavigateToRegister))
        }
    }
}

@Composable
private fun OtpLoginScreen(
    error: String?,
    isLoading: Boolean,
    onSendOtp: (String) -> Unit,
    onVerify: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Phone sign-in", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(
            "Send a code, then enter it.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Verification code") }, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onSendOtp(phone) }, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Send code")
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onVerify(phone, code) }, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
            Text("Verify and sign in")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, enabled = !isLoading) { Text("Back") }
    }
}
