package com.apkprevention.auth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkprevention.auth.data.local.TokenManager
import com.apkprevention.auth.data.model.AuthResponse
import com.apkprevention.auth.data.model.UserDto
import com.apkprevention.auth.data.repository.AuthRepository
import com.apkprevention.auth.oauth.JwtPayloadParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val resetEmailSent: Boolean = false,
    val resetPasswordDone: Boolean = false,
    val registrationComplete: Boolean = false,
    val otpSent: Boolean = false,
    /** Shown on OTP screen when API returns debugOtp (development builds / no SMS). */
    val lastDevOtp: String? = null,
    val user: UserDto? = null,
    val token: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    fun registerWithEmail(email: String, password: String, name: String, phone: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            repository.register(name, email.trim(), phone, password)
                .onSuccess { response ->
                    response.token?.let { tokenManager.saveToken(it) }
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        registrationComplete = true,
                        user = response.user,
                        token = response.token
                    )
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                error = null,
                resetEmailSent = false
            )

            repository.login(email.trim(), password)
                .onSuccess { response ->
                    response.token?.let { tokenManager.saveToken(it) }
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = response.user,
                        token = response.token
                    )
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun sendOtp(phone: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            repository.sendOtp(phone)
                .onSuccess { response ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        otpSent = true,
                        lastDevOtp = response.debugOtp
                    )
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun verifyOtp(phone: String, code: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            repository.verifyOtp(phone, code)
                .onSuccess {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        lastDevOtp = null
                    )
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _authState.value = _authState.value.copy(
                error = "Enter your email first",
                resetEmailSent = false
            )
            return
        }
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                error = null,
                resetEmailSent = false
            )

            repository.resetPassword(email)
                .onSuccess {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        resetEmailSent = true,
                        resetPasswordDone = false
                    )
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message,
                        resetPasswordDone = false
                    )
                }
        }
    }

    fun confirmResetPassword(token: String, newPassword: String) {
        if (token.isBlank()) {
            _authState.value = _authState.value.copy(error = "Reset token is required")
            return
        }
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                error = null,
                resetPasswordDone = false
            )
            repository.confirmResetPassword(token, newPassword)
                .onSuccess {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        resetPasswordDone = true
                    )
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message,
                        resetPasswordDone = false
                    )
                }
        }
    }

    fun completeGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            repository.oauthGoogle(idToken)
                .onSuccess { applyOAuthSuccess(it) }
                .onFailure { e ->
                    _authState.value = _authState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun completeOAuthFromDeepLink(token: String?, error: String?) {
        if (error != null) {
            _authState.value = _authState.value.copy(isLoading = false, error = error)
            return
        }
        val t = token ?: return
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            tokenManager.saveToken(t)
            val user = JwtPayloadParser.parseUser(t)
            _authState.value = _authState.value.copy(
                isLoading = false,
                isAuthenticated = true,
                registrationComplete = false,
                user = user,
                token = t,
                error = null
            )
        }
    }

    fun setOAuthError(message: String?) {
        _authState.value = _authState.value.copy(isLoading = false, error = message)
    }

    fun clearOAuthLoading() {
        _authState.value = _authState.value.copy(isLoading = false)
    }

    private fun applyOAuthSuccess(response: AuthResponse) {
        response.token?.let { tokenManager.saveToken(it) }
        _authState.value = _authState.value.copy(
            isLoading = false,
            isAuthenticated = true,
            registrationComplete = false,
            user = response.user,
            token = response.token,
            error = null
        )
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun clearLastDevOtp() {
        _authState.value = _authState.value.copy(lastDevOtp = null)
    }

    fun clearRegistrationComplete() {
        _authState.value = _authState.value.copy(registrationComplete = false)
    }

    fun clearResetEmailSent() {
        _authState.value = _authState.value.copy(resetEmailSent = false)
    }

    fun clearResetPasswordDone() {
        _authState.value = _authState.value.copy(resetPasswordDone = false)
    }

    fun logout() {
        tokenManager.clearToken()
        _authState.value = AuthUiState()
    }

    fun resetState() {
        _authState.value = AuthUiState()
    }
}
