package com.safeguard.security.layers.layer4

import com.safeguard.core.domain.model.LayerResult
import com.safeguard.core.domain.model.Verdict

data class Layer4Result(
    override val layerId: Int = 4,
    override val layerName: String = "Signature Validator",
    override val verdict: Verdict,
    override val confidence: Float,
    override val riskScore: Int,
    val certificateInfo: CertificateInfo?,
    override val evidence: List<String>,
    override val executionTimeMs: Long = 0
) : LayerResult

data class CertificateInfo(
    val issuer: String,
    val subject: String,
    val notAfter: Long,
    val algorithm: String,
    val isSelfSigned: Boolean
)
