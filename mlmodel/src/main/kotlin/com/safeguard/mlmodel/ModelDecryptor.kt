package com.safeguard.mlmodel

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelDecryptor @Inject constructor(private val context: Context) {

    private val ALGORITHM = "AES/GCM/NoPadding"
    private val TAG_LENGTH = 128
    
    /**
     * Decrypts an encrypted asset using a key derived from the app's signing signature.
     * This ensures the model cannot be used in a cloned or re-signed app.
     */
    fun decryptModel(encryptedData: ByteArray): ByteArray? {
        return try {
            val key = deriveKeyFromSignature() ?: return null
            
            // Extract IV (first 12 bytes for GCM)
            val iv = encryptedData.copyOfRange(0, 12)
            val cipherText = encryptedData.copyOfRange(12, encryptedData.size)
            
            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), spec)
            
            cipher.doFinal(cipherText)
        } catch (e: Exception) {
            null
        }
    }

    private fun deriveKeyFromSignature(): ByteArray? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            signatures?.firstOrNull()?.let { sig ->
                val md = MessageDigest.getInstance("SHA-256")
                // Use signature + package name as entropy for the key
                md.update(sig.toByteArray())
                md.update(context.packageName.toByteArray())
                md.digest() // Returns 32 bytes (AES-256)
            }
        } catch (e: Exception) {
            null
        }
    }
}
