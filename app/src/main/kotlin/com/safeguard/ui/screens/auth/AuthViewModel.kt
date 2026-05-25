package com.safeguard.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.data.local.preferences.SecurePreferencesManager
import com.safeguard.data.remote.dto.auth.AuthResponse
import com.safeguard.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val resetEmailSent: Boolean = false,
    val sessionSynced: Boolean = false,
    val resetPasswordDone: Boolean = false,
    val resetDebugToken: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val prefs: SecurePreferencesManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    // AuthFlow should only navigate after an explicit auth action in this UI session.
    private val _uiState = MutableStateFlow(AuthUiState(isAuthenticated = false))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * AuthFlow can be shown after logout while this ViewModel instance still exists.
     * Re-sync with persisted prefs so stale in-memory auth state cannot auto-navigate.
     */
    fun syncWithPersistedSession() {
        val persistedAuth = prefs.isAuthenticated
        _uiState.update {
            it.copy(
                isAuthenticated = persistedAuth,
                isLoading = false,
                error = null,
                resetEmailSent = false,
                sessionSynced = true
            )
        }
    }

    private fun persistSession(response: AuthResponse, requireOnboarding: Boolean) {
        val token = response.token ?: return
        val user = response.user ?: return
        prefs.authAccessToken = token
        prefs.registeredEmail = user.email
        prefs.registeredFullName = user.fullName
        prefs.registeredPhone = user.phone
        prefs.registeredPassword = null
        prefs.isAuthenticated = true
        prefs.requireOnboardingAfterRegistration = requireOnboarding
    }

    fun register(fullName: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            val name = fullName.trim()
            val normalizedEmail = email.trim().lowercase()
            val phoneTrim = phone.trim()
            if (name.length < 2) {
                _uiState.update { it.copy(error = "Name must be at least 2 characters") }
                return@launch
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                _uiState.update { it.copy(error = "Enter a valid email address") }
                return@launch
            }
            if (phoneTrim.length < 5) {
                _uiState.update { it.copy(error = "Enter a valid phone number") }
                return@launch
            }
            if (password.length < 8) {
                _uiState.update { it.copy(error = "Password must be at least 8 characters") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.register(name, normalizedEmail, phoneTrim, password).fold(
                onSuccess = { r ->
                    persistSession(r, requireOnboarding = true)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            isAuthenticated = true,
                            sessionSynced = true
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val normalizedEmail = email.trim().lowercase()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                _uiState.update { it.copy(error = "Enter a valid email address") }
                return@launch
            }
            if (password.isEmpty()) {
                _uiState.update { it.copy(error = "Enter your password") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null, resetEmailSent = false) }
            authRepository.login(normalizedEmail, password).fold(
                onSuccess = { r ->
                    persistSession(r, requireOnboarding = false)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            isAuthenticated = true,
                            sessionSynced = true
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            val normalizedEmail = email.trim().lowercase()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                _uiState.update { it.copy(error = "Enter a valid email address", resetEmailSent = false) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null, resetEmailSent = false) }
            authRepository.resetPassword(normalizedEmail).fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            resetEmailSent = true,
                            resetPasswordDone = false,
                            resetDebugToken = response.debugResetToken
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message,
                            resetEmailSent = false,
                            resetPasswordDone = false
                        )
                    }
                }
            )
        }
    }

    fun confirmPasswordReset(token: String, newPassword: String) {
        viewModelScope.launch {
            val t = token.trim()
            if (t.length < 8) {
                _uiState.update { it.copy(error = "Enter a valid reset token") }
                return@launch
            }
            if (newPassword.length < 8) {
                _uiState.update { it.copy(error = "New password must be at least 8 characters") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null, resetPasswordDone = false) }
            authRepository.confirmResetPassword(t, newPassword).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            resetPasswordDone = true
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message, resetPasswordDone = false) }
                }
            )
        }
    }

    fun sendOtp(phone: String) {
        viewModelScope.launch {
            val p = phone.trim()
            if (p.length < 5) {
                _uiState.update { it.copy(error = "Enter a valid phone number") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendOtp(p).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun verifyOtp(phone: String, code: String) {
        viewModelScope.launch {
            val p = phone.trim()
            val c = code.trim()
            if (p.length < 5) {
                _uiState.update { it.copy(error = "Enter a valid phone number") }
                return@launch
            }
            if (c.length < 4) {
                _uiState.update { it.copy(error = "Enter the verification code") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.verifyOtp(p, c).fold(
                onSuccess = { r ->
                    if (r.token != null && r.user != null) {
                        persistSession(r, requireOnboarding = false)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                isAuthenticated = true,
                                sessionSynced = true
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = r.message) }
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun completeGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.oauthGoogle(idToken).fold(
                onSuccess = { r ->
                    persistSession(r, requireOnboarding = false)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            isAuthenticated = true,
                            sessionSynced = true
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun setOAuthError(message: String?) {
        _uiState.update { it.copy(isLoading = false, error = message) }
    }

    fun clearOAuthLoading() {
        _uiState.update { it.copy(isLoading = false) }
    }
}
