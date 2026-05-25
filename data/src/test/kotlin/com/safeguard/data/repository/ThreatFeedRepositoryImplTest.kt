package com.safeguard.data.repository

import com.safeguard.core.domain.repository.ThreatFeedCursorStore
import com.safeguard.core.domain.repository.ThreatFeedStatus
import com.safeguard.core.domain.repository.ThreatFeedStatusStore
import com.safeguard.core.domain.repository.ThreatFeedSyncResult
import com.safeguard.data.local.database.dao.MalwareSignatureDao
import com.safeguard.data.local.database.entity.MalwareSignatureEntity
import com.safeguard.data.remote.api.ThreatIntelligenceApi
import com.safeguard.data.remote.signing.ThreatFeedSignatureVerifier
import com.safeguard.data.remote.signing.VerifiedThreatFeed
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * JVM unit tests for [ThreatFeedRepositoryImpl]. Exercise the full Retrofit + Moshi
 * pipeline against a [MockWebServer] so we also catch wire-format mismatches between
 * the server's `ThreatFeedResponse` (Pydantic) and the device's `ThreatFeedResponseJson`
 * (Moshi). Hand-rolled fakes for [MalwareSignatureDao] and [ThreatFeedCursorStore] keep
 * the test free of Android dependencies.
 */
class ThreatFeedRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ThreatIntelligenceApi
    private lateinit var dao: FakeMalwareDao
    private lateinit var cursor: InMemoryCursor
    private lateinit var statusStore: InMemoryStatusStore
    private lateinit var verifier: ScriptedSignatureVerifier
    private lateinit var repository: ThreatFeedRepositoryImpl
    /** Fixed wall-clock seam so we can assert exact `lastSuccessMs` / `lastAttemptMs` values. */
    private var fakeNow: Long = 1_700_000_000_000L

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        api = retrofit.create(ThreatIntelligenceApi::class.java)
        dao = FakeMalwareDao()
        cursor = InMemoryCursor()
        statusStore = InMemoryStatusStore()
        // Default verifier behaviour: signing disabled in this build, so every raw body is
        // accepted as unsigned. Individual tests scripting the Phase 3.1 signed path
        // override this via [verifier.script]. Tests that simulate "build pinned a key,
        // server returned bad signature" call [verifier.rejectAll].
        verifier = ScriptedSignatureVerifier()
        repository = ThreatFeedRepositoryImpl(
            api = api,
            malwareDao = dao,
            cursorStore = cursor,
            statusStore = statusStore,
            signatureVerifier = verifier,
            moshi = moshi
        )
        repository.nowMs = { fakeNow }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ── helpers ──────────────────────────────────────────────────────────────────────

    private fun feedJson(
        items: String,
        nextCursorMs: Long,
        hasMore: Boolean
    ): String = """
        {"items": [$items], "next_cursor_ms": $nextCursorMs, "has_more": $hasMore}
    """.trimIndent()

    private fun item(
        sha256: String = "a".repeat(64),
        sha512: String? = "b".repeat(128),
        fuzzy: String = "F".repeat(70),
        threatName: String = "Anatsa",
        threatFamily: String? = "Anatsa",
        severity: Int = 95,
        firstSeenMs: Long? = 1_700_000_000_000L,
        source: String? = "malwarebazaar"
    ): String = """
        {
          "sha256": "$sha256",
          "sha512": ${if (sha512 == null) "null" else "\"$sha512\""},
          "fuzzy_hash": "$fuzzy",
          "threat_name": "$threatName",
          "threat_family": ${if (threatFamily == null) "null" else "\"$threatFamily\""},
          "severity": $severity,
          "first_seen_ms": ${firstSeenMs ?: "null"},
          "source": ${if (source == null) "null" else "\"$source\""}
        }
    """.trimIndent()

    // ── tests ────────────────────────────────────────────────────────────────────────

    @Test
    fun firstSync_omitsSinceParam_andPersistsCursor() = runBlocking {
        val payload = feedJson(
            items = listOf(item()).joinToString(","),
            nextCursorMs = 2_000L,
            hasMore = false
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(payload))

        val result = repository.sync(batchLimit = 200, maxBatches = 5)

        // The fresh cursor was 0 → null → no `since` query parameter on the wire. Pinning
        // this prevents accidental regressions where `0` leaks through and the server
        // starts filtering the *first* sync against epoch zero (returning nothing).
        val recorded = server.takeRequest()
        val sentSince = recorded.requestUrl?.queryParameter("since")
        val sentLimit = recorded.requestUrl?.queryParameter("limit")
        assertNull("since must be omitted on first sync", sentSince)
        assertEquals("200", sentLimit)

        assertTrue(result is ThreatFeedSyncResult.Success)
        result as ThreatFeedSyncResult.Success
        assertEquals(1, result.insertedCount)
        assertEquals(1, result.batches)
        assertEquals(2_000L, result.cursorAdvancedTo)
        assertEquals(2_000L, cursor.cursorMs)
        assertEquals(1, dao.inserted.size)
        assertEquals("a".repeat(64), dao.inserted.single().sha256)
    }

    @Test
    fun subsequentSync_passesPersistedCursorAsSince() = runBlocking {
        cursor.cursorMs = 1_500L
        val payload = feedJson(
            items = listOf(item(sha256 = "c".repeat(64), fuzzy = "A".repeat(70))).joinToString(","),
            nextCursorMs = 3_000L,
            hasMore = false
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(payload))

        repository.sync()

        val recorded = server.takeRequest()
        assertEquals("1500", recorded.requestUrl?.queryParameter("since"))
        assertEquals(3_000L, cursor.cursorMs)
    }

    @Test
    fun pagination_followsHasMoreUntilExhausted() = runBlocking {
        // Three pages: hasMore=true, hasMore=true, hasMore=false. Each successive request
        // should pass the previous response's `next_cursor_ms` as `since`.
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                feedJson(
                    items = item(sha256 = "1".repeat(64), fuzzy = "A".repeat(70)),
                    nextCursorMs = 100L,
                    hasMore = true
                )
            )
        )
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                feedJson(
                    items = item(sha256 = "2".repeat(64), fuzzy = "B".repeat(70)),
                    nextCursorMs = 200L,
                    hasMore = true
                )
            )
        )
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                feedJson(
                    items = item(sha256 = "3".repeat(64), fuzzy = "C".repeat(70)),
                    nextCursorMs = 300L,
                    hasMore = false
                )
            )
        )

        val result = repository.sync(batchLimit = 1, maxBatches = 5) as ThreatFeedSyncResult.Success
        assertEquals(3, result.insertedCount)
        assertEquals(3, result.batches)
        assertEquals(300L, cursor.cursorMs)

        val first = server.takeRequest()
        val second = server.takeRequest()
        val third = server.takeRequest()
        assertNull(first.requestUrl?.queryParameter("since"))
        assertEquals("100", second.requestUrl?.queryParameter("since"))
        assertEquals("200", third.requestUrl?.queryParameter("since"))
    }

    @Test
    fun pagination_stopsAtMaxBatchesEvenIfServerStillReportsHasMore() = runBlocking {
        // Defensive: a misbehaving server that always sets `has_more: true` must not be
        // allowed to drain the device's network/battery. The cap is the user-provided
        // `maxBatches` argument.
        repeat(5) { idx ->
            val cursorMs = (idx + 1) * 100L
            server.enqueue(
                MockResponse().setResponseCode(200).setBody(
                    feedJson(
                        items = item(
                            sha256 = idx.toString().repeat(64).take(64).padStart(64, '0'),
                            fuzzy = ("ABCDE"[idx]).toString().repeat(70)
                        ),
                        nextCursorMs = cursorMs,
                        hasMore = true
                    )
                )
            )
        }

        val result = repository.sync(batchLimit = 1, maxBatches = 2) as ThreatFeedSyncResult.Success
        assertEquals("Must respect the user-supplied batch budget", 2, result.batches)
        assertEquals(2, result.insertedCount)
        assertEquals(200L, cursor.cursorMs)
    }

    @Test
    fun networkFailureMidPagination_doesNotAdvanceCursor() = runBlocking {
        cursor.cursorMs = 50L
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                feedJson(
                    items = item(sha256 = "a".repeat(64), fuzzy = "A".repeat(70)),
                    nextCursorMs = 100L,
                    hasMore = true
                )
            )
        )
        // Second request: simulate a server outage. Repository should bail out of the
        // pagination loop without persisting the partial cursor.
        server.enqueue(MockResponse().setResponseCode(503))

        val result = repository.sync(batchLimit = 1, maxBatches = 5)
        assertTrue("Failed sync must surface to the worker as Failed", result is ThreatFeedSyncResult.Failed)
        result as ThreatFeedSyncResult.Failed
        assertEquals("First batch's row should still have been committed", 1, result.insertedCount)
        // Cursor must NOT advance to the partial value — next periodic tick re-reads from 50L.
        assertEquals(50L, cursor.cursorMs)
        // ... but the rows already inserted stay durable: idempotent SHA-256 PK upserts.
        assertEquals(1, dao.inserted.size)
    }

    @Test
    fun malformedRowsAreFilteredButValidOnesArePersisted() = runBlocking {
        // Bad sha256 length, bad TLSH length, valid row. Only the valid one survives.
        val payload = feedJson(
            items = listOf(
                item(sha256 = "shortsha"),
                item(sha256 = "x".repeat(64), fuzzy = "tooshort"),
                item(sha256 = "d".repeat(64), fuzzy = "F".repeat(70))
            ).joinToString(","),
            nextCursorMs = 9_000L,
            hasMore = false
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(payload))

        val result = repository.sync() as ThreatFeedSyncResult.Success
        // Defensive validation: only the well-formed row reaches the DAO even though the
        // server in theory should have dropped the others. The on-device check is the last
        // line of defence against a misbehaving proxy or stale cached response.
        assertEquals(1, result.insertedCount)
        assertEquals(1, dao.inserted.size)
        assertEquals("d".repeat(64), dao.inserted.single().sha256)
    }

    @Test
    fun severityIsClampedAndOptionalFieldsAreNormalised() = runBlocking {
        val payload = feedJson(
            items = item(
                sha256 = "e".repeat(64),
                sha512 = null,
                threatName = "",
                threatFamily = "",
                severity = 250,
                source = ""
            ),
            nextCursorMs = 1L,
            hasMore = false
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(payload))

        repository.sync()
        val entity = dao.inserted.single()
        assertEquals(100, entity.severity) // clamped to [0, 100]
        // Empty threatName falls back to "Unknown" so the UI never renders a blank label.
        assertEquals("Unknown", entity.threatName)
        // Empty threatFamily becomes null (nothing useful to show).
        assertNull(entity.threatFamily)
        // Empty source falls back to the default provider name.
        assertEquals("malwarebazaar", entity.source)
    }

    @Test
    fun emptyResponseStillAdvancesCursor() = runBlocking {
        cursor.cursorMs = 5L
        val payload = feedJson(items = "", nextCursorMs = 10_000L, hasMore = false)
        server.enqueue(MockResponse().setResponseCode(200).setBody(payload))

        val result = repository.sync() as ThreatFeedSyncResult.Success
        assertEquals(0, result.insertedCount)
        // Even an empty feed advances the cursor — guards against re-walking a quiet
        // window forever on every periodic tick.
        assertEquals(10_000L, cursor.cursorMs)
        assertTrue("DAO must not be touched when no rows were eligible", dao.inserted.isEmpty())
    }

    @Test
    fun malformedJsonIsReportedAsFailedWithoutCursorAdvance() = runBlocking {
        cursor.cursorMs = 7L
        server.enqueue(MockResponse().setResponseCode(200).setBody("{not valid json"))

        val result = repository.sync()
        assertTrue(result is ThreatFeedSyncResult.Failed)
        assertEquals("Cursor must be preserved across parse failures", 7L, cursor.cursorMs)
    }

    // ── observability tests (Phase 2.4) ──────────────────────────────────────────────

    @Test
    fun successfulSync_writesSuccessStatusWithFreshTimestamp() = runBlocking {
        fakeNow = 1_700_001_234_000L
        val payload = feedJson(
            items = listOf(item()).joinToString(","),
            nextCursorMs = 2_000L,
            hasMore = false
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(payload))

        repository.sync(batchLimit = 200, maxBatches = 5)

        val status = statusStore.snapshot()
        assertEquals(ThreatFeedStatus.Outcome.SUCCESS, status.lastOutcome)
        // On success both timestamps are pinned to the start-of-attempt clock — by design;
        // a "Updated 1 second ago" with a 30 s-old `lastAttemptMs` would be confusing.
        assertEquals(1_700_001_234_000L, status.lastSuccessMs)
        assertEquals(1_700_001_234_000L, status.lastAttemptMs)
        assertNull("Failure reason must be cleared on success", status.lastFailureReason)
        assertEquals(1, status.lastInsertedCount)
    }

    @Test
    fun failedSync_preservesPriorSuccessTimestampAndRecordsReason() = runBlocking {
        // Simulate "we synced cleanly yesterday, today the server is throwing 503s." The
        // dashboard needs lastSuccessMs preserved so it can render "last refreshed
        // yesterday — current attempt failed". If we reset lastSuccessMs to 0 on failure
        // the user would see "never synced" the moment one transient error landed, which
        // is genuinely scary messaging for what's actually a 30-second blip.
        statusStore.update(
            ThreatFeedStatus(
                lastSuccessMs = 1_699_900_000_000L,
                lastAttemptMs = 1_699_900_000_000L,
                lastOutcome = ThreatFeedStatus.Outcome.SUCCESS,
                lastFailureReason = null,
                lastInsertedCount = 42
            )
        )
        cursor.cursorMs = 50L
        fakeNow = 1_700_005_000_000L
        server.enqueue(MockResponse().setResponseCode(503))

        val result = repository.sync()

        assertTrue(result is ThreatFeedSyncResult.Failed)
        val status = statusStore.snapshot()
        assertEquals(ThreatFeedStatus.Outcome.FAILED, status.lastOutcome)
        assertEquals(
            "Prior success timestamp must survive transient failures",
            1_699_900_000_000L,
            status.lastSuccessMs
        )
        assertEquals(
            "lastAttemptMs is bumped to the failed attempt's wall-clock",
            1_700_005_000_000L,
            status.lastAttemptMs
        )
        assertEquals(
            "Inserted count carries forward from the last successful run",
            42,
            status.lastInsertedCount
        )
        assertNotNull(status.lastFailureReason)
        assertTrue(
            "Failure reason should encode the HTTP status",
            status.lastFailureReason!!.contains("503")
        )
    }

    @Test
    fun networkFailure_preservesNeverSyncedSentinelWhenNoPriorSuccess() = runBlocking {
        // Fresh install (statusStore.snapshot() = NEVER) hitting the network and timing
        // out: we must still write a FAILED row but `lastSuccessMs` stays at 0 so the
        // dashboard renders "never synced" + "last attempt failed" instead of pretending
        // a sync ever succeeded.
        fakeNow = 1_700_006_000_000L
        // MockWebServer raw-disconnect simulates an IOException during the request.
        server.enqueue(
            MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START)
        )

        val result = repository.sync()

        assertTrue(result is ThreatFeedSyncResult.Failed)
        val status = statusStore.snapshot()
        assertEquals(ThreatFeedStatus.Outcome.FAILED, status.lastOutcome)
        assertEquals(0L, status.lastSuccessMs)
        assertEquals(1_700_006_000_000L, status.lastAttemptMs)
        assertEquals(0, status.lastInsertedCount)
    }

    @Test
    fun observeStatus_emitsInitialThenUpdatedSnapshots() = runBlocking {
        // Pre-seed an initial state so subscribers don't have to wait for a worker run.
        statusStore.update(
            ThreatFeedStatus(
                lastSuccessMs = 100L,
                lastAttemptMs = 100L,
                lastOutcome = ThreatFeedStatus.Outcome.SUCCESS,
                lastFailureReason = null,
                lastInsertedCount = 7
            )
        )
        // First emission ⇒ the seeded snapshot. Pinning this means a fresh dashboard
        // doesn't sit blank waiting for the next worker tick to populate the tile.
        val first = repository.observeStatus().first()
        assertEquals(ThreatFeedStatus.Outcome.SUCCESS, first.lastOutcome)
        assertEquals(7, first.lastInsertedCount)
    }

    @Test
    fun batchLimitIsClampedToProtocolCeiling() = runBlocking {
        val payload = feedJson(items = "", nextCursorMs = 1L, hasMore = false)
        server.enqueue(MockResponse().setResponseCode(200).setBody(payload))

        // Caller asks for 5_000 but the protocol cap is 1_000. Ensure we don't ship a value
        // that the server will reject (or worse, silently truncate without telling us).
        repository.sync(batchLimit = 5_000, maxBatches = 1)
        val recorded = server.takeRequest()
        assertEquals("1000", recorded.requestUrl?.queryParameter("limit"))
    }

    // ── signature-verification tests (Phase 3.1) ─────────────────────────────────────

    @Test
    fun signedEnvelopeIsUnwrappedAndPersisted() = runBlocking {
        // Server ships the Phase 3.1 signed envelope. The verifier is scripted to return
        // a `Signed(...)` result whose inner JSON the repository must parse and persist.
        // Pinning this proves the wrapper -> inner-JSON unwrap path lands rows in the DB
        // identically to the legacy unsigned path.
        val innerJson = feedJson(
            items = item(sha256 = "9".repeat(64), fuzzy = "F".repeat(70)),
            nextCursorMs = 4_242L,
            hasMore = false
        )
        val outerEnvelope = """
            {"schema":"v1.signed","key_id":"feed-test","signed_at_ms":1700000000000,
             "payload_b64":"<doesnt-matter-test-uses-fake-verifier>",
             "signature_b64":"<doesnt-matter-test-uses-fake-verifier>"}
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(outerEnvelope))
        verifier.script = { _ ->
            VerifiedThreatFeed.Signed(
                keyId = "feed-test",
                signedAtMs = 1_700_000_000_000L,
                payloadJson = innerJson
            )
        }

        val result = repository.sync() as ThreatFeedSyncResult.Success
        assertEquals(1, result.insertedCount)
        assertEquals("9".repeat(64), dao.inserted.single().sha256)
        assertEquals(4_242L, cursor.cursorMs)
    }

    @Test
    fun rejectedSignatureFailsTheSyncWithoutAdvancingCursor() = runBlocking {
        // Build pinned a key, server returned an envelope whose signature didn't match.
        // The repository must surface this as Failed with a `sig: signature_invalid`
        // reason (not as a generic parse error) so the dashboard can warn precisely.
        cursor.cursorMs = 99L
        val anyBody = feedJson(items = "", nextCursorMs = 100L, hasMore = false)
        server.enqueue(MockResponse().setResponseCode(200).setBody(anyBody))
        verifier.script = { _ -> VerifiedThreatFeed.Rejected("signature_invalid") }

        val result = repository.sync()

        assertTrue(result is ThreatFeedSyncResult.Failed)
        result as ThreatFeedSyncResult.Failed
        assertTrue(
            "Failure reason must surface the signature category from the verifier",
            result.reason.startsWith("sig: ")
        )
        assertTrue(result.reason.contains("signature_invalid"))
        assertEquals("Cursor must NOT advance on signature failure", 99L, cursor.cursorMs)
        assertTrue("DAO must not be touched", dao.inserted.isEmpty())

        val status = statusStore.snapshot()
        assertEquals(ThreatFeedStatus.Outcome.FAILED, status.lastOutcome)
        assertNotNull(status.lastFailureReason)
        assertTrue(
            "Status reason must encode the signature subcategory for ops dashboards",
            status.lastFailureReason!!.contains("signature_invalid")
        )
    }

    @Test
    fun rejectedDowngradeAttemptIsDistinctFromGenericFailure() = runBlocking {
        // "Build pinned a key, server returned an unsigned legacy response." This is a
        // separate downgrade-attempt category from a tampered signature so we can spot it
        // in telemetry — a misconfigured deployment looks the same as an attacker stripping
        // the envelope, but the failure reason should make the diagnostic obvious.
        val unsignedBody = feedJson(items = "", nextCursorMs = 100L, hasMore = false)
        server.enqueue(MockResponse().setResponseCode(200).setBody(unsignedBody))
        verifier.script = { _ -> VerifiedThreatFeed.Rejected("unsigned_response_in_signed_build") }

        val result = repository.sync()
        assertTrue(result is ThreatFeedSyncResult.Failed)
        val status = statusStore.snapshot()
        assertEquals(ThreatFeedStatus.Outcome.FAILED, status.lastOutcome)
        assertTrue(status.lastFailureReason!!.contains("unsigned_response_in_signed_build"))
    }
}

/**
 * In-memory DAO that records `insertAll` calls in arrival order. Other DAO methods are
 * unimplemented because the repository under test does not exercise them.
 */
private class FakeMalwareDao : MalwareSignatureDao {
    val inserted: MutableList<MalwareSignatureEntity> = mutableListOf()

    override suspend fun insert(entity: MalwareSignatureEntity) {
        inserted.add(entity)
    }

    override suspend fun insertAll(entities: List<MalwareSignatureEntity>) {
        inserted.addAll(entities)
    }

    override suspend fun findBySha256(sha256: String): MalwareSignatureEntity? =
        inserted.firstOrNull { it.sha256 == sha256 }

    override suspend fun getAllWithFuzzyHash(): List<MalwareSignatureEntity> =
        inserted.filter { !it.fuzzyHash.isNullOrBlank() }
}

private class InMemoryCursor : ThreatFeedCursorStore {
    override var cursorMs: Long = 0L
}

/**
 * In-memory [ThreatFeedStatusStore] backed by a [MutableStateFlow] so observers see every
 * `update` synchronously and tests can assert on the emission timeline without sleeping.
 */
private class InMemoryStatusStore : ThreatFeedStatusStore {
    private val state = MutableStateFlow(ThreatFeedStatus())
    override fun snapshot(): ThreatFeedStatus = state.value
    override fun update(status: ThreatFeedStatus) {
        state.value = status
    }
    override fun observe(): Flow<ThreatFeedStatus> = state
}

/**
 * Test-only signature verifier whose [script] block can be reassigned per test.
 *
 * Default behaviour mirrors the production "signing not enforced" path: every raw body is
 * accepted as legacy unsigned content. Tests that exercise the Phase 3.1 verification
 * path replace [script] with one that returns a `Signed` or `Rejected` result.
 */
private class ScriptedSignatureVerifier : ThreatFeedSignatureVerifier {
    var script: (String) -> VerifiedThreatFeed = { rawJson -> VerifiedThreatFeed.Unsigned(rawJson) }
    override fun verify(rawJson: String): VerifiedThreatFeed = script(rawJson)
}
