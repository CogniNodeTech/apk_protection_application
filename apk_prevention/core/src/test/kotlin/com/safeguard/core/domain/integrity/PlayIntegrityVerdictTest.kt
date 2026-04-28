package com.safeguard.core.domain.integrity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayIntegrityVerdictTest {

    @Test
    fun disabled_singleton_isCanonicalForwardCompatibleSentinel() {
        val a = PlayIntegrityVerdict.Disabled
        val b = PlayIntegrityVerdict.Disabled
        // Identity check is intentional — call sites use `=== Disabled` for trace logging.
        assertSame(a, b)
        assertEquals(PlayIntegrityVerdict.DeviceIntegrityLevel.UNAVAILABLE, a.device)
        assertEquals(PlayIntegrityVerdict.AppIntegrityLevel.UNAVAILABLE, a.app)
        assertEquals(PlayIntegrityVerdict.AccountIntegrityLevel.UNAVAILABLE, a.account)
        assertEquals(PlayIntegrityVerdict.Source.DISABLED, a.source)
    }

    @Test
    fun looksTrustworthy_requiresAtLeastBasicAndPlayRecognized() {
        val ok = PlayIntegrityVerdict(
            device = PlayIntegrityVerdict.DeviceIntegrityLevel.DEVICE,
            app = PlayIntegrityVerdict.AppIntegrityLevel.PLAY_RECOGNIZED,
            account = PlayIntegrityVerdict.AccountIntegrityLevel.LICENSED,
            source = PlayIntegrityVerdict.Source.PLAY_INTEGRITY_API
        )
        assertTrue(ok.looksTrustworthy())

        val basicAndRecognized = ok.copy(device = PlayIntegrityVerdict.DeviceIntegrityLevel.BASIC)
        assertTrue(basicAndRecognized.looksTrustworthy())

        val rawDevice = ok.copy(device = PlayIntegrityVerdict.DeviceIntegrityLevel.RAW_OR_UNKNOWN)
        assertFalse(rawDevice.looksTrustworthy())

        val unrecognizedApp = ok.copy(app = PlayIntegrityVerdict.AppIntegrityLevel.UNRECOGNIZED_VERSION)
        assertFalse(unrecognizedApp.looksTrustworthy())

        // The Disabled sentinel is explicitly NOT trustworthy.
        assertFalse(PlayIntegrityVerdict.Disabled.looksTrustworthy())
    }

    @Test
    fun toEvidenceLine_isStable_andTruncatesLongNotes() {
        val v = PlayIntegrityVerdict(
            device = PlayIntegrityVerdict.DeviceIntegrityLevel.STRONG,
            app = PlayIntegrityVerdict.AppIntegrityLevel.PLAY_RECOGNIZED,
            account = PlayIntegrityVerdict.AccountIntegrityLevel.LICENSED,
            source = PlayIntegrityVerdict.Source.PLAY_INTEGRITY_API,
            note = "x".repeat(500)
        )
        val line = v.toEvidenceLine()
        assertTrue("missing device", line.contains("device=STRONG"))
        assertTrue("missing app", line.contains("app=PLAY_RECOGNIZED"))
        assertTrue("missing account", line.contains("account=LICENSED"))
        assertTrue("missing source", line.contains("source=PLAY_INTEGRITY_API"))
        // Long notes get clamped to 120 chars to keep the evidence list grep-friendly.
        assertTrue("note not truncated, line length=${line.length}", line.length < 250)
    }

    @Test
    fun toEvidenceLine_omitsBlankNote() {
        val v = PlayIntegrityVerdict(
            device = PlayIntegrityVerdict.DeviceIntegrityLevel.UNAVAILABLE,
            app = PlayIntegrityVerdict.AppIntegrityLevel.UNAVAILABLE,
            account = PlayIntegrityVerdict.AccountIntegrityLevel.UNAVAILABLE,
            source = PlayIntegrityVerdict.Source.DISABLED,
            note = null
        )
        val line = v.toEvidenceLine()
        assertFalse(line.contains("note="))
    }
}
