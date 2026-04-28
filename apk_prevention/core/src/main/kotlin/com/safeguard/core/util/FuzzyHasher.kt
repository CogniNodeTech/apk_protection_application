package com.safeguard.core.util

import app.keve.ktlsh.TLSHUtil
import java.io.File
import java.security.MessageDigest
import java.security.Provider
import java.security.Security

/**
 * TLSH-based fuzzy hashing for APK similarity detection.
 *
 * Trend Locality Sensitive Hash (TLSH) is the published, peer-reviewed standard for binary
 * similarity. Two APKs that belong to the same malware family (repacked, resigned, recompiled
 * with different obfuscation) produce TLSH hashes whose distance is small, even though their
 * SHA-256 digests diverge completely. The algorithm is documented at
 * https://github.com/trendmicro/tlsh.
 *
 * Hash format here: 70 upper-case hex characters (kTLSH "TLSH-128-1" variant). A leading
 * "T1" prefix is also accepted as input for forward compatibility with the reference
 * implementation's encoded form.
 *
 * Score → similarity mapping is linear: `similarity_pct = max(0, 100 - score / 3)`:
 *  - score 0    → 100% (identical)
 *  - score 30   →  90% (very similar — typical for repacked variants of the same family)
 *  - score 90   →  70% (similar — the default `minSimilarity` threshold in HashValidator)
 *  - score 300+ →   0% (unrelated)
 */
object FuzzyHasher {

    /** TLSH-128-1: 128 buckets, 1-byte checksum, 5-byte sliding window. Default profile. */
    private const val ALGORITHM = "TLSH-128-1"

    /**
     * Inputs smaller than this are skipped: TLSH needs enough data to produce a stable
     * bucket distribution. The reference implementation requires ≥ 50 bytes of varied
     * input; we use 256 to keep noise out of the lookup index.
     */
    private const val MIN_INPUT_BYTES = 256L

    private const val BUFFER_SIZE = 8192
    private const val HEX_LEN = 70
    private const val SCORE_DIVISOR = 3
    private const val MAX_SIMILARITY = 100

    /**
     * Cached provider name (kTLSH publishes "K"). Resolved lazily so a missing provider
     * surfaces as a `null` digest rather than a class-init failure that takes down the
     * entire app.
     */
    private val providerName: String? by lazy { registerProvider() }

    /**
     * Registers the kTLSH `java.security.Provider` directly via reflection. The library's own
     * [TLSHUtil.registerProvider] uses `ServiceLoader<Provider>`, which does not reliably fire
     * inside the Android Gradle Plugin's unit-test classloader (and can also be stripped by R8
     * on release builds). Loading the provider class explicitly side-steps both issues.
     *
     * Returns the registered provider name, or `null` if the library is not on the classpath
     * (e.g. tests in a module that excluded it). All hash/similarity calls become no-ops in
     * that case.
     */
    private fun registerProvider(): String? = runCatching {
        val name = TLSHUtil.providerNameK()
        if (Security.getProvider(name) != null) return@runCatching name
        // Try the upstream service-loader path first; if it does not pick the provider up, fall
        // back to instantiating it reflectively.
        TLSHUtil.registerProvider()
        if (Security.getProvider(name) != null) return@runCatching name
        val providerClass = Class.forName("app.keve.ktlsh.spi.KProvider")
        val provider = providerClass.getDeclaredConstructor().newInstance() as Provider
        Security.addProvider(provider)
        name
    }.getOrNull()

    /**
     * Compute the TLSH hash of [file].
     *
     * Returns `null` if the file is unreadable, smaller than [MIN_INPUT_BYTES], lacks the
     * entropy required by TLSH, or the digest could not be computed for any other reason.
     */
    fun hash(file: File): String? {
        if (!file.isFile || !file.canRead()) return null
        if (file.length() < MIN_INPUT_BYTES) return null
        val digest = newDigest() ?: return null
        return runCatching {
            file.inputStream().use { input ->
                val buffer = ByteArray(BUFFER_SIZE)
                var read: Int
                while (input.read(buffer).also { read = it } > 0) {
                    digest.update(buffer, 0, read)
                }
            }
            encodeOrNull(digest.digest())
        }.getOrNull()
    }

    /**
     * Compute the TLSH hash of an in-memory byte array. Mainly for tests and small payloads.
     */
    fun hash(bytes: ByteArray): String? {
        if (bytes.size < MIN_INPUT_BYTES) return null
        val digest = newDigest() ?: return null
        return runCatching {
            digest.update(bytes)
            encodeOrNull(digest.digest())
        }.getOrNull()
    }

    /**
     * Compute the similarity (0..100, higher = more similar) between two TLSH hash strings.
     *
     * Returns `0` if either hash is `null`, blank, or malformed (which includes legacy pseudo-fuzzy
     * entries written before TLSH was introduced, so existing rows in the threat DB silently
     * stop matching instead of producing nonsensical scores).
     */
    fun similarity(hashA: String?, hashB: String?): Int {
        val a = parseHash(hashA) ?: return 0
        val b = parseHash(hashB) ?: return 0
        val score = runCatching { TLSHUtil.score(a, b, /* lenDiff = */ true) }.getOrNull() ?: return 0
        return (MAX_SIMILARITY - score / SCORE_DIVISOR).coerceIn(0, MAX_SIMILARITY)
    }

    private fun newDigest(): MessageDigest? {
        val provider = providerName ?: return null
        return MessageDigest.getInstance(ALGORITHM, provider)
    }

    private fun encodeOrNull(raw: ByteArray): String? {
        if (raw.isEmpty()) return null
        // Defensive guard: kTLSH never returns an all-zero buffer for normal input, but we keep
        // the check so a future spec change or unexpected provider behaviour does not pollute
        // the index with hashes that match every other zeroed-out row.
        if (raw.all { it == 0.toByte() }) return null
        return TLSHUtil.encoded(raw)
    }

    private fun parseHash(hash: String?): ByteArray? {
        if (hash.isNullOrBlank()) return null
        val hex = hash.trim().let {
            when {
                it.startsWith("T1", ignoreCase = true) -> it.substring(2)
                else -> it
            }
        }
        if (hex.length != HEX_LEN) return null
        if (!hex.all { it.isHexDigit() }) return null
        return runCatching { TLSHUtil.hexToBytes(hex) }.getOrNull()
    }

    private fun Char.isHexDigit(): Boolean =
        this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
}
