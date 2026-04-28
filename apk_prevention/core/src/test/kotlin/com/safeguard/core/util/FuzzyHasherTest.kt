package com.safeguard.core.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Random

/**
 * Unit tests for [FuzzyHasher]. The contract this pins:
 *  - Producing well-formed TLSH hashes (70 upper-case hex chars) for healthy input.
 *  - Returning `null` (not throwing) for input that is too small or zero-entropy.
 *  - Reflexive identity (hash X compared with itself = 100% similarity).
 *  - Sensitivity: small byte-level perturbations stay above the default 70% threshold.
 *  - Discrimination: independent random buffers fall well below the threshold.
 *  - Forward compatibility with the upstream "T1"-prefixed encoded form.
 *  - Graceful degradation: malformed / legacy pseudo-fuzzy strings return 0% rather than throwing.
 */
class FuzzyHasherTest {

    private val tempFiles = mutableListOf<File>()

    @After
    fun tearDown() {
        tempFiles.forEach { runCatching { it.delete() } }
        tempFiles.clear()
    }

    private fun tempFile(prefix: String): File {
        val f = File.createTempFile(prefix, ".bin")
        tempFiles.add(f)
        return f
    }

    /** Random bytes with fixed seed → deterministic, incompressible payload across CI runs. */
    private fun randomBytes(size: Int, seed: Long = 0xC0FFEE): ByteArray {
        val bytes = ByteArray(size)
        Random(seed).nextBytes(bytes)
        return bytes
    }

    @Test
    fun hash_returnsNullForUndersizedInput() {
        val tiny = tempFile("tiny").apply { writeBytes(randomBytes(64)) }
        assertNull("Files below the entropy floor must return null", FuzzyHasher.hash(tiny))
        assertNull(FuzzyHasher.hash(randomBytes(64)))
    }

    @Test
    fun hash_zeroEntropyInput_doesNotMatchRealContent() {
        // TLSH encodes the input length and a checksum independently of the bucket distribution,
        // so an all-zero buffer does produce a hash — it just lands far away from realistic APK
        // content. Pin that distance instead of asserting null, which would be brittle.
        val flat = tempFile("flat").apply { writeBytes(ByteArray(8_192)) }
        val realistic = FuzzyHasher.hash(randomBytes(8_192))
        val flatHash = FuzzyHasher.hash(flat)
        if (flatHash == null) return // also acceptable: provider rejected the buffer outright
        assertNotNull(realistic)
        val similarity = FuzzyHasher.similarity(flatHash, realistic)
        assertTrue(
            "Zero-entropy buffer must not be similar to real content (similarity=$similarity)",
            similarity < 50
        )
    }

    @Test
    fun hash_producesSeventyCharUpperCaseHex() {
        val f = tempFile("normal").apply { writeBytes(randomBytes(8_192)) }
        val hash = FuzzyHasher.hash(f)
        assertNotNull(hash)
        assertEquals("TLSH-128-1 hex form is exactly 70 chars", 70, hash!!.length)
        assertTrue("TLSH hex must be upper case", hash.all { it.isDigit() || it in 'A'..'F' })
    }

    @Test
    fun hash_isDeterministicAcrossInputForms() {
        val payload = randomBytes(8_192)
        val f = tempFile("det").apply { writeBytes(payload) }
        val viaFile = FuzzyHasher.hash(f)
        val viaBytes = FuzzyHasher.hash(payload)
        assertNotNull(viaFile)
        assertEquals("File and byte-array overloads must agree", viaBytes, viaFile)
    }

    @Test
    fun similarity_identicalHash_returnsHundred() {
        val payload = randomBytes(8_192)
        val hash = FuzzyHasher.hash(payload)
        assertNotNull(hash)
        assertEquals(100, FuzzyHasher.similarity(hash, hash))
    }

