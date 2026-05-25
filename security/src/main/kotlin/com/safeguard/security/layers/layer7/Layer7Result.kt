package com.safeguard.security.layers.layer7

import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.ThreatInfo
import com.safeguard.core.domain.model.Verdict

/**
 * Layer 7 (YARA-style content rules) verdict.
 *
 * Carries both the human-readable evidence list every layer surfaces and a structured
 * list of fired rule names so downstream telemetry / cloud reporting can ship the
 * exact ruleset hits without re-parsing free-form evidence strings.
 */
data class Layer7Result(
    override val layerId: Int = 7,
    override val layerName: String = "Pattern Rules (YARA)",
    override val verdict: Verdict,
    override val confidence: Float,
    override val riskScore: Int,
    val firedRules: List<String>,
    val highestSeverity: Int,
    override val evidence: List<String>,
    override val executionTimeMs: Long = 0,
    override val threatInfo: ThreatInfo? = null
) : LayerResult
