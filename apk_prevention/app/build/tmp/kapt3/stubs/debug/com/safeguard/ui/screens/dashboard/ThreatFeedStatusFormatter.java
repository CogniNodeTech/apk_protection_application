package com.safeguard.ui.screens.dashboard;

import com.safeguard.core.domain.repository.ThreatFeedStatus;

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
 *  1. The dashboard already uses the same `Just now / mins ago / hr ago / days ago` style
 *     for [DashboardViewModel.formatTimeAgo], and consistency beats accuracy here;
 *  2. `DateUtils` requires an Android `Context` and would block a JVM-only test.
 *
 * Staleness threshold is **48 h** ([STALE_THRESHOLD_MS]) rather than the worker's 12 h sync
 * interval because:
 *  - Periodic WorkManager runs can drift by ±2 h on idle devices (Doze, network constraints);
 *  - The first re-attempt after a failure waits 30 min of exponential backoff, then 60, 120…
 *  - Firing a "stale" warning after one missed sync would create a noisy dashboard the user
 *    learns to ignore. 48 h ⇒ at least 4 missed cycles, which is signal, not noise.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0007\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0002\u0012\u0013B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004J\u0012\u0010\n\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\f\u001a\u00020\rH\u0002J\u0010\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u000bH\u0002J\u0010\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u0004H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter;", "", "()V", "STALE_THRESHOLD_MS", "", "format", "Lcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter$Display;", "status", "Lcom/safeguard/core/domain/repository/ThreatFeedStatus;", "nowMs", "insertedSummary", "", "count", "", "reasonText", "reason", "relative", "diffMs", "Display", "Severity", "app_debug"})
public final class ThreatFeedStatusFormatter {
    
    /**
     * 48 hours — see class-level comment for the reasoning.
     */
    public static final long STALE_THRESHOLD_MS = 172800000L;
    @org.jetbrains.annotations.NotNull
    public static final com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter INSTANCE = null;
    
    private ThreatFeedStatusFormatter() {
        super();
    }
    
    /**
     * @param status latest snapshot from [com.safeguard.core.domain.repository.ThreatFeedRepository.observeStatus].
     * @param nowMs current device wall-clock; injected so tests can deterministically assert
     *  the day-boundary cases (e.g. a 47:59:59-old success is fresh, a 48:00:01-old success
     *  is stale).
     */
    @org.jetbrains.annotations.NotNull
    public final com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Display format(@org.jetbrains.annotations.NotNull
    com.safeguard.core.domain.repository.ThreatFeedStatus status, long nowMs) {
        return null;
    }
    
    /**
     * Compact human-readable durations matching `DashboardViewModel.formatTimeAgo`. Kept as
     * a private helper instead of pulling that one out — duplication is two `when` blocks,
     * extraction would mean a cross-file dependency for ~6 LOC.
     */
    private final java.lang.String relative(long diffMs) {
        return null;
    }
    
    private final java.lang.String insertedSummary(int count) {
        return null;
    }
    
    /**
     * Map the repository's machine-readable failure tags (`network: SocketTimeoutException`,
     * `http_503`, `db: SQLiteFullException`) to short user-facing snippets. We don't try to
     * be comprehensive — anything we don't recognise is rendered verbatim, which is still
     * better than swallowing the diagnostic.
     */
    private final java.lang.String reasonText(java.lang.String reason) {
        return null;
    }
    
    /**
     * Result struct consumed by the Compose layer. Kept flat (no `sealed`) so the dashboard
     * binding stays a one-liner — the warning level is just a tri-state enum the card colour
     * can branch on.
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B)\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0010\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0006H\u00c6\u0003J\u000b\u0010\u0012\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J5\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001J\t\u0010\u0019\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\nR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u001a"}, d2 = {"Lcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter$Display;", "", "headline", "", "detail", "severity", "Lcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter$Severity;", "insertedSummary", "(Ljava/lang/String;Ljava/lang/String;Lcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter$Severity;Ljava/lang/String;)V", "getDetail", "()Ljava/lang/String;", "getHeadline", "getInsertedSummary", "getSeverity", "()Lcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter$Severity;", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class Display {
        
        /**
         * Headline string (e.g. "Updated 4 hr ago"). Always non-empty.
         */
        @org.jetbrains.annotations.NotNull
        private final java.lang.String headline = null;
        
        /**
         * Optional secondary line (e.g. "Last attempt failed: network"). Null ⇒ no second row.
         */
        @org.jetbrains.annotations.Nullable
        private final java.lang.String detail = null;
        
        /**
         * Used by the tile to pick colour + icon (green / amber / red).
         */
        @org.jetbrains.annotations.NotNull
        private final com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Severity severity = null;
        
        /**
         * Inserted-count summary (e.g. "Last sync added 87 signatures"). Null ⇒ omit.
         */
        @org.jetbrains.annotations.Nullable
        private final java.lang.String insertedSummary = null;
        
        public Display(@org.jetbrains.annotations.NotNull
        java.lang.String headline, @org.jetbrains.annotations.Nullable
        java.lang.String detail, @org.jetbrains.annotations.NotNull
        com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Severity severity, @org.jetbrains.annotations.Nullable
        java.lang.String insertedSummary) {
            super();
        }
        
        /**
         * Headline string (e.g. "Updated 4 hr ago"). Always non-empty.
         */
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getHeadline() {
            return null;
        }
        
        /**
         * Optional secondary line (e.g. "Last attempt failed: network"). Null ⇒ no second row.
         */
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getDetail() {
            return null;
        }
        
        /**
         * Used by the tile to pick colour + icon (green / amber / red).
         */
        @org.jetbrains.annotations.NotNull
        public final com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Severity getSeverity() {
            return null;
        }
        
        /**
         * Inserted-count summary (e.g. "Last sync added 87 signatures"). Null ⇒ omit.
         */
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getInsertedSummary() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Severity component3() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component4() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Display copy(@org.jetbrains.annotations.NotNull
        java.lang.String headline, @org.jetbrains.annotations.Nullable
        java.lang.String detail, @org.jetbrains.annotations.NotNull
        com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Severity severity, @org.jetbrains.annotations.Nullable
        java.lang.String insertedSummary) {
            return null;
        }
        
        @java.lang.Override
        public boolean equals(@org.jetbrains.annotations.Nullable
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/safeguard/ui/screens/dashboard/ThreatFeedStatusFormatter$Severity;", "", "(Ljava/lang/String;I)V", "OK", "WARNING", "ERROR", "app_debug"})
    public static enum Severity {
        /*public static final*/ OK /* = new OK() */,
        /*public static final*/ WARNING /* = new WARNING() */,
        /*public static final*/ ERROR /* = new ERROR() */;
        
        Severity() {
        }
        
        @org.jetbrains.annotations.NotNull
        public static kotlin.enums.EnumEntries<com.safeguard.ui.screens.dashboard.ThreatFeedStatusFormatter.Severity> getEntries() {
            return null;
        }
    }
}