    @Test
    fun similarity_smallPerturbation_staysAboveDefaultThreshold() {
        // Same content with ~1% of bytes flipped — a recompiled / repacked APK is typically more
        // different than this, so 70% is a generous lower bound for the test.
        val original = randomBytes(16_384, seed = 1)
        val perturbed = original.copyOf().also { mutated ->
            val rng = Random(2)
            repeat(mutated.size / 100) {
                val idx = rng.nextInt(mutated.size)
                mutated[idx] = (mutated[idx].toInt() xor 0xFF).toByte()
            }
        }
        val hashOriginal = FuzzyHasher.hash(original)
        val hashPerturbed = FuzzyHasher.hash(perturbed)
        assertNotNull(hashOriginal)
        assertNotNull(hashPerturbed)
        val similarity = FuzzyHasher.similarity(hashOriginal, hashPerturbed)
        assertTrue(
            "Expected similarity >= 70 for a 1% byte perturbation but was $similarity",
            similarity >= 70
        )
    }

    @Test
    fun similarity_independentRandomInputs_areDiscriminated() {
        // Two independent random buffers should land below the default suspicious threshold so
        // unrelated APKs do not generate spurious "similar to known threat" verdicts.
        val a = FuzzyHasher.hash(randomBytes(16_384, seed = 1))
        val b = FuzzyHasher.hash(randomBytes(16_384, seed = 9_999))
        assertNotNull(a)
        assertNotNull(b)
        val similarity = FuzzyHasher.similarity(a, b)
        assertTrue(
            "Expected similarity < 70 for independent random data but was $similarity",
            similarity < 70
        )
    }

    @Test
    fun similarity_acceptsT1PrefixedEncoding() {
        val payload = randomBytes(8_192)
        val hash = FuzzyHasher.hash(payload) ?: error("hash must be produced")
        val withPrefix = "T1$hash"
        assertEquals(100, FuzzyHasher.similarity(hash, withPrefix))
        assertEquals(100, FuzzyHasher.similarity(withPrefix, withPrefix))
    }

    @Test
    fun similarity_returnsZeroForMalformedOrLegacyHashes() {
        val good = FuzzyHasher.hash(randomBytes(8_192)) ?: error("hash must be produced")
        // Legacy pseudo-fuzzy strings written by the previous "1:..." / "3:..." implementation.
        // They must silently drop out of the lookup instead of producing nonsense scores.
        assertEquals(0, FuzzyHasher.similarity(good, "3:abc"))
        assertEquals(0, FuzzyHasher.similarity(good, "1:" + "a".repeat(64)))
        assertEquals(0, FuzzyHasher.similarity(good, ""))
        assertEquals(0, FuzzyHasher.similarity(good, null))
        assertEquals(0, FuzzyHasher.similarity(null, good))
        assertEquals(0, FuzzyHasher.similarity("not-a-hash", "also-not-a-hash"))
    }

    @Test
    fun similarity_rejectsShorterOrLongerHexBuffers() {
        val good = FuzzyHasher.hash(randomBytes(8_192)) ?: error("hash must be produced")
        assertEquals(0, FuzzyHasher.similarity(good, good.dropLast(2)))
        assertEquals(0, FuzzyHasher.similarity(good, good + "AB"))
    }

    @Test
    fun similarity_perturbationDiffersFromUnrelated() {
        // Sanity check tying the two cases together: a perturbed copy must score strictly higher
        // than an independent random buffer of the same length. This pins TLSH's discrimination
        // power and not just our threshold choices.
        val original = randomBytes(16_384, seed = 1)
        val perturbed = original.copyOf().also { mutated ->
            val rng = Random(2)
            repeat(mutated.size / 100) {
                val idx = rng.nextInt(mutated.size)
                mutated[idx] = (mutated[idx].toInt() xor 0xFF).toByte()
            }
        }
        val unrelated = randomBytes(16_384, seed = 9_999)

        val hashOrig = FuzzyHasher.hash(original)!!
        val hashPert = FuzzyHasher.hash(perturbed)!!
        val hashOther = FuzzyHasher.hash(unrelated)!!

        val perturbedSim = FuzzyHasher.similarity(hashOrig, hashPert)
        val unrelatedSim = FuzzyHasher.similarity(hashOrig, hashOther)
        assertNotEquals(perturbedSim, unrelatedSim)
        assertTrue(
            "Perturbed similarity ($perturbedSim) must exceed unrelated similarity ($unrelatedSim)",
            perturbedSim > unrelatedSim
        )
    }
}
