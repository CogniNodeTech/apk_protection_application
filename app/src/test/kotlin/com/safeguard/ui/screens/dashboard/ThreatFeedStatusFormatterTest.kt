package com.safeguard.ui.screens.dashboard

import com.safeguard.core.domain.repository.ThreatFeedStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for [ThreatFeedStatusFormatter]. The formatter is the only place where the
 * dashboard's user-facing wording for sync state is assembled; subtle drift here ("Updated 0
 * mins ago" instead of "just now", "stale" warnings firing inside the freshness window)
 * directly erodes user trust in the threat database, so each branch of the `when` is pinned
 * down explicitly.
 *
 * `nowMs` is always passed explicitly so these tests never touch the wall clock — flakiness
 * across daylight savings transitions or slow CI runners would silently rot the suite.
 */
class ThreatFeedStatusFormatterTest {

    private val now = 1_700_000_000_000L

    // ── NEVER ─────────────────────────────────────────────────────────────────────────

    @Test
    fun never_synced_renders_as_error_with_first_run_hint() {
        val display = ThreatFeedStatusFormatter.format(ThreatFeedStatus(), nowMs = now)

        assertEquals(ThreatFeedStatusFormatter.Severity.ERROR, display.severity)
        assertEquals("Threat database not yet synced", display.headline)
        assertTrue(display.detail!!.contains("Wi-Fi"))
        assertNull(display.insertedSummary)
    }

    @Test
    fun success_with_zero_lastSuccess_is_treated_as_never() {
        // Defensive: a torn snapshot or a forward-incompat status read should not produce
        // "Updated 53 years ago" (epoch 0). Falling back to NEVER is the safer failure mode.
        val torn = ThreatFeedStatus(
            lastSuccessMs = 0L,
            lastAttemptMs = now,
            lastOutcome = ThreatFeedStatus.Outcome.SUCCESS,
            lastInsertedCount = 1
        )
        val display = ThreatFeedStatusFormatter.format(torn, nowMs = now)

        assertEquals(ThreatFeedStatusFormatter.Severity.ERROR, display.severity)
        assertEquals("Threat database not yet synced", display.headline)
    }

    // ── SUCCESS — fresh ───────────────────────────────────────────────────────────────

    @Test
    fun fresh_success_within_minute_says_just_now() {
        val status = ThreatFeedStatus(
            lastSuccessMs = now - 30_000L,
            lastAttemptMs = now - 30_000L,
            lastOutcome = ThreatFeedStatus.Outcome.SUCCESS,
            lastInsertedCount = 5
        )
        val display = ThreatFeedStatusFormatter.format(status, nowMs = now)

        assertEquals(ThreatFeedStatusFormatter.Severity.OK, display.severity)
        assertEquals("Updated just now", display.headline)
        assertNull("No detail row when fresh + clean", display.detail)
        assertEquals("Last sync added 5 signatures.", display.insertedSummary)
    }

    @Test
    fun fresh_success_minutes_ago_uses_mins_label() {
        val status = ThreatFeedStatus(
            lastSuccessMs = now - 5L * 60_000L,
            lastAttemptMs = now - 5L * 60_000L,
            lastOutcome = ThreatFeedStatus.Outcome.SUCCESS,
            lastInsertedCount = 1
        )
        val display = ThreatFeedStatusFormatter.format(status, nowMs = now)

        assertEquals(ThreatFeedStatusFormatter.Severity.OK, display.severity)
        assertEquals("Updated 5 mins ago", display.headline)
        // Singular wording is exercised: "1 signature" not "1 signatures".
        assertEquals("Last sync added 1 signature.", display.insertedSummary)
    }

    @Test
    fun fresh_success_hours_ago_uses_hr_label() {
        val status = ThreatFeedStatus(
            lastSuccessMs = now - 4L * 3_600_000L,
            lastAttemptMs = now - 4L * 3_600_000L,
            lastOutcome = ThreatFeedStatus.Outcome.SUCCESS,
            lastInsertedCount = 0
        )
        val display = ThreatFeedStatusFormatter.format(status, nowMs = now)

        assertEquals("Updated 4 hr ago", display.headline)
        assertEquals(ThreatFeedStatusFormatter.Severity.OK, display.severity)
        // Zero-insert syncs are valid (quiet hour at the source) — no dangling tile line.
        assertNull(display.insertedSummary)
    }

    // ── SUCCESS — stale ───────────────────────────────────────────────────────────────

    @Test
    fun success_at_exactly_threshold_minus_one_is_still_ok() {
        // Boundary check: 47:59:59-old success must NOT trip the warning.
        val ageMs = ThreatFeedStatusFormatter.STALE_THRESHOLD_MS - 1L
        val status = ThreatFeedStatus(
            lastSuccessMs = now - ageMs,
            lastAttemptMs = now - ageMs,
            lastOutcome = ThreatFeedStatus.Outcome.SUCCESS,
            lastInsertedCount = 3
        )
        val display = ThreatFeedStatusFormatter.format(status, nowMs = now)

        assertEquals(ThreatFeedStatusFormatter.Severity.OK, display.severity)
        assertNull(display.detail)
    }

