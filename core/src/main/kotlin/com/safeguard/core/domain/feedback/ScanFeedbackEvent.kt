package com.safeguard.core.domain.feedback

/**
 * One privacy-safe scan feedback event queued for delivery to the SafeGuard backend.
 *
 * Phase 3.2 introduces this as the *opt-in* counterpart to the existing fire-and-forget
 * [ScanTelemetry] (which only emits a verdict counter). The feedback channel ships
 * structured metadata that the backend can use to:
 *  - tune layer thresholds (we know `layer4_signature_score=37` was *actually* malicious),
 *  - fold real-world false positives back into the bundled YARA / hash sources,
 *  - and drive the eventual Phase 3.3 benchmark against a corpus that mirrors what users
 *    actually scan, not what we synthesised in the lab.
 *
 * **Privacy contract (must be preserved by every implementation):**
 *  - **No APK bytes ever.** The on-disk Room queue stores hashes and metadata only.
 *  - **No file paths.** The package name is enough; absolute paths leak account names,
 *    SD-card slot identifiers, and personal folder structure.
 *  - **No clipboard / contacts / SMS / location.** This is a *signature feedback* channel,
 *    not a generic crash reporter.
 *  - **Default off.** Wired through [SecurePreferencesManager.scanFeedbackEnabled]; the
 *    repository drops events that arrive while the toggle is false instead of queueing.
 *  - **User-controllable purge.** The settings screen exposes "clear queued feedback" so
 *    a user who toggles off mid-flight can confirm nothing is left in the queue.
 *
 * @property id stable UUID generated at scan time. Acts as the server-side dedupe key so
 *   network retries don't double-count an event in the corpus.
 * @property createdAtMs epoch-ms the scan completed (not the upload time). Used by the
 *   server to drop outliers (e.g. devices with skewed clocks) and to bin events by week.
 * @property sha256 lowercase hex of the scanned APK. The corpus is keyed on this; rerunning
 *   the same APK on a new device produces a duplicate row that the server collapses.
 * @property verdict the on-device decision engine's final label (`CLEAN`, `SUSPICIOUS`, ...).
 * @property confidence the engine's confidence in [0, 1].
 * @property packageName top-level Android package, **no path**. Optional because
 *   side-loaded `.apk`s scanned outside the installed-app loop don't have one.
 * @property versionCode optional integer version code; supports the "this banker reuses
 *   versionCode 17 across droppers" kind of analysis without leaking version strings.
 * @property layerScores per-layer normalised score (0..1). Keyed on the layer's stable
 *   identifier (`"layer1"` … `"layer7"`) to keep the wire format stable across
 *   `ProtectionLayer` enum reorderings.
 * @property triggeredRules YARA rule names that matched during this scan, if any. Empty
 *   list when no rules fired. Names only — no rule body, no captured byte ranges.
 * @property androidApiLevel `Build.VERSION.SDK_INT`. Used to bucket false positives by OS
 *   (often a behavioural pattern only triggers on Android 13+).
 * @property appVersionCode SafeGuard's own `BuildConfig.VERSION_CODE` so the server can
 *   correlate a regression with a specific app release.
 */
data class ScanFeedbackEvent(
    val id: String,
    val createdAtMs: Long,
    val sha256: String,
    val verdict: String,
    val confidence: Float,
    val packageName: String?,
    val versionCode: Int?,
    val layerScores: Map<String, Float>,
    val triggeredRules: List<String>,
    val androidApiLevel: Int,
    val appVersionCode: Int
) {
    init {
        // Defensive — invariants that, if violated, indicate a programming error rather
        // than a data error. We crash early so the bug shows up in tests instead of
        // shipping malformed events to the backend.
        require(id.isNotBlank()) { "feedback id must not be blank" }
        require(sha256.length == 64) { "feedback sha256 must be 64 hex chars; got ${sha256.length}" }
        require(confidence in 0f..1f) { "confidence must be in [0,1]; got $confidence" }
        require(createdAtMs >= 0L) { "createdAtMs must be non-negative" }
    }
}

/**
 * Outcome of a single feedback batch upload. Exposed to the worker so it can decide
 * whether to retry, back off, or report the failure to the dashboard if we ever wire one.
 */
sealed interface FeedbackUploadResult {
    /** Server accepted [uploadedCount] rows; the repository deleted them from the queue. */
    data class Success(val uploadedCount: Int) : FeedbackUploadResult

    /** Worker had nothing to upload — queue empty or feedback pref disabled. */
    object Skipped : FeedbackUploadResult

    /**
     * Network or server failure. [reason] is a short opaque tag (e.g. `network`, `http_502`,
     * `validation`) used for retry / backoff decisions. Rows stay in the queue.
     */
    data class Failed(val reason: String) : FeedbackUploadResult
}
