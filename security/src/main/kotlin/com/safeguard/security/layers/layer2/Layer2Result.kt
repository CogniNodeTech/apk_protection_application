package com.safeguard.security.layers.layer2

import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.ThreatInfo
import com.safeguard.core.domain.model.Verdict

data class Layer2Result(
    override val layerId: Int = 2,
    override val layerName: String = "Hash Database Checker",
    override val verdict: Verdict,
    override val confidence: Float,
    override val riskScore: Int,
    val sha256: String,
    val threatName: String?,
    val similarity: Int?,
    override val evidence: List<String>,
    override val executionTimeMs: Long = 0,
    override val threatInfo: ThreatInfo? = null,
    /**
     * SHA-512 digest of the scanned APK (lowercase hex, no prefix). Populated whenever the
     * validator computed it — i.e. for every scan once Phase 2.5 lands. Cached alongside
     * [sha256] in [com.safeguard.core.domain.model.APKContext] so downstream layers (e.g.
     * forensic reasoning) can render the strongest hash they have without re-reading the file.
     * Null only on extreme-edge code paths where the validator returned without computing
     * digests at all (e.g. file deleted between detection and scan).
     */
    val sha512: String? = null,

    /**
     * `true` iff the scanned file's SHA-256 matched a known-malware row but its SHA-512 did
     * **not** match the SHA-512 stored on that row. This is either (a) a genuine SHA-256
     * collision — astronomically unlikely but no longer impossible — or (b) evidence of
     * threat-DB tampering or a feed-pipeline bug that paired the wrong hashes. Either way
     * the validator declines to label the file with the matched row's threat name and
     * downgrades the verdict to SUSPICIOUS so the user (and the orchestrator) treat the
     * detection with appropriate scepticism.
     *
     * The corresponding evidence row carries the `Hash collision detected` string so this
     * boolean isn't load-bearing for UI rendering — it's surfaced separately to let the
     * decision engine route collision cases through a dedicated "needs human review" lane
     * if a future policy wants to (today both lanes are SUSPICIOUS).
     */
    val isCollision: Boolean = false
) : LayerResult