    @Test
    fun success_just_past_threshold_is_warning_with_stale_hint() {
        val ageMs = ThreatFeedStatusFormatter.STALE_THRESHOLD_MS + 60_000L
        val status = ThreatFeedStatus(
            lastSuccessMs = now - ageMs,
            lastAttemptMs = now - ageMs,
            lastOutcome = ThreatFeedStatus.Outcome.SUCCESS,
            lastInsertedCount = 12
        )
        val display = ThreatFeedStatusFormatter.format(status, nowMs = now)

        assertEquals(ThreatFeedStatusFormatter.Severity.WARNING, display.severity)
        assertTrue(display.headline.startsWith("Updated"))
        assertTrue(
            "Stale tile must hint that a refresh is recommended",
            display.detail!!.contains("stale")
        )
    }

    // ── SKIPPED ───────────────────────────────────────────────────────────────────────

    @Test
    fun skipped_with_no_prior_success_renders_as_error() {
        // User opted out of cloud verification before any sync ever ran.
        val status = ThreatFeedStatus(
            lastSuccessMs = 0L,
            lastAttemptMs = now,
            lastOutcome = ThreatFeedStatus.Outcome.SKIPPED,
            lastFailureReason = "cloud_verification_disabled"
        )
        val display = ThreatFeedStatusFormatter.format(status, nowMs = now)

        assertEquals(ThreatFeedStatusFormatter.Severity.ERROR, display.severity)
        assertEquals("Threat database not yet synced", display.headline)
        assertTrue(display.detail!!.contains("cloud verification"))
    }

    @Test
    fun skipped_with_recent_prior_success_keeps_ok_severity() {
        val status = ThreatFeedStatus(
            lastSuccessMs = now - 2L * 3_600_000L,
            lastAttemptMs = now,
            lastOutcome = ThreatFeedStatus.Outcome.SKIPPED,
            lastFailureReason = "cloud_verification_disabled",
            lastInsertedCount = 9
        )
        val display = ThreatFeedStatusFormatter.format(status, nowMs = now)

        assertEquals(ThreatFeedStatusFormatter.Severity.OK, display.severity)
        assertEquals("Updated 2 hr ago", display.headline)
        assertTrue(display.detail!!.contains("paused"))
    }

    // ── FAILED ────────────────────────────────────────────────────────────────────────

    @Test
    fun failed_with_no_prior_success_renders_as_error_with_reason() {
        val status = ThreatFeedStatus(
            lastSuccessMs = 0L,
            lastAttemptMs = now,
            lastOutcome = ThreatFeedStatus.Outcome.FAILED,
            lastFailureReason = "network: SocketTimeoutException"
        )
        val display = ThreatFeedStatusFormatter.format(status, nowMs = now)

        assertEquals(ThreatFeedStatusFormatter.Severity.ERROR, display.severity)
        assertEquals("Threat database not yet synced", display.headline)
        // "network: ..." → "no network" — the user-facing snippet, not the raw tag.
        assertTrue(display.detail!!.contains("no network"))
        assertTrue(display.detail!!.contains("retry"))
    }

    @Test
    fun failed_with_recent_prior_success_renders_as_warning() {
        val status = ThreatFeedStatus(
            lastSuccessMs = now - 90L * 60_000L,
            lastAttemptMs = now,
            lastOutcome = ThreatFeedStatus.Outcome.FAILED,
            lastFailureReason = "http_503",
            lastInsertedCount = 4
        )
        val display = ThreatFeedStatusFormatter.format(status, nowMs = now)

        // Recent success means we don't panic, but the failure must still be visible.
        assertEquals(ThreatFeedStatusFormatter.Severity.WARNING, display.severity)
        assertEquals("Updated 1 hr ago", display.headline)
        assertTrue(display.detail!!.contains("server error"))
    }

    @Test
    fun failed_with_unknown_reason_falls_back_to_raw_text() {
        val status = ThreatFeedStatus(
            lastSuccessMs = now - 12L * 3_600_000L,
            lastAttemptMs = now,
            lastOutcome = ThreatFeedStatus.Outcome.FAILED,
            lastFailureReason = "weird_unrecognised_tag"
        )
        val display = ThreatFeedStatusFormatter.format(status, nowMs = now)

        assertTrue(
            "Unmapped reasons must be surfaced verbatim, not silently swallowed",
            display.detail!!.contains("weird_unrecognised_tag")
        )
    }

    @Test
    fun failed_with_blank_reason_uses_unknown_label() {
        val status = ThreatFeedStatus(
            lastSuccessMs = 0L,
            lastAttemptMs = now,
            lastOutcome = ThreatFeedStatus.Outcome.FAILED,
            lastFailureReason = "   "
        )
        val display = ThreatFeedStatusFormatter.format(status, nowMs = now)

        assertTrue(display.detail!!.contains("unknown"))
    }
}
