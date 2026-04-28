package com.safeguard.data.di

import com.safeguard.data.remote.api.AuthApiService
import com.safeguard.data.remote.api.ThreatIntelligenceApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @Named("base_url") baseUrl: String,
        @Named("cert_pin") certPin: String?,
        @Named("http_logging_enabled") httpLoggingEnabled: Boolean,
        @Named("api_key") apiKey: String?
    ): OkHttpClient {
        if (!httpLoggingEnabled && certPin.isNullOrBlank()) {
            throw IllegalStateException("Release network client requires certificate pinning.")
        }
        val logging = HttpLoggingInterceptor().apply {
            level = if (httpLoggingEnabled) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        val builder = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val original = chain.request()
                val path = original.url.encodedPath
                val requestBuilder: Request.Builder = original.newBuilder()
                    .header("X-Client-Version", "SafeGuard-Android")
                    .header("X-Request-Id", UUID.randomUUID().toString())
                // TI API key is for /v1/* only; /auth/* uses separate user sessions (JWT on device).
                if (!apiKey.isNullOrBlank() && path.contains("/v1/")) {
                    requestBuilder.header("Authorization", "Bearer $apiKey")
                }
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
        if (!certPin.isNullOrBlank()) {
            val host = try {
                java.net.URL(baseUrl).host
            } catch (_: Exception) {
                baseUrl.substringAfter("://").substringBefore("/")
            }
            if (host.isNotBlank()) {
                val pin = if (certPin.startsWith("sha256/")) certPin else "sha256/$certPin"
                builder.certificatePinner(
                    CertificatePinner.Builder()
                        .add(host, pin)
                        .build()
                )
            }
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        moshi: Moshi,
        @Named("base_url") baseUrl: String
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideThreatIntelligenceApi(retrofit: Retrofit): ThreatIntelligenceApi =
        retrofit.create(ThreatIntelligenceApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)
}
