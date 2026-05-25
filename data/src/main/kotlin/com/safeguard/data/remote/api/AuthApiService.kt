package com.safeguard.data.remote.api

import com.safeguard.data.remote.dto.auth.AuthResponse
import com.safeguard.data.remote.dto.auth.LoginRequest
import com.safeguard.data.remote.dto.auth.OAuthGoogleRequest
import com.safeguard.data.remote.dto.auth.OtpResponse
import com.safeguard.data.remote.dto.auth.OtpSendRequest
import com.safeguard.data.remote.dto.auth.OtpVerifyRequest
import com.safeguard.data.remote.dto.auth.RegisterRequest
import com.safeguard.data.remote.dto.auth.ResetPasswordConfirmRequest
import com.safeguard.data.remote.dto.auth.ResetPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: OtpSendRequest): Response<OtpResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<AuthResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<OtpResponse>

    @POST("auth/reset-password/confirm")
    suspend fun confirmResetPassword(@Body request: ResetPasswordConfirmRequest): Response<OtpResponse>

    @POST("auth/oauth/google")
    suspend fun oauthGoogle(@Body request: OAuthGoogleRequest): Response<AuthResponse>
}
