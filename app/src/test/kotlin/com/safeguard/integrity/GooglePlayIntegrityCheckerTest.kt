package com.safeguard.integrity

import android.content.Context
import com.safeguard.core.domain.integrity.PlayIntegrityConfig
import com.safeguard.core.domain.integrity.PlayIntegrityVerdict
import com.safeguard.core.domain.integrity.ScanIntegrityContext
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GooglePlayIntegrityCheckerTest {

    /**
     * The scaffold MUST NOT pretend to have done a real Play Integrity call. Until the
     * SDK call is actually wired, every check is reported as
     * [PlayIntegrityVerdict.Source.PLAY_INTEGRITY_API_ERROR] so the cloud verifier and
     * forensic engine know not to trust it.
     */
    @Test
    fun scaffold_returnsApiErrorVerdict_neverPretendsToVerify() = runTest {
        val context = mockk<Context>(relaxed = true)
        val checker = GooglePlayIntegrityChecker(
            context = context,
            config = PlayIntegrityConfig(cloudProjectNumber = "1234567890")
        )

        val verdict = checker.check(ScanIntegrityContext(requestHash = "a".repeat(64)))

        assertEquals(PlayIntegrityVerdict.Source.PLAY_INTEGRITY_API_ERROR, verdict.source)
        assertEquals(PlayIntegrityVerdict.DeviceIntegrityLevel.UNAVAILABLE, verdict.device)
        assertEquals(PlayIntegrityVerdict.AppIntegrityLevel.UNAVAILABLE, verdict.app)
        assertEquals(PlayIntegrityVerdict.AccountIntegrityLevel.UNAVAILABLE, verdict.account)
        assertNotNull(verdict.note)
        assertTrue("note should mention scaffold", verdict.note!!.contains("scaffold"))
    }

    /**
     * Belt-and-braces: if a future refactor accidentally constructs the real checker
     * with a disabled config (e.g. someone removed the Hilt selection guard), the
     * checker must self-disable rather than silently making API_ERROR look like a
     * legitimate failure-to-verify.
     */
    @Test
    fun whenConfigDisabled_returnsCanonicalDisabledVerdict() = runTest {
        val context = mockk<Context>(relaxed = true)
        val checker = GooglePlayIntegrityChecker(
            context = context,
            config = PlayIntegrityConfig(cloudProjectNumber = "")
        )

        val verdict = checker.check(ScanIntegrityContext(requestHash = "a".repeat(64)))

        assertSame(PlayIntegrityVerdict.Disabled, verdict)
    }
}
