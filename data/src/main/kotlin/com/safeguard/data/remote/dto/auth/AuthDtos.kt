package com.safeguard.data.remote.dto.auth

import com.squareup.moshi.Json

data class RegisterRequest(
    @Json(name = "fullName") val fullName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "password") val password: String
)

data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

data class OtpSendRequest(
    @Json(name = "phone") val phone: String
)

data class OtpVerifyRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "code") val code: String
)

data class ResetPasswordRequest(
    @Json(name = "email") val email: String
)

data class ResetPasswordConfirmRequest(
    @Json(name = "token") val token: String,
    @Json(name = "newPassword") val newPassword: String
)

data class OAuthGoogleRequest(
    @Json(name = "idToken") val idToken: String
)

data class AuthResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String,
    @Json(name = "token") val token: String? = null,
    @Json(name = "user") val user: AuthUserDto? = null
)

data class AuthUserDto(
    @Json(name = "id") val id: String,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String? = null
)

data class OtpResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String,
    @Json(name = "debugResetToken") val debugResetToken: String? = null
)
