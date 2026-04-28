package com.safeguard.ui.screens.auth;

import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ButtonDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.PasswordVisualTransformation;
import androidx.compose.ui.text.input.VisualTransformation;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.text.style.TextAlign;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.safeguard.BuildConfig;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000B\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\u001a \u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u00072\b\b\u0002\u0010\b\u001a\u00020\tH\u0007\u001a\u00a6\u0001\u0010\n\u001a\u00020\u00052\b\u0010\u000b\u001a\u0004\u0018\u00010\u00012\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\r2\b\u0010\u000f\u001a\u0004\u0018\u00010\u00012\u0006\u0010\u0010\u001a\u00020\r2\u0018\u0010\u0011\u001a\u0014\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00050\u00122\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00050\u00142\u0018\u0010\u0015\u001a\u0014\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00050\u00122\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00050\u00072\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00050\u00072\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007H\u0003\u001aV\u0010\u0019\u001a\u00020\u00052\b\u0010\u000b\u001a\u0004\u0018\u00010\u00012\u0006\u0010\u0010\u001a\u00020\r2\u0012\u0010\u001a\u001a\u000e\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00050\u00142\u0018\u0010\u001b\u001a\u0014\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00050\u00122\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007H\u0003\u001aj\u0010\u001d\u001a\u00020\u00052\b\u0010\u000b\u001a\u0004\u0018\u00010\u00012\u0006\u0010\u0010\u001a\u00020\r2$\u0010\u001e\u001a \u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00050\u001f2\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00050\u00072\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00050\u00072\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007H\u0003\u001a\u0016\u0010!\u001a\u00020\u00052\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007H\u0003\u001a\u0010\u0010\"\u001a\u00020\u00012\u0006\u0010#\u001a\u00020$H\u0002\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0002\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0003\u001a\u00020\u0001X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"ROUTE_LOGIN", "", "ROUTE_OTP", "ROUTE_REGISTER", "AuthFlow", "", "onAuthenticated", "Lkotlin/Function0;", "viewModel", "Lcom/safeguard/ui/screens/auth/AuthViewModel;", "LoginScreen", "error", "resetEmailSent", "", "resetPasswordDone", "resetDebugToken", "isLoading", "onLogin", "Lkotlin/Function2;", "onForgotPassword", "Lkotlin/Function1;", "onConfirmPasswordReset", "onGoogleClick", "onNavigateToRegister", "onNavigateToOtp", "OtpLoginScreen", "onSendOtp", "onVerify", "onBack", "RegistrationScreen", "onRegister", "Lkotlin/Function4;", "onNavigateToLogin", "SocialButtons", "googleSignInErrorMessage", "e", "Lcom/google/android/gms/common/api/ApiException;", "app_debug"})
public final class AuthFlowKt {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String ROUTE_REGISTER = "register";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String ROUTE_LOGIN = "login";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String ROUTE_OTP = "otp";
    
    @androidx.compose.runtime.Composable
    public static final void AuthFlow(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onAuthenticated, @org.jetbrains.annotations.NotNull
    com.safeguard.ui.screens.auth.AuthViewModel viewModel) {
    }
    
    private static final java.lang.String googleSignInErrorMessage(com.google.android.gms.common.api.ApiException e) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void SocialButtons(kotlin.jvm.functions.Function0<kotlin.Unit> onGoogleClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RegistrationScreen(java.lang.String error, boolean isLoading, kotlin.jvm.functions.Function4<? super java.lang.String, ? super java.lang.String, ? super java.lang.String, ? super java.lang.String, kotlin.Unit> onRegister, kotlin.jvm.functions.Function0<kotlin.Unit> onGoogleClick, kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToLogin, kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToOtp) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void LoginScreen(java.lang.String error, boolean resetEmailSent, boolean resetPasswordDone, java.lang.String resetDebugToken, boolean isLoading, kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.String, kotlin.Unit> onLogin, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onForgotPassword, kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.String, kotlin.Unit> onConfirmPasswordReset, kotlin.jvm.functions.Function0<kotlin.Unit> onGoogleClick, kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToRegister, kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToOtp) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void OtpLoginScreen(java.lang.String error, boolean isLoading, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSendOtp, kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.String, kotlin.Unit> onVerify, kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
}