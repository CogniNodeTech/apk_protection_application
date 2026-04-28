package com.safeguard.data.remote.api;

import com.safeguard.data.remote.dto.auth.AuthResponse;
import com.safeguard.data.remote.dto.auth.LoginRequest;
import com.safeguard.data.remote.dto.auth.OAuthGoogleRequest;
import com.safeguard.data.remote.dto.auth.OtpResponse;
import com.safeguard.data.remote.dto.auth.OtpSendRequest;
import com.safeguard.data.remote.dto.auth.OtpVerifyRequest;
import com.safeguard.data.remote.dto.auth.RegisterRequest;
import com.safeguard.data.remote.dto.auth.ResetPasswordConfirmRequest;
import com.safeguard.data.remote.dto.auth.ResetPasswordRequest;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u001e\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u001e\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u001e\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u0011J\u001e\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u001e\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0016H\u00a7@\u00a2\u0006\u0002\u0010\u0017J\u001e\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001a\u00a8\u0006\u001b"}, d2 = {"Lcom/safeguard/data/remote/api/AuthApiService;", "", "confirmResetPassword", "Lretrofit2/Response;", "Lcom/safeguard/data/remote/dto/auth/OtpResponse;", "request", "Lcom/safeguard/data/remote/dto/auth/ResetPasswordConfirmRequest;", "(Lcom/safeguard/data/remote/dto/auth/ResetPasswordConfirmRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "login", "Lcom/safeguard/data/remote/dto/auth/AuthResponse;", "Lcom/safeguard/data/remote/dto/auth/LoginRequest;", "(Lcom/safeguard/data/remote/dto/auth/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "oauthGoogle", "Lcom/safeguard/data/remote/dto/auth/OAuthGoogleRequest;", "(Lcom/safeguard/data/remote/dto/auth/OAuthGoogleRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "register", "Lcom/safeguard/data/remote/dto/auth/RegisterRequest;", "(Lcom/safeguard/data/remote/dto/auth/RegisterRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "resetPassword", "Lcom/safeguard/data/remote/dto/auth/ResetPasswordRequest;", "(Lcom/safeguard/data/remote/dto/auth/ResetPasswordRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendOtp", "Lcom/safeguard/data/remote/dto/auth/OtpSendRequest;", "(Lcom/safeguard/data/remote/dto/auth/OtpSendRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyOtp", "Lcom/safeguard/data/remote/dto/auth/OtpVerifyRequest;", "(Lcom/safeguard/data/remote/dto/auth/OtpVerifyRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public abstract interface AuthApiService {
    
    @retrofit2.http.POST(value = "auth/register")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object register(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.auth.RegisterRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.safeguard.data.remote.dto.auth.AuthResponse>> $completion);
    
    @retrofit2.http.POST(value = "auth/login")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object login(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.auth.LoginRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.safeguard.data.remote.dto.auth.AuthResponse>> $completion);
    
    @retrofit2.http.POST(value = "auth/send-otp")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object sendOtp(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.auth.OtpSendRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.safeguard.data.remote.dto.auth.OtpResponse>> $completion);
    
    @retrofit2.http.POST(value = "auth/verify-otp")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object verifyOtp(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.auth.OtpVerifyRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.safeguard.data.remote.dto.auth.AuthResponse>> $completion);
    
    @retrofit2.http.POST(value = "auth/reset-password")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object resetPassword(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.auth.ResetPasswordRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.safeguard.data.remote.dto.auth.OtpResponse>> $completion);
    
    @retrofit2.http.POST(value = "auth/reset-password/confirm")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object confirmResetPassword(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.auth.ResetPasswordConfirmRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.safeguard.data.remote.dto.auth.OtpResponse>> $completion);
    
    @retrofit2.http.POST(value = "auth/oauth/google")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object oauthGoogle(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.dto.auth.OAuthGoogleRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.safeguard.data.remote.dto.auth.AuthResponse>> $completion);
}