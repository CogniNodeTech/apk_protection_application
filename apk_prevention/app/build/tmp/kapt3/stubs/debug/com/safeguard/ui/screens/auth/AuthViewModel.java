package com.safeguard.ui.screens.auth;

import androidx.lifecycle.ViewModel;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import com.safeguard.data.remote.dto.auth.AuthResponse;
import com.safeguard.data.repository.AuthRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;
import kotlinx.coroutines.flow.StateFlow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u000b\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u000e\u001a\u00020\u000fJ\u0006\u0010\u0010\u001a\u00020\u000fJ\u000e\u0010\u0011\u001a\u00020\u000f2\u0006\u0010\u0012\u001a\u00020\u0013J\u0016\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0015\u001a\u00020\u00132\u0006\u0010\u0016\u001a\u00020\u0013J\u0016\u0010\u0017\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u00132\u0006\u0010\u0019\u001a\u00020\u0013J\u0018\u0010\u001a\u001a\u00020\u000f2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001eH\u0002J&\u0010\u001f\u001a\u00020\u000f2\u0006\u0010 \u001a\u00020\u00132\u0006\u0010\u0018\u001a\u00020\u00132\u0006\u0010!\u001a\u00020\u00132\u0006\u0010\u0019\u001a\u00020\u0013J\u000e\u0010\"\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u0013J\u000e\u0010#\u001a\u00020\u000f2\u0006\u0010!\u001a\u00020\u0013J\u0010\u0010$\u001a\u00020\u000f2\b\u0010%\u001a\u0004\u0018\u00010\u0013J\u0006\u0010&\u001a\u00020\u000fJ\u0016\u0010\'\u001a\u00020\u000f2\u0006\u0010!\u001a\u00020\u00132\u0006\u0010(\u001a\u00020\u0013R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006)"}, d2 = {"Lcom/safeguard/ui/screens/auth/AuthViewModel;", "Landroidx/lifecycle/ViewModel;", "prefs", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "authRepository", "Lcom/safeguard/data/repository/AuthRepository;", "(Lcom/safeguard/data/local/preferences/SecurePreferencesManager;Lcom/safeguard/data/repository/AuthRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/safeguard/ui/screens/auth/AuthUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearError", "", "clearOAuthLoading", "completeGoogleSignIn", "idToken", "", "confirmPasswordReset", "token", "newPassword", "login", "email", "password", "persistSession", "response", "Lcom/safeguard/data/remote/dto/auth/AuthResponse;", "requireOnboarding", "", "register", "fullName", "phone", "requestPasswordReset", "sendOtp", "setOAuthError", "message", "syncWithPersistedSession", "verifyOtp", "code", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class AuthViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.preferences.SecurePreferencesManager prefs = null;
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.safeguard.ui.screens.auth.AuthUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.auth.AuthUiState> uiState = null;
    
    @javax.inject.Inject
    public AuthViewModel(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager prefs, @org.jetbrains.annotations.NotNull
    com.safeguard.data.repository.AuthRepository authRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.safeguard.ui.screens.auth.AuthUiState> getUiState() {
        return null;
    }
    
    public final void clearError() {
    }
    
    /**
     * AuthFlow can be shown after logout while this ViewModel instance still exists.
     * Re-sync with persisted prefs so stale in-memory auth state cannot auto-navigate.
     */
    public final void syncWithPersistedSession() {
    }
    
    private final void persistSession(com.safeguard.data.remote.dto.auth.AuthResponse response, boolean requireOnboarding) {
    }
    
    public final void register(@org.jetbrains.annotations.NotNull
    java.lang.String fullName, @org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String phone, @org.jetbrains.annotations.NotNull
    java.lang.String password) {
    }
    
    public final void login(@org.jetbrains.annotations.NotNull
    java.lang.String email, @org.jetbrains.annotations.NotNull
    java.lang.String password) {
    }
    
    public final void requestPasswordReset(@org.jetbrains.annotations.NotNull
    java.lang.String email) {
    }
    
    public final void confirmPasswordReset(@org.jetbrains.annotations.NotNull
    java.lang.String token, @org.jetbrains.annotations.NotNull
    java.lang.String newPassword) {
    }
    
    public final void sendOtp(@org.jetbrains.annotations.NotNull
    java.lang.String phone) {
    }
    
    public final void verifyOtp(@org.jetbrains.annotations.NotNull
    java.lang.String phone, @org.jetbrains.annotations.NotNull
    java.lang.String code) {
    }
    
    public final void completeGoogleSignIn(@org.jetbrains.annotations.NotNull
    java.lang.String idToken) {
    }
    
    public final void setOAuthError(@org.jetbrains.annotations.Nullable
    java.lang.String message) {
    }
    
    public final void clearOAuthLoading() {
    }
}