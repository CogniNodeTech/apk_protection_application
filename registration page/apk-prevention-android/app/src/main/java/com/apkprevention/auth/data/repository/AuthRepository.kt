package com.apkprevention.auth.data.repository

import com.apkprevention.auth.data.api.AuthApiService
import com.apkprevention.auth.data.model.AuthResponse
import com.apkprevention.auth.data.model.LoginRequest
import com.apkprevention.auth.data.model.OAuthGoogleRequest
import com.apkprevention.auth.data.model.OtpResponse
import com.apkprevention.auth.data.model.OtpSendRequest
import com.apkprevention.auth.data.model.OtpVerifyRequest
import com.apkprevention.auth.data.model.RegisterRequest
import com.apkprevention.auth.data.model.ResetPasswordConfirmRequest
import com.apkprevention.auth.data.model.ResetPasswordRequest
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApiService
) {
    private val gson = Gson()

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Result<AuthResponse> {
        return safeApiCall {
            api.register(RegisterRequest(fullName, email.trim(), phone, password))
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return safeApiCall {
            api.login(LoginRequest(email.trim(), password))
        }
    }

    suspend fun sendOtp(phone: String): Result<OtpResponse> {
        return safeOtpCall { api.sendOtp(OtpSendRequest(phone)) }
    }

    suspend fun verifyOtp(phone: String, code: String): Result<OtpResponse> {
        return safeOtpCall { api.verifyOtp(OtpVerifyRequest(phone, code)) }
    }

    suspend fun resetPassword(email: String): Result<OtpResponse> {
        return safeOtpCall { api.resetPassword(ResetPasswordRequest(email.trim())) }
    }

    suspend fun confirmResetPassword(token: String, newPassword: String): Result<OtpResponse> {
        return safeOtpCall {
            api.confirmResetPassword(
                ResetPasswordConfirmRequest(
                    token = token.trim(),
                    newPassword = newPassword
                )
            )
        }
    }

    suspend fun oauthGoogle(idToken: String): Result<AuthResponse> {
        return safeApiCall { api.oauthGoogle(OAuthGoogleRequest(idToken)) }
    }

    private suspend fun safeApiCall(
        call: suspend () -> Response<AuthResponse>
    ): Result<AuthResponse> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) Result.success(body)
                else Result.failure(Exception(body.message))
            } else {
                Result.failure(Exception(parseErrorBody(response)))
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkError(e))
        }
    }

    private suspend fun safeOtpCall(
        call: suspend () -> Response<OtpResponse>
    ): Result<OtpResponse> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) Result.success(body)
                else Result.failure(Exception(body.message))
            } else {
                Result.failure(Exception(parseErrorBody(response)))
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkError(e))
        }
    }

    private fun <T> parseErrorBody(response: Response<T>): String {
        val code = response.code()
        val raw = response.errorBody()?.string()

        val serverMessage = try {
            raw?.let { gson.fromJson(it, ErrorBody::class.java)?.message }
        } catch (_: JsonSyntaxException) {
            null
        }

        return serverMessage ?: when (code) {
            400 -> "Invalid request. Check your input."
            401 -> "Invalid email or password."
            403 -> "Access denied."
            409 -> "This email is already registered."
            429 -> "Too many attempts. Try again later."
            503 -> "Service temporarily unavailable. Try again later."
            in 500..599 -> "Server error. Please try again later."
            else -> "Request failed ($code)"
        }
    }

    private fun mapNetworkError(e: Exception): Exception {
        val msg = e.message ?: ""
        val emulatorHint =
            "Android emulator: use http://10.0.2.2:3001/api/ in debug and run the Node server (npm start in server/)."
        return when (e) {
            is UnknownHostException ->
                Exception("Cannot connect to server. Check your connection and API URL ($emulatorHint)")
            is SocketTimeoutException ->
                Exception("Request timed out. Try again.")
            is ConnectException ->
                Exception(
                    "Cannot reach the auth server. Start the backend on port 3001, or $emulatorHint"
                )
            else -> when {
                msg.contains("Unable to resolve host", ignoreCase = true) ->
                    Exception("Cannot connect to server. Check your connection and API URL ($emulatorHint)")
                msg.contains("timeout", ignoreCase = true) ->
                    Exception("Request timed out. Try again.")
                msg.contains("Connection refused", ignoreCase = true) ||
                    msg.contains("Failed to connect", ignoreCase = true) ->
                    Exception("Server is not running or wrong port. $emulatorHint")
                msg.contains("SocketException", ignoreCase = true) ||
                    msg.contains("Network is unreachable", ignoreCase = true) ->
                    Exception("Network error. Check your connection.")
                else -> Exception(msg.ifBlank { "Something went wrong" })
            }
        }
    }

    private data class ErrorBody(val success: Boolean?, val message: String?)
}
