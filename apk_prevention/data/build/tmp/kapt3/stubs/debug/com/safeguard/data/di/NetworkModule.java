package com.safeguard.data.di;

import com.safeguard.data.remote.api.AuthApiService;
import com.safeguard.data.remote.api.ThreatIntelligenceApi;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;

@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\b\u0010\u0007\u001a\u00020\bH\u0007J4\u0010\t\u001a\u00020\n2\b\b\u0001\u0010\u000b\u001a\u00020\f2\n\b\u0001\u0010\r\u001a\u0004\u0018\u00010\f2\b\b\u0001\u0010\u000e\u001a\u00020\u000f2\n\b\u0001\u0010\u0010\u001a\u0004\u0018\u00010\fH\u0007J\"\u0010\u0011\u001a\u00020\u00062\u0006\u0010\u0012\u001a\u00020\n2\u0006\u0010\u0013\u001a\u00020\b2\b\b\u0001\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006\u0016"}, d2 = {"Lcom/safeguard/data/di/NetworkModule;", "", "()V", "provideAuthApiService", "Lcom/safeguard/data/remote/api/AuthApiService;", "retrofit", "Lretrofit2/Retrofit;", "provideMoshi", "Lcom/squareup/moshi/Moshi;", "provideOkHttpClient", "Lokhttp3/OkHttpClient;", "baseUrl", "", "certPin", "httpLoggingEnabled", "", "apiKey", "provideRetrofit", "client", "moshi", "provideThreatIntelligenceApi", "Lcom/safeguard/data/remote/api/ThreatIntelligenceApi;", "data_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class NetworkModule {
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.data.di.NetworkModule INSTANCE = null;
    
    private NetworkModule() {
        super();
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.squareup.moshi.Moshi provideMoshi() {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final okhttp3.OkHttpClient provideOkHttpClient(@javax.inject.Named(value = "base_url")
    @org.jetbrains.annotations.NotNull
    java.lang.String baseUrl, @javax.inject.Named(value = "cert_pin")
    @org.jetbrains.annotations.Nullable
    java.lang.String certPin, @javax.inject.Named(value = "http_logging_enabled")
    boolean httpLoggingEnabled, @javax.inject.Named(value = "api_key")
    @org.jetbrains.annotations.Nullable
    java.lang.String apiKey) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final retrofit2.Retrofit provideRetrofit(@org.jetbrains.annotations.NotNull
    okhttp3.OkHttpClient client, @org.jetbrains.annotations.NotNull
    com.squareup.moshi.Moshi moshi, @javax.inject.Named(value = "base_url")
    @org.jetbrains.annotations.NotNull
    java.lang.String baseUrl) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.api.ThreatIntelligenceApi provideThreatIntelligenceApi(@org.jetbrains.annotations.NotNull
    retrofit2.Retrofit retrofit) {
        return null;
    }
    
    @dagger.Provides
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.data.remote.api.AuthApiService provideAuthApiService(@org.jetbrains.annotations.NotNull
    retrofit2.Retrofit retrofit) {
        return null;
    }
}