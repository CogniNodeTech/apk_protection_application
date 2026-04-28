package com.safeguard.data.local.cache

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Ensures Room JSON for layer results stays stable across Moshi upgrades (regression guard).
 */
class LayerResultDtoMoshiTest {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listAdapter =
        moshi.adapter<List<LayerResultDto>>(
            Types.newParameterizedType(List::class.java, LayerResultDto::class.java)
        )

    @Test
    fun layerResultList_roundTrip_preservesFields() {
        val original = listOf(
            LayerResultDto(
                layerId = 5,
                layerName = "ML Analysis",
                verdict = "SUSPICIOUS",
                confidence = 0.72f,
                riskScore = 72,
                evidence = listOf("line1", "line2"),
                executionTimeMs = 150L,
                threatName = "Test",
                threatFamily = null,
                threatRiskScore = 7
            )
        )
        val json = listAdapter.toJson(original)
        val parsed = listAdapter.fromJson(json)!!
        assertEquals(original, parsed)
    }
}
