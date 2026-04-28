package com.safeguard.ui.screens.dashboard

import com.safeguard.core.domain.repository.ThreatFeedStatus

/**
 * Pure-JVM mapping from a [ThreatFeedStatus] snapshot to the strings + warning level the
 * dashboard tile renders. Kept as a separate object (rather than buried inside the
 * `ViewModel`) so the formatting rules are unit-testable without spinning up Hilt or any
 * Compose runtime — the dashboard tile is one of the few user-facing surfaces where a
 * subtle wording bug ("synced 3 weeks ago" vs "never synced") quietly erodes user trust,
 * so this code path is worth pinning down with cheap deterministic tests.
 *
 * Time formatting is done in fixed thresholds (minute / hour / day) instead of locale-aware
 * `DateUtils.getRelativeTimeSpanString` because:
 *   1. The dashboard already uses the same `Just now / mins ago / hr ago / days ago` style
 *      for [DashboardViewModel.formatTimeAgo], and consistency beats accuracy here;
 *   2. `DateUtils` requires an Android `Context` and would block a JVM-only test.
 *
 * Staleness threshold is **48 h** ([STALE_THRESHOLD_MS]) rather than the worker's 12 h sync
 * interval because:
 *   - Periodic WorkManager runs can drift by ±2 h on idle devices (Doze, network constraints);
 *   - The first re-attempt after a failure waits 30 min of exponential backoff, then 60, 120…
 *   - Firing a "stale" warning after one missed sync would create a noisy dashboard the user
 *     learns to ignore. 48 h ⇒ at least 4 missed cycles, which is signal, not noise.
 */
object ThreatFeedStatusFormatter {

    /**
     * Result struct consumed by the Compose layer. Kept flat (no `sealed`) so the dashboard
     * binding stays a one-liner — the warning level is just a tri-state enum the card colour
     * can branch on.
     */
    data class Display(
        /** Headline string (e.g. "Updated 4 hr ago"). Always non-empty. */
        val headline: String,

        /** Optional secondary line (e.g. "Last attempt failed: network"). Null ⇒ no second row. */
        val detail: String?,

        /** Used by the tile to pick colour + icon (green / amber / red). */
        val severity: Severity,

        /** Inserted-count summary (e.g. "Last sync added 87 signatures"). Null ⇒ omit. */
        val insertedSummary: String?
    )

    enum class Severity {
        /** Fresh ([lastSuccessMs] within [STALE_THRESHOLD_MS]) and no failure on the most recent attempt. */
        OK,

        /** Either stale (over threshold), or last attempt failed but a recent success exists. */
        WARNING,

        /** Never synced, or every attempt has failed and no prior success exists. */
        ERROR
    }

