package com.apkprevention.auth.data.model

import com.google.gson.annotations.SerializedName

// --- Request DTOs ---

data class RegisterRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class OtpVerifyRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("code") val code: String
)

data class OtpSendRequest(
    @SerializedName("phone") val phone: String
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String
)

data class ResetPasswordConfirmRequest(
    @SerializedName("token") val token: String,
    @SerializedName("newPassword") val newPassword: String
)

data class OAuthGoogleRequest(
    @SerializedName("idToken") val idToken: String
)

// --- Response DTOs ---

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String? = null,
    @SerializedName("user") val user: UserDto? = null
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = null
)

data class OtpResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    /** Present only in non-production API responses when SMS is not wired; use for dev/testing only. */
    @SerializedName("debugOtp") val debugOtp: String? = null
)
