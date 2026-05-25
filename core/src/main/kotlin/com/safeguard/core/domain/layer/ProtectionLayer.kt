package com.safeguard.core.domain.layer

import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.LayerResult

/**
 * Interface for a single protection layer. Implemented by security module.
 * [previousLayerResults] is populated by the orchestrator for layers that run after others (e.g. Layer 6 cloud can use local scores).
 */
interface ProtectionLayer {
    suspend fun verify(context: APKContext, previousLayerResults: List<LayerResult> = emptyList()): LayerResult
}
