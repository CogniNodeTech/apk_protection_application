package com.safeguard.mlmodel

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.security.MessageDigest

private const val MODEL_ASSET = "malware_detector_v3.tflite.enc"
private const val EXPECTED_HASH_ASSET = "model_expected_sha256.txt"

class TFLiteRunner(
    private val context: Context,
    private val modelDecryptor: ModelDecryptor
) {

    private var interpreter: Interpreter? = loadModel()

    private fun loadModel(): Interpreter? {
        return try {
            val declaredLength = context.assets.openFd(MODEL_ASSET).length
            if (!verifyModelIntegrity(declaredLength)) return null
            val encryptedBytes = context.assets.open(MODEL_ASSET).use { it.readBytes() }
            val decryptedBytes = modelDecryptor.decryptModel(encryptedBytes) ?: return null
            
            // Allocate direct buffer for the interpreter
            val buffer = ByteBuffer.allocateDirect(decryptedBytes.size)
            buffer.order(ByteOrder.nativeOrder())
            buffer.put(decryptedBytes)
            buffer.rewind()
            
            Interpreter(buffer)
        } catch (e: Exception) {
            // Log fallback or failure (in production we shouldn't fail silently if it's a security app)
            null
        }
    }

    /** If model_expected_sha256.txt exists and is non-blank, verifies the model file matches; otherwise allows load. Fail load on mismatch. */
    private fun verifyModelIntegrity(declaredLength: Long): Boolean {
        return try {
            val expectedHash = context.assets.open(EXPECTED_HASH_ASSET).bufferedReader().use { it.readText().trim() }
            if (expectedHash.isBlank()) return true
            val assetFd = context.assets.openFd(MODEL_ASSET)
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(assetFd.fileDescriptor).use { input ->
                val channel = input.channel
                val buffer = channel.map(FileChannel.MapMode.READ_ONLY, assetFd.startOffset, declaredLength)
                digest.update(buffer)
            }
            val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
            actualHash.equals(expectedHash, ignoreCase = true)
        } catch (_: Exception) {
            // Hash file missing or unreadable: allow load. Hash file present but verify failed: return false above.
            true
        }
    }

    fun run(features: FloatArray): FloatArray? {
        val interp = interpreter ?: return null
        return try {
            val inputBuffer = ByteBuffer.allocateDirect(features.size * 4).order(ByteOrder.nativeOrder())
            features.forEach { inputBuffer.putFloat(it) }
            inputBuffer.rewind()
            val output = Array(1) { FloatArray(2) }
            interp.run(inputBuffer, output)
            output[0]
        } catch (e: Exception) {
            null
        }
    }

    fun isLoaded(): Boolean = interpreter != null
}
