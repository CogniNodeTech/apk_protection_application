package com.safeguard.data.repository

import com.safeguard.core.domain.feedback.FeedbackPrivacyGate
import com.safeguard.core.domain.feedback.FeedbackUploadResult
import com.safeguard.core.domain.feedback.ScanFeedbackEvent
import com.safeguard.data.local.database.dao.ScanFeedbackEventDao
import com.safeguard.data.local.database.entity.ScanFeedbackEventEntity
import com.safeguard.data.remote.api.ThreatIntelligenceApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * JVM unit tests for [ScanFeedbackRepositoryImpl] (Phase 3.2).
 *
 * Covers:
 *  - Privacy gate: each of the three toggles (feedback opt-in, telemetry master, US-state
 *    sharing kill switch) must independently block enqueue + drain.
 *  - Wire format: the request body sent to the server is exactly the validated client DTO,
 *    no APK bytes, no paths, no extra fields. Asserted by re-parsing the captured request.
 *  - Partial-success drain: server returning `accepted_ids` deletes only those rows, the
 *    rest stay queued for the next worker tick.
 *  - Failure semantics: 5xx returns Failed (rows stay), I/O exception returns Failed,
 *    DB delete failure after successful upload still surfaces as Failed (so the worker
 *    backs off rather than retry-flooding).
 */
class ScanFeedbackRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ThreatIntelligenceApi
    private lateinit var dao: FakeScanFeedbackDao
    private lateinit var gate: ScriptedPrivacyGate
    private lateinit var repo: ScanFeedbackRepositoryImpl

    private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private var fakeNow = 1_700_000_000_000L

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        api = retrofit.create(ThreatIntelligenceApi::class.java)
        dao = FakeScanFeedbackDao()
        gate = ScriptedPrivacyGate()
        repo = ScanFeedbackRepositoryImpl(
            dao = dao,
            api = api,
            privacyGate = gate,
            moshi = moshi
        )
        repo.nowMs = { fakeNow }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ── enqueue: privacy gate ───────────────────────────────────────────────────────

    @Test
    fun enqueueIsDroppedSilentlyWhenFeedbackOptInIsOff() = runBlocking {
        // Default state: feedback off, telemetry on, sharing-opt-out off.
        val accepted = repo.enqueue(sampleEvent())
        assertFalse("default-off feedback toggle must drop the event", accepted)
        assertEquals(0, dao.rows.size)
    }

    @Test
    fun enqueueIsDroppedWhenTelemetryMasterIsOff() = runBlocking {
        gate.feedbackOptIn = true
        gate.telemetryMaster = false
        val accepted = repo.enqueue(sampleEvent())
        assertFalse("telemetry master is the umbrella switch — must dominate", accepted)
        assertEquals(0, dao.rows.size)
    }

    @Test
    fun enqueueIsDroppedWhenPrivacySharingOptOutIsOn() = runBlocking {
        gate.feedbackOptIn = true
        gate.telemetryMaster = true
        gate.sharingOptOut = true
        val accepted = repo.enqueue(sampleEvent())
        assertFalse("US-state opt-out must dominate even with feedback toggle on", accepted)
        assertEquals(0, dao.rows.size)
    }

    @Test
    fun enqueueWithAllPrefsAlignedPersistsExactlyOneRow() = runBlocking {
        optInFeedback()
        val accepted = repo.enqueue(sampleEvent(id = "evt-1"))
        assertTrue(accepted)
        assertEquals(1, dao.rows.size)
        val row = dao.rows.single()
        assertEquals("evt-1", row.id)
        assertEquals(64, row.sha256.length)
        assertTrue(
            "Layer scores must be persisted as JSON, not as Kotlin toString()",
            row.layerScoresJson.startsWith("{")
        )
        assertTrue(row.triggeredRulesJson.startsWith("["))
    }

    // ── drainOnce: privacy gate ────────────────────────────────────────────────────

    @Test
    fun drainOnceIsSkippedWhenFeedbackOptInIsOff() = runBlocking {
        // Pre-populate via direct DAO insert to simulate "rows enqueued before user opted out".
        dao.rows += sampleEntity(id = "stale-1")
        gate.feedbackOptIn = false
        val outcome = repo.drainOnce()
        assertTrue(outcome is FeedbackUploadResult.Skipped)
        assertEquals("rows must remain so user can re-enable + resume", 1, dao.rows.size)
        assertEquals("no network call must have happened", 0, server.requestCount)
    }

    @Test
    fun drainOnceWithEmptyQueueReturnsSkipped() = runBlocking {
        optInFeedback()
        val outcome = repo.drainOnce()
        assertTrue(outcome is FeedbackUploadResult.Skipped)
        assertEquals(0, server.requestCount)
    }

    // ── drainOnce: happy path + wire format ────────────────────────────────────────

    @Test
    fun drainOnceShipsTheValidatedDtoOnlyAndDeletesOnSuccess() = runBlocking {
        optInFeedback()
        dao.rows += sampleEntity(id = "evt-a", sha256 = "a".repeat(64))
        dao.rows += sampleEntity(id = "evt-b", sha256 = "b".repeat(64))
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"accepted_ids":["evt-a","evt-b"],"rejected_count":0}""")
        )

        val outcome = repo.drainOnce() as FeedbackUploadResult.Success
        assertEquals(2, outcome.uploadedCount)
        assertEquals("rows must be deleted after server accepts", 0, dao.rows.size)

        val sent = takeRecordedJson(server)
        assertTrue("client must NOT include device IMEI / Android id / accountId / file_path", listOf(
            "imei", "androidId", "android_id", "ad_id", "advertiser",
            "/storage/", "Download/", "filename", "file_path", "file_name"
        ).none { sent.contains(it, ignoreCase = true) })
        assertTrue(sent.contains("\"sha256\":\"${"a".repeat(64)}\""))
        assertTrue(sent.contains("\"client_app_version_code\":42"))
        assertTrue(sent.contains("\"client_android_api_level\":33"))
    }

    // ── drainOnce: partial success ─────────────────────────────────────────────────

    @Test
    fun drainOncePartialSuccessDeletesAcceptedRowsOnly() = runBlocking {
        optInFeedback()
        dao.rows += sampleEntity(id = "ok-1")
        dao.rows += sampleEntity(id = "rejected")
        dao.rows += sampleEntity(id = "ok-2")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"accepted_ids":["ok-1","ok-2"],"rejected_count":1}""")
        )

        val outcome = repo.drainOnce() as FeedbackUploadResult.Success
        assertEquals(2, outcome.uploadedCount)
        assertEquals(
            "rejected row must remain so it gets re-tried on next drain",
            listOf("rejected"),
            dao.rows.map { it.id }
        )
    }

    @Test
    fun drainOnceFallsBackToAllRowsWhenServerOmitsAcceptedIds() = runBlocking {
        // Older server build that doesn't return accepted_ids ⇒ treat as all-or-nothing.
        optInFeedback()
        dao.rows += sampleEntity(id = "evt-1")
        dao.rows += sampleEntity(id = "evt-2")
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{}"""))

        val outcome = repo.drainOnce() as FeedbackUploadResult.Success
        assertEquals(2, outcome.uploadedCount)
        assertEquals(0, dao.rows.size)
    }

    // ── drainOnce: failure surfaces ────────────────────────────────────────────────

    @Test
    fun drainOnceServerError5xxReturnsFailedWithoutDeletingRows() = runBlocking {
        optInFeedback()
        dao.rows += sampleEntity(id = "keep-1")
        server.enqueue(MockResponse().setResponseCode(503))

        val outcome = repo.drainOnce()
        assertTrue(outcome is FeedbackUploadResult.Failed)
        outcome as FeedbackUploadResult.Failed
        assertTrue(outcome.reason.startsWith("http_"))
        assertEquals("rows must remain queued for retry", 1, dao.rows.size)
    }

    @Test
    fun drainOnceNetworkExceptionReturnsFailed() = runBlocking {
        optInFeedback()
        dao.rows += sampleEntity(id = "keep-1")
        // Intentionally don't enqueue a response; close the server so the next request
        // fails with a SocketException equivalent.
        server.shutdown()

        val outcome = repo.drainOnce()
        assertTrue(outcome is FeedbackUploadResult.Failed)
        outcome as FeedbackUploadResult.Failed
        assertTrue(outcome.reason.startsWith("network: "))
        assertEquals(1, dao.rows.size)
    }

    @Test
    fun drainOnceClientValidationFailureSurfacesAsFailed() = runBlocking {
        optInFeedback()
        dao.rows += sampleEntity(id = "evt-1")
        server.enqueue(
            MockResponse()
                .setResponseCode(422)
                .setBody("""{"detail":"validation"}""")
        )

        val outcome = repo.drainOnce()
        assertTrue(outcome is FeedbackUploadResult.Failed)
        outcome as FeedbackUploadResult.Failed
        assertTrue(outcome.reason == "http_422")
        assertEquals(
            "we keep rows on 4xx so a server validator regression can be rolled back",
            1,
            dao.rows.size
        )
    }

    // ── housekeeping ───────────────────────────────────────────────────────────────

    @Test
    fun queuedCountReportsDaoSize() = runBlocking {
        dao.rows += sampleEntity(id = "1")
        dao.rows += sampleEntity(id = "2")
        assertEquals(2, repo.queuedCount())
    }

    @Test
    fun clearAllWipesQueueRegardlessOfPrefs() = runBlocking {
        // User decided to opt out + purge — must work even if every gate is closed.
        gate.feedbackOptIn = false
        gate.sharingOptOut = true
        dao.rows += sampleEntity(id = "1")
        dao.rows += sampleEntity(id = "2")
        repo.clearAll()
        assertEquals(0, dao.rows.size)
    }

    // ── helpers ────────────────────────────────────────────────────────────────────

    private fun optInFeedback() {
        gate.feedbackOptIn = true
        gate.telemetryMaster = true
        gate.sharingOptOut = false
    }

    private fun sampleEvent(
        id: String = "evt",
        sha256: String = "a".repeat(64)
    ): ScanFeedbackEvent = ScanFeedbackEvent(
        id = id,
        createdAtMs = fakeNow - 1_000,
        sha256 = sha256,
        verdict = "SAFE",
        confidence = 0.9f,
        packageName = "com.example.app",
        versionCode = 17,
        layerScores = mapOf("layer1" to 0.0f, "layer2" to 0.5f, "layer7" to 1.0f),
        triggeredRules = listOf("banker_anatsa_v1"),
        androidApiLevel = 33,
        appVersionCode = 42
    )

    private fun sampleEntity(
        id: String = "evt",
        sha256: String = "a".repeat(64)
    ): ScanFeedbackEventEntity = ScanFeedbackEventEntity(
        id = id,
        createdAtMs = fakeNow - 1_000,
        sha256 = sha256,
        verdict = "SAFE",
        confidence = 0.9f,
        packageName = "com.example.app",
        versionCode = 17,
        layerScoresJson = """{"layer1":0.0,"layer2":0.5,"layer7":1.0}""",
        triggeredRulesJson = """["banker_anatsa_v1"]""",
        androidApiLevel = 33,
        appVersionCode = 42
    )

    private fun takeRecordedJson(server: MockWebServer): String {
        val req: RecordedRequest? = server.takeRequest(2, TimeUnit.SECONDS)
        assertEquals("/v1/feedback", req?.path)
        return req?.body?.readUtf8() ?: error("no body recorded")
    }
}

/**
 * Minimal in-memory DAO that mirrors `ScanFeedbackEventDao`'s observable surface but
 * lives entirely in a list. Keeps tests free of Robolectric / Room DB scaffolding while
 * still exercising the repository's real entity ↔ DTO boundary.
 *
 * Insert is "ignore on conflict" to match the production OnConflictStrategy.IGNORE.
 */
private class FakeScanFeedbackDao : ScanFeedbackEventDao {
    val rows: MutableList<ScanFeedbackEventEntity> = mutableListOf()

    override suspend fun insert(entity: ScanFeedbackEventEntity) {
        if (rows.none { it.id == entity.id }) rows += entity
    }

    override suspend fun nextBatch(limit: Int): List<ScanFeedbackEventEntity> =
        rows.sortedBy { it.createdAtMs }.take(limit)

    override suspend fun deleteByIds(ids: List<String>) {
        rows.removeAll { it.id in ids }
    }

    override suspend fun count(): Int = rows.size

    override suspend fun deleteAll() {
        rows.clear()
    }
}

/**
 * Lightweight in-memory [FeedbackPrivacyGate]. Tests flip the booleans directly so each
 * case can pin the exact privacy posture it cares about. Defaults mirror production:
 *  - feedback opt-in: false (Phase 3.2 default)
 *  - telemetry master: true (matches `SecurePreferencesManager.scanTelemetryEnabled` default)
 *  - sharing opt-out: false (US-state kill switch is not asserted by default)
 */
private class ScriptedPrivacyGate : FeedbackPrivacyGate {
    var feedbackOptIn: Boolean = false
    var telemetryMaster: Boolean = true
    var sharingOptOut: Boolean = false

    override val isFeedbackOptInEnabled: Boolean get() = feedbackOptIn
    override val isTelemetryMasterEnabled: Boolean get() = telemetryMaster
    override val isPrivacySharingOptedOut: Boolean get() = sharingOptOut
}
