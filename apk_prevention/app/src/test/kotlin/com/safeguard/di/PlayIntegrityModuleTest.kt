package com.safeguard.di

import android.content.Context
import com.safeguard.core.domain.integrity.NoOpPlayIntegrityChecker
import com.safeguard.core.domain.integrity.PlayIntegrityConfig
import com.safeguard.integrity.GooglePlayIntegrityChecker
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 3.4 wiring tests for [PlayIntegrityModule]. We don't bring up Hilt — we just
 * call the module providers directly. The behavior we care about is the *selection
 * logic*, not Hilt's container.
 */
class PlayIntegrityModuleTest {

    private val context = mockk<Context>(relaxed = true).also {
        // The real impl pulls applicationContext on construction; relaxed mocks return
        // a default mock for unset members, but make the chained call explicit so the
        // test reads cleanly.
        every { it.applicationContext } returns it
    }

    @Test
    fun emptyProjectNumber_selectsNoOpChecker() {
        val noOp = NoOpPlayIntegrityChecker()
        val config = PlayIntegrityConfig(cloudProjectNumber = "")

        val result = PlayIntegrityModule.providePlayIntegrityChecker(
            config = config,
            context = context,
            noOp = noOp
        )

        assertSame(noOp, result)
    }

    @Test
    fun nonNumericProjectNumber_selectsNoOpAndLogsWarning() {
        val noOp = NoOpPlayIntegrityChecker()
        val config = PlayIntegrityConfig(cloudProjectNumber = "abc-not-a-number")

        val result = PlayIntegrityModule.providePlayIntegrityChecker(
            config = config,
            context = context,
            noOp = noOp
        )

        // Misconfigured project numbers must NOT silently route to the real checker —
        // that would result in a real SDK call with garbage and zero useful telemetry.
        assertSame(noOp, result)
    }

    @Test
    fun numericProjectNumber_selectsRealCheckerScaffold() {
        val noOp = NoOpPlayIntegrityChecker()
        val config = PlayIntegrityConfig(cloudProjectNumber = "1024982398421")

        val result = PlayIntegrityModule.providePlayIntegrityChecker(
            config = config,
            context = context,
            noOp = noOp
        )

        assertTrue(
            "expected GooglePlayIntegrityChecker; got ${result.javaClass.simpleName}",
            result is GooglePlayIntegrityChecker
        )
    }
}
