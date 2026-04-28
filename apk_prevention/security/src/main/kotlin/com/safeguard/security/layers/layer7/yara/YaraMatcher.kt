package com.safeguard.security.layers.layer7.yara

/**
 * Runs a set of [YaraRule]s against one or more byte buffers and returns which rules
 * fired, plus a per-rule list of matched-string identifiers (for [Layer7Result] evidence).
 *
 * Performance model:
 *  - One match-set is built per rule, accumulating across all [scan] calls. So the caller
 *    can feed in `classes.dex`, `classes2.dex`, manifest, and native libs in sequence
 *    and a rule that asks for `2 of them` can match strings spread across artifacts.
 *  - For each input buffer the matcher walks the bytes **once** and at each offset asks:
 *    "does any literal/hex pattern start here?" This is O(N · P · L_max) worst case
 *    where N=buffer size, P=patterns, L_max=longest pattern. Concrete bundled rulesets
 *    have P < 50 and L_max < 64, so this is fine even on a large dex without an
 *    Aho-Corasick layer (which would be a fair next-step optimisation).
 *
 * Safety:
 *  - Each individual buffer is capped by [maxBytesPerBuffer] inside [YaraScanner];
 *    the matcher itself is unconditionally linear so a malicious input cannot push it
 *    into super-linear time.
 *  - Patterns longer than the buffer are silently skipped (cannot match anyway).
 */
internal class YaraMatcher(private val rules: List<YaraRule>) {

    /** Mutable per-rule set of matched string identifiers (`$a`, `$b`, …). */
    private val matchedByRule: Map<String, MutableSet<String>> =
        rules.associate { it.name to mutableSetOf() }

    /**
     * Index every string-pattern-form across all rules into a single flat list so we can
     * walk the input once per offset and check all patterns. Storing the originating
     * rule + identifier with each form is enough to update [matchedByRule] on a hit.
     */
    private data class IndexedPattern(
        val ruleName: String,
        val identifier: String,
        val form: LiteralOrHex,
        val nocase: Boolean
    )

    private val patterns: List<IndexedPattern> = rules.flatMap { rule ->
        rule.strings.flatMap { sp ->
            // Strip the `\u0001NOCASE` suffix the parser appended for nocase tracking.
            val baseId = sp.identifier.substringBefore('\u0001')
            val nocase = sp.identifier.endsWith("\u0001NOCASE")
            sp.matchableForms.map { form ->
                IndexedPattern(rule.name, baseId, form, nocase)
            }
        }
    }

    /** Feed a single byte buffer into the matcher. Idempotent across multiple calls. */
    fun scan(buffer: ByteArray, length: Int = buffer.size) {
        val bound = length.coerceAtMost(buffer.size)
        if (bound <= 0 || patterns.isEmpty()) return
        for (offset in 0 until bound) {
            for (p in patterns) {
                if (matchAt(buffer, bound, offset, p)) {
                    matchedByRule.getValue(p.ruleName).add(p.identifier)
                }
            }
        }
    }

    /** Evaluate every rule's condition against the accumulated match-set. */
    fun results(): List<RuleHit> = rules.mapNotNull { rule ->
        val matched = matchedByRule.getValue(rule.name)
        val total = rule.strings.size
        if (evaluate(rule.condition, matched, total)) {
            RuleHit(
                rule = rule,
                matchedIdentifiers = matched.toSortedSet().toList()
            )
        } else null
    }

    // ── matching primitives ────────────────────────────────────────────────────────────

    private fun matchAt(buf: ByteArray, len: Int, off: Int, p: IndexedPattern): Boolean {
        return when (val f = p.form) {
            is LiteralOrHex.Literal -> matchLiteral(buf, len, off, f.bytes, p.nocase)
            is LiteralOrHex.Hex -> matchHex(buf, len, off, f.pattern)
        }
    }

    private fun matchLiteral(
        buf: ByteArray,
        len: Int,
        off: Int,
        needle: ByteArray,
        nocase: Boolean
    ): Boolean {
        if (off + needle.size > len) return false
        if (!nocase) {
            for (i in needle.indices) if (buf[off + i] != needle[i]) return false
            return true
        }
        for (i in needle.indices) {
            val a = buf[off + i].toInt() and 0xFF
            val b = needle[i].toInt() and 0xFF
            if (toLowerByte(a) != toLowerByte(b)) return false
        }
        return true
    }

    private fun toLowerByte(b: Int): Int =
        if (b in 'A'.code..'Z'.code) b + ('a'.code - 'A'.code) else b

    private fun matchHex(buf: ByteArray, len: Int, off: Int, hex: HexBytePattern): Boolean {
        if (off + hex.size > len) return false
        for (i in 0 until hex.size) {
            if (!hex.mask[i]) continue
            if (buf[off + i] != hex.bytes[i]) return false
        }
        return true
    }

    // ── condition evaluation ───────────────────────────────────────────────────────────

    private fun evaluate(c: YaraCondition, matched: Set<String>, total: Int): Boolean {
        return when (c) {
            is YaraCondition.StringRef -> matched.contains(c.identifier)
            YaraCondition.AnyOfThem -> matched.isNotEmpty()
            YaraCondition.AllOfThem -> matched.size >= total
            is YaraCondition.NOfThem -> matched.size >= c.n
            is YaraCondition.Not -> !evaluate(c.inner, matched, total)
            is YaraCondition.And -> evaluate(c.left, matched, total) && evaluate(c.right, matched, total)
            is YaraCondition.Or -> evaluate(c.left, matched, total) || evaluate(c.right, matched, total)
        }
    }
}

internal data class RuleHit(
    val rule: YaraRule,
    val matchedIdentifiers: List<String>
)
