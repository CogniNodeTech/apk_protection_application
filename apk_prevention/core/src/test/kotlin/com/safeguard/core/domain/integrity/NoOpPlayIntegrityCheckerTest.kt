package com.safeguard.core.domain.integrity

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class NoOpPlayIntegrityCheckerTest {

    @Test
    fun returnsCanonicalDisabledVerdict() = runTest {
        val checker = NoOpPlayIntegrityChecker()
        val verdict = checker.check(ScanIntegrityContext(requestHash = "a".repeat(64)))
        // Identity, not just equals: callers can `=== Disabled` to short-circuit.
        assertSame(PlayIntegrityVerdict.Disabled, verdict)
    }

    @Test
    fun isReentrant_andStateless() = runTest {
        val checker = NoOpPlayIntegrityChecker()
        val v1 = checker.check(ScanIntegrityContext("a".repeat(64)))
        val v2 = checker.check(ScanIntegrityContext("b".repeat(64)))
        // Different inputs must not change the output. The NoOp must be a true constant.
        assertEquals(v1, v2)
    }
}
