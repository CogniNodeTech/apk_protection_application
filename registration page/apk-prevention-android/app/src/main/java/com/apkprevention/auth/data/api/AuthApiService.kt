package com.apkprevention.auth.data.api

import com.apkprevention.auth.data.model.AuthResponse
import com.apkprevention.auth.data.model.LoginRequest
import com.apkprevention.auth.data.model.OAuthGoogleRequest
import com.apkprevention.auth.data.model.OtpResponse
import com.apkprevention.auth.data.model.OtpSendRequest
import com.apkprevention.auth.data.model.OtpVerifyRequest
import com.apkprevention.auth.data.model.RegisterRequest
import com.apkprevention.auth.data.model.ResetPasswordConfirmRequest
import com.apkprevention.auth.data.model.ResetPasswordRequest
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
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<OtpResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<OtpResponse>

    @POST("auth/reset-password/confirm")
    suspend fun confirmResetPassword(@Body request: ResetPasswordConfirmRequest): Response<OtpResponse>

    @POST("auth/oauth/google")
    suspend fun oauthGoogle(@Body request: OAuthGoogleRequest): Response<AuthResponse>
}
