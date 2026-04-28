package com.safeguard.data.repository;

import com.safeguard.data.remote.api.AuthApiService;
import com.safeguard.data.remote.dto.auth.AuthResponse;
import com.safeguard.data.remote.dto.auth.LoginRequest;
import com.safeguard.data.remote.dto.auth.OAuthAppleRequest;
import com.safeguard.data.remote.dto.auth.OAuthFacebookRequest;
import com.safeguard.data.remote.dto.auth.OAuthGoogleRequest;
import com.safeguard.data.remote.dto.auth.OtpResponse;
import com.safeguard.data.remote.dto.auth.OtpSendRequest;
import com.safeguard.data.remote.dto.auth.OtpVerifyRequest;
import com.safeguard.data.remote.dto.auth.RegisterRequest;
import com.safeguard.data.remote.dto.auth.ResetPasswordRequest;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.json.JSONObject;
import retrofit2.Response;

@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\n\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J,\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u000b\u0010\fJ\u0018\u0010\r\u001a\u00060\u000ej\u0002`\u000f2\n\u0010\u0010\u001a\u00060\u000ej\u0002`\u000fH\u0002J@\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0015\u0010\u0016J$\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\u0018\u001a\u00020\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0019\u0010\u001aJ$\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\u001c\u001a\u00020\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001d\u0010\u001aJ\u0014\u0010\u001e\u001a\u00020\t2\n\u0010\u001f\u001a\u0006\u0012\u0002\b\u00030 H\u0002J<\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\"\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\t2\u0006\u0010#\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b$\u0010%J$\u0010&\u001a\b\u0012\u0004\u0012\u00020\'0\u00062\u0006\u0010\b\u001a\u00020\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b(\u0010\u001aJ@\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\"\u0010*\u001a\u001e\b\u0001\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070 0,\u0012\u0006\u0012\u0004\u0018\u00010\u00010+H\u0082@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b-\u0010.J@\u0010/\u001a\b\u0012\u0004\u0012\u00020\'0\u00062\"\u0010*\u001a\u001e\b\u0001\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\'0 0,\u0012\u0006\u0012\u0004\u0018\u00010\u00010+H\u0082@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b0\u0010.J$\u00101\u001a\b\u0012\u0004\u0012\u00020\'0\u00062\u0006\u0010#\u001a\u00020\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b2\u0010\u001aJ,\u00103\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010#\u001a\u00020\t2\u0006\u00104\u001a\u00020\tH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b5\u0010\fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u00066"}, d2 = {"Lcom/safeguard/data/repository/AuthRepository;", "", "api", "Lcom/safeguard/data/remote/api/AuthApiService;", "(Lcom/safeguard/data/remote/api/AuthApiService;)V", "login", "Lkotlin/Result;", "Lcom/safeguard/data/remote/dto/auth/AuthResponse;", "email", "", "password", "login-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "mapNetworkError", "Ljava/lang/Exception;", "Lkotlin/Exception;", "e", "oauthApple", "identityToken", "authorizationCode", "redirectUri", "oauthApple-BWLJW6A", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "oauthFacebook", "accessToken", "oauthFacebook-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "oauthGoogle", "idToken", "oauthGoogle-gIAlu-s", "parseErrorMessage", "response", "Lretrofit2/Response;", "register", "fullName", "phone", "register-yxL6bBk", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "resetPassword", "Lcom/safeguard/data/remote/dto/auth/OtpResponse;", "resetPassword-gIAlu-s", "safeAuthCall", "call", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "safeAuthCall-gIAlu-s", "(Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "safeOtpCall", "safeOtpCall-gIAlu-s", "sendOtp", "sendOtp-gIAlu-s", "verifyOtp", "code", "verifyOtp-0E7RQCE", "data_release"})
public final class AuthRepository {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.remote.api.AuthApiService api = null;
    
    @javax.inject.Inject
    public AuthRepository(@org.jetbrains.annotations.NotNull
    com.safeguard.data.remote.api.AuthApiService api) {
        super();
    }
    
    private final java.lang.String parseErrorMessage(retrofit2.Response<?> response) {
        return null;
    }
    
    private final java.lang.Exception mapNetworkError(java.lang.Exception e) {
        return null;
    }
}