    /**
     * @param status latest snapshot from [com.safeguard.core.domain.repository.ThreatFeedRepository.observeStatus].
     * @param nowMs current device wall-clock; injected so tests can deterministically assert
     *   the day-boundary cases (e.g. a 47:59:59-old success is fresh, a 48:00:01-old success
     *   is stale).
     */
    fun format(status: ThreatFeedStatus, nowMs: Long): Display {
        val outcome = status.lastOutcome
        // Defensive against a torn snapshot (success outcome with `lastSuccessMs == 0`): treat
        // it as never-synced rather than printing "Updated ... ago" with nonsense duration.
        val effectiveOutcome = if (
            outcome == ThreatFeedStatus.Outcome.SUCCESS && status.lastSuccessMs <= 0L
        ) ThreatFeedStatus.Outcome.NEVER else outcome

        return when (effectiveOutcome) {
            ThreatFeedStatus.Outcome.NEVER -> Display(
                headline = "Threat database not yet synced",
                detail = "First sync runs once your device has Wi-Fi.",
                severity = Severity.ERROR,
                insertedSummary = null
            )

            ThreatFeedStatus.Outcome.SUCCESS -> {
                val ageMs = (nowMs - status.lastSuccessMs).coerceAtLeast(0L)
                val stale = ageMs > STALE_THRESHOLD_MS
                Display(
                    headline = "Updated " + relative(ageMs),
                    detail = if (stale) "Threat data is stale — pull-to-sync recommended." else null,
                    severity = if (stale) Severity.WARNING else Severity.OK,
                    insertedSummary = insertedSummary(status.lastInsertedCount)
                )
            }

            ThreatFeedStatus.Outcome.SKIPPED -> {
                val hasPriorSuccess = status.lastSuccessMs > 0L
                if (hasPriorSuccess) {
                    val ageMs = (nowMs - status.lastSuccessMs).coerceAtLeast(0L)
                    Display(
                        headline = "Updated " + relative(ageMs),
                        detail = "Sync paused: cloud verification is off.",
                        severity = if (ageMs > STALE_THRESHOLD_MS) Severity.WARNING else Severity.OK,
                        insertedSummary = insertedSummary(status.lastInsertedCount)
                    )
                } else {
                    Display(
                        headline = "Threat database not yet synced",
                        detail = "Sync paused: cloud verification is off.",
                        severity = Severity.ERROR,
                        insertedSummary = null
                    )
                }
            }

            ThreatFeedStatus.Outcome.FAILED -> {
                val reason = status.lastFailureReason?.takeIf { it.isNotBlank() } ?: "unknown"
                val hasPriorSuccess = status.lastSuccessMs > 0L
                if (hasPriorSuccess) {
                    val ageMs = (nowMs - status.lastSuccessMs).coerceAtLeast(0L)
                    Display(
                        headline = "Updated " + relative(ageMs),
                        detail = "Last attempt failed: ${reasonText(reason)} (will retry).",
                        // Stale prior success is still WARNING; a *recent* prior success with a
                        // freshly failed attempt is also WARNING (not OK) — the user should
                        // know retries are in flight.
                        severity = Severity.WARNING,
                        insertedSummary = insertedSummary(status.lastInsertedCount)
                    )
                } else {
                    Display(
                        headline = "Threat database not yet synced",
                        detail = "Last attempt failed: ${reasonText(reason)} (will retry).",
                        severity = Severity.ERROR,
                        insertedSummary = null
                    )
                }
            }
        }
    }

    /**
     * Compact human-readable durations matching `DashboardViewModel.formatTimeAgo`. Kept as
     * a private helper instead of pulling that one out — duplication is two `when` blocks,
     * extraction would mean a cross-file dependency for ~6 LOC.
     */
    private fun relative(diffMs: Long): String = when {
        diffMs < 60_000L -> "just now"
        diffMs < 3_600_000L -> "${diffMs / 60_000L} mins ago"
        diffMs < 86_400_000L -> "${diffMs / 3_600_000L} hr ago"
        else -> "${diffMs / 86_400_000L} days ago"
    }

    private fun insertedSummary(count: Int): String? = when {
        count <= 0 -> null
        count == 1 -> "Last sync added 1 signature."
        else -> "Last sync added $count signatures."
    }

    /**
     * Map the repository's machine-readable failure tags (`network: SocketTimeoutException`,
     * `http_503`, `db: SQLiteFullException`) to short user-facing snippets. We don't try to
     * be comprehensive — anything we don't recognise is rendered verbatim, which is still
     * better than swallowing the diagnostic.
     */
    private fun reasonText(reason: String): String {
        val trimmed = reason.trim()
        return when {
            trimmed.startsWith("network", ignoreCase = true) -> "no network"
            trimmed.startsWith("http_5", ignoreCase = true) -> "server error"
            trimmed.startsWith("http_4", ignoreCase = true) -> "request rejected"
            trimmed.startsWith("db:", ignoreCase = true) -> "database error"
            trimmed.startsWith("parse:", ignoreCase = true) -> "bad response"
            else -> trimmed
        }
    }

    /** 48 hours — see class-level comment for the reasoning. */
    const val STALE_THRESHOLD_MS: Long = 48L * 60L * 60L * 1_000L
}
