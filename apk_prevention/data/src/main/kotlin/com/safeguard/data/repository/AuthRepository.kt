package com.safeguard.data.repository

import com.safeguard.data.remote.api.AuthApiService
import com.safeguard.data.remote.dto.auth.AuthResponse
import com.safeguard.data.remote.dto.auth.LoginRequest
import com.safeguard.data.remote.dto.auth.OAuthGoogleRequest
import com.safeguard.data.remote.dto.auth.OtpResponse
import com.safeguard.data.remote.dto.auth.OtpSendRequest
import com.safeguard.data.remote.dto.auth.OtpVerifyRequest
import com.safeguard.data.remote.dto.auth.RegisterRequest
import com.safeguard.data.remote.dto.auth.ResetPasswordConfirmRequest
import com.safeguard.data.remote.dto.auth.ResetPasswordRequest
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import retrofit2.Response

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApiService
) {

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Result<AuthResponse> =
        safeAuthCall { api.register(RegisterRequest(fullName, email.trim(), phone.trim(), password)) }

    suspend fun login(email: String, password: String): Result<AuthResponse> =
        safeAuthCall { api.login(LoginRequest(email.trim(), password)) }

    suspend fun sendOtp(phone: String): Result<OtpResponse> =
        safeOtpCall { api.sendOtp(OtpSendRequest(phone.trim())) }

    suspend fun verifyOtp(phone: String, code: String): Result<AuthResponse> =
        safeAuthCall { api.verifyOtp(OtpVerifyRequest(phone.trim(), code.trim())) }

    suspend fun resetPassword(email: String): Result<OtpResponse> =
        safeOtpCall { api.resetPassword(ResetPasswordRequest(email.trim())) }

    suspend fun confirmResetPassword(token: String, newPassword: String): Result<OtpResponse> =
        safeOtpCall { api.confirmResetPassword(ResetPasswordConfirmRequest(token.trim(), newPassword)) }

    suspend fun oauthGoogle(idToken: String): Result<AuthResponse> =
        safeAuthCall { api.oauthGoogle(OAuthGoogleRequest(idToken.trim())) }

    private suspend fun safeAuthCall(
        call: suspend () -> Response<AuthResponse>
    ): Result<AuthResponse> =
        try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) Result.success(body)
                else Result.failure(Exception(body.message))
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkError(e))
        }

    private suspend fun safeOtpCall(
        call: suspend () -> Response<OtpResponse>
    ): Result<OtpResponse> =
        try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) Result.success(body)
                else Result.failure(Exception(body.message))
            } else {
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkError(e))
        }

    private fun parseErrorMessage(response: Response<*>): String {
        val code = response.code()
        val raw = response.errorBody()?.string()
        val serverMessage = raw?.let { json ->
            try {
                val j = JSONObject(json)
                when {
                    j.has("message") -> j.optString("message", "").takeIf { it.isNotBlank() }
                    j.has("detail") -> {
                        val d = j.get("detail")
                        when (d) {
                            is String -> d
                            else -> d.toString()
                        }
                    }
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }
        return serverMessage ?: when (code) {
            400 -> "Invalid request. Check your input."
            401 -> "Invalid email or password."
            403 -> "Access denied."
            404 -> "Not found."
            409 -> "This email is already registered."
            429 -> "Too many attempts. Try again later."
            in 500..599 -> "Server error. Please try again later."
            else -> "Request failed ($code)"
        }
    }

    private fun mapNetworkError(e: Exception): Exception {
        val msg = e.message ?: ""
        return when {
            msg.contains("Unable to resolve host", ignoreCase = true) ->
                Exception("Cannot connect to server. Check your connection and API URL (emulator: use 10.0.2.2).")
            msg.contains("timeout", ignoreCase = true) ->
                Exception("Request timed out. Try again.")
            msg.contains("Connection refused", ignoreCase = true) ->
                Exception("Server is not running. Start the backend or fix safeguard.api.base.url.")
            msg.contains("SocketException", ignoreCase = true) ->
                Exception("Network error. Check your connection.")
            else -> Exception(msg.ifBlank { "Something went wrong" })
        }
    }
}
