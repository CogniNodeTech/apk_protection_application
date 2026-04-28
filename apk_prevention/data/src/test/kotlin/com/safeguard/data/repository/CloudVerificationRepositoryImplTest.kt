package com.safeguard.data.repository

import com.safeguard.core.domain.repository.DeviceCloudMetadata
import com.safeguard.core.domain.repository.LocalLayerScores
import com.safeguard.data.remote.api.ThreatIntelligenceApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Integration-style JVM test: verifies Retrofit + Moshi + [CloudVerificationRepositoryImpl] against a mock HTTP server.
 */
class CloudVerificationRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: CloudVerificationRepositoryImpl

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val api = retrofit.create(ThreatIntelligenceApi::class.java)
        repository = CloudVerificationRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun verify_mapsJsonResponse_toDomain() = runBlocking {
        val body = """
            {
              "verdict": "SUSPICIOUS",
              "confidence": 0.82,
              "threat_name": "TestThreat",
              "threat_family": "TestFamily",
              "av_detections": 3,
              "total_av_scanned": 60,
              "community_reports": 1,
              "virustotal_link": null,
              "evidence": ["hash match"],
              "recommendation": "WARN_USER"
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val response = repository.verify(
            sha256 = "a".repeat(64),
            sha512 = "b".repeat(128),
            packageName = "com.example.app",
            versionCode = 1,
            permissions = listOf("INTERNET"),
            fileSize = 1000L,
            targetSdk = 34,
            signatureFingerprint = "ab",
            localLayerScores = LocalLayerScores(
                layer2HashResult = "UNKNOWN",
                layer3PermissionScore = 10,
                layer4SignatureScore = 20,
                layer5MlProbability = 0.5
            ),
            deviceMetadata = DeviceCloudMetadata(androidVersion = 34, deviceLocale = "en-US")
        )

        assertEquals("SUSPICIOUS", response.verdict)
        assertEquals(0.82f, response.confidence, 0.001f)
        assertEquals("TestThreat", response.threatName)
        assertEquals("hash match", response.evidence.first())
    }

    @Test
    fun verify_http4xx_doesNotRetryClientError() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("{}"))
        val response = repository.verify(
            sha256 = "a".repeat(64),
            sha512 = "b".repeat(128),
            packageName = "com.example.app",
            versionCode = 1,
            permissions = emptyList(),
            fileSize = 1L,
            targetSdk = 34,
            signatureFingerprint = null,
            localLayerScores = LocalLayerScores("SAFE", 0, 0, 0.1),
            deviceMetadata = DeviceCloudMetadata(34, "en")
        )
        assertEquals("UNKNOWN", response.verdict)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun verify_http429_retriesThenSucceeds() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "0")
                .setBody("{}")
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "0")
                .setBody("{}")
        )

        val body = """
            {
              "verdict": "MALICIOUS",
              "confidence": 0.91,
              "threat_name": "TestThreat",
              "threat_family": "TestFamily",
              "av_detections": 10,
              "total_av_scanned": 20,
              "community_reports": 2,
              "virustotal_link": null,
              "evidence": ["ok"],
              "recommendation": "WARN_USER"
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val response = repository.verify(
            sha256 = "a".repeat(64),
            sha512 = "b".repeat(128),
            packageName = "com.example.app",
            versionCode = 1,
            permissions = emptyList(),
            fileSize = 1L,
            targetSdk = 34,
            signatureFingerprint = null,
            localLayerScores = LocalLayerScores("SAFE", 0, 0, 0.1),
            deviceMetadata = DeviceCloudMetadata(34, "en")
        )

        assertEquals("MALICIOUS", response.verdict)
        assertEquals(3, server.requestCount)
    }
}
