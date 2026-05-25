package com.safeguard.security.layers.layer4

import com.safeguard.core.domain.model.APKContext
import com.safeguard.core.domain.model.Verdict
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SignatureValidatorTest {

    @Test
    fun unsignedApk_noMetaInfCert_returnsMalicious() = runTest {
        val file = File.createTempFile("app", ".apk")
        file.outputStream().use { out ->
            ZipOutputStream(out).use { zos ->
                zos.putNextEntry(ZipEntry("AndroidManifest.xml"))
                zos.write(ByteArray(10))
                zos.closeEntry()
            }
        }
        val validator = SignatureValidator(emptySet())

        val result = validator.verify(APKContext(file), emptyList())

        assertEquals(Verdict.MALICIOUS, result.verdict)
        assertEquals(1.0f, result.confidence, 0.01f)
        assertEquals(100, result.riskScore)
        assert(result.evidence.any { it.contains("Unsigned") })
        file.delete()
    }

}
