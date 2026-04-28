package com.safeguard.security.layers.layer1

import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.Verdict
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class Layer1FileMonitorTest {

    @Test
    fun suspiciousFilenameAndTinyFile_triggersHighRiskVerdict() = runTest {
        val file = File.createTempFile("update_mod_patch", ".apk").apply {
            writeBytes(ByteArray(64))
        }
        val layer = Layer1FileMonitor()
        val result = layer.verify(APKContext(file), emptyList())
        assertEquals(Verdict.MALICIOUS, result.verdict)
        file.delete()
    }

    @Test
    fun benignFile_returnsSafe() = runTest {
        val file = File.createTempFile("benign_release", ".apk").apply {
            writeBytes(ByteArray(2_000_000))
        }
        val layer = Layer1FileMonitor()
        val result = layer.verify(APKContext(file), emptyList())
        assertEquals(Verdict.SAFE, result.verdict)
        file.delete()
    }
}

