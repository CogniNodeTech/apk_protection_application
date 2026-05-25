package com.safeguard.security.layers.base

import com.safeguard.core.domain.layer.ProtectionLayer as CoreProtectionLayer
import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.LayerResult

/**
 * Security module's implementation of the core ProtectionLayer interface.
 * All 6 layers implement this.
 */
interface ProtectionLayer : CoreProtectionLayer {
    override suspend fun verify(context: APKContext, previousLayerResults: List<LayerResult>): LayerResult
}
