package com.apkprevention.auth.navigation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apkprevention.auth.BuildConfig
import com.apkprevention.auth.ui.screens.LoginScreen
import com.apkprevention.auth.ui.screens.OtpVerificationScreen
import com.apkprevention.auth.ui.screens.RegistrationScreen
import com.apkprevention.auth.ui.screens.ResetPasswordScreen
import com.apkprevention.auth.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AuthRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "reset-password?email={email}"
    const val OTP = "otp/{phone}"

    fun otpRoute(phone: String): String {
        val encoded = URLEncoder.encode(phone, StandardCharsets.UTF_8.toString())
        return "otp/$encoded"
    }

    fun resetPasswordRoute(email: String): String {
        val encoded = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
        return "reset-password?email=$encoded"
    }
}

@Composable
fun AuthNavigation(
    onAuthSuccess: () -> Unit = {}
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    val googleClient = remember(context) {
        val webId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
        // Don't crash the app when the Web client ID isn't configured yet.
        if (webId.isNotEmpty()) {
            builder.requestIdToken(webId)
        }
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
                authViewModel.completeGoogleSignIn(idToken)
            } else {
                authViewModel.setOAuthError(
                    "Google sign-in did not return an ID token. Set GOOGLE_WEB_CLIENT_ID (Web client ID) in app build.gradle to match the server."
                )
            }
        } catch (e: ApiException) {
            if (e.statusCode != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                authViewModel.setOAuthError(e.message ?: "Google sign-in failed")
            } else {
                authViewModel.clearOAuthLoading()
            }
        }
    }

    fun onGoogleClick() {
        val webId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        if (webId.isEmpty()) {
            Toast.makeText(
                context,
                "Set GOOGLE_WEB_CLIENT_ID in app build.gradle (Web client ID, same as server GOOGLE_CLIENT_ID).",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        googleClient.signOut().addOnCompleteListener {
            googleLauncher.launch(googleClient.signInIntent)
        }
    }

    fun openDashboard() {
        onAuthSuccess()
    }

    NavHost(
        navController = navController,
        startDestination = AuthRoutes.REGISTER
    ) {
        composable(AuthRoutes.REGISTER) {
            LaunchedEffect(authState.isAuthenticated) {
                if (authState.isAuthenticated) {
                    openDashboard()
                }
            }

            RegistrationScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    authViewModel.clearError()
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(AuthRoutes.REGISTER) { inclusive = true }
                    }
                },
                onRegistrationSuccess = { phone ->
                    navController.navigate(AuthRoutes.otpRoute(phone))
                },
                onGoogleSignIn = ::onGoogleClick
            )
        }

        composable(AuthRoutes.LOGIN) {
            LaunchedEffect(authState.isAuthenticated) {
                if (authState.isAuthenticated) {
                    openDashboard()
                }
            }

            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    authViewModel.clearError()
                    navController.navigate(AuthRoutes.REGISTER) {
                        popUpTo(AuthRoutes.LOGIN) { inclusive = true }
                    }
                },
                onForgotPassword = { email ->
                    authViewModel.clearError()
                    authViewModel.clearResetEmailSent()
                    authViewModel.clearResetPasswordDone()
                    navController.navigate(AuthRoutes.resetPasswordRoute(email))
                },
                onGoogleSignIn = ::onGoogleClick
            )
        }

        composable(
            route = AuthRoutes.RESET_PASSWORD,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val encodedEmail = backStackEntry.arguments?.getString("email") ?: ""
            val initialEmail = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())
            ResetPasswordScreen(
                viewModel = authViewModel,
                initialEmail = initialEmail,
                onBack = {
                    authViewModel.clearError()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AuthRoutes.OTP,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rawPhone = backStackEntry.arguments?.getString("phone") ?: ""
            val phone = URLDecoder.decode(rawPhone, StandardCharsets.UTF_8.toString())

            LaunchedEffect(phone) {
                authViewModel.clearError()
                authViewModel.sendOtp(phone)
            }

            LaunchedEffect(authState.isAuthenticated) {
                if (authState.isAuthenticated) {
                    openDashboard()
                }
            }

            OtpVerificationScreen(
                phoneNumber = phone,
                devOtpHint = if (BuildConfig.DEBUG) authState.lastDevOtp else null,
                onVerify = { code ->
                    authViewModel.verifyOtp(phone, code)
                },
                onResend = {
                    authViewModel.sendOtp(phone)
                },
                onBack = {
                    authViewModel.clearLastDevOtp()
                    navController.popBackStack()
                },
                isLoading = authState.isLoading,
                error = authState.error
            )
        }

    }
}
