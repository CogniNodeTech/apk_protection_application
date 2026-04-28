package com.safeguard.core.domain.integrity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayIntegrityConfigTest {

    @Test
    fun emptyProjectNumber_isDisabled() {
        val cfg = PlayIntegrityConfig(cloudProjectNumber = "")
        assertFalse(cfg.isEnabled)
        assertNull(cfg.cloudProjectNumberAsLong())
    }

    @Test
    fun whitespaceProjectNumber_isDisabled() {
        val cfg = PlayIntegrityConfig(cloudProjectNumber = "   ")
        assertFalse(cfg.isEnabled)
        assertNull(cfg.cloudProjectNumberAsLong())
    }

    @Test
    fun nonNumericProjectNumber_isDisabled() {
        // A typo in local.properties should disable, not crash, and never be parsed
        // as a partial Long.
        val cfg = PlayIntegrityConfig(cloudProjectNumber = "1234abc")
        assertFalse(cfg.isEnabled)
        assertNull(cfg.cloudProjectNumberAsLong())
    }

    @Test
    fun numericProjectNumber_isEnabled_andParsesAsLong() {
        val cfg = PlayIntegrityConfig(cloudProjectNumber = "1024982398421")
        assertTrue(cfg.isEnabled)
        assertEquals(1024982398421L, cfg.cloudProjectNumberAsLong())
    }
}
