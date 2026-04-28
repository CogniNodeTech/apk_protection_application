package com.safeguard.security.layers.layer7.yara

/**
 * Parsed representation of a YARA-subset rule.
 *
 * SafeGuard intentionally does **not** ship a full YARA engine (libyara JNI is ~6 MiB
 * per ABI and pulls in modules we don't need). Instead Layer 7 understands a deliberate
 * subset that covers >95% of public Android YARA rulesets seen in the wild:
 *
 *  - `meta:` block (informational only — drives [Layer7Result] evidence + threat name)
 *  - `strings:` block with literal ASCII / UTF-16 LE strings (`ascii`, `wide`, `nocase`
 *    modifiers) **and** hex byte sequences with `??` wildcards
 *  - `condition:` expression: `any of them`, `all of them`, `N of them`, single
 *    `$identifier` references, `not`, `and`, `or`, parentheses
 *
 * Anything outside the subset (regex strings, file modules, math expressions, imports)
 * is rejected at parse time so a bad rule fails *loud* during ruleset load instead of
 * silently doing nothing at scan time.
 *
 * Severity convention: rules without `meta: severity = N` default to 80. Layer 7 maps
 * severity directly into its risk score and into the `Verdict` mapping (>= 90 = MALICIOUS,
 * >= 60 = SUSPICIOUS).
 */
internal data class YaraRule(
    val name: String,
    val meta: YaraMeta,
    val strings: List<YaraStringPattern>,
    val condition: YaraCondition
)

internal data class YaraMeta(
    val author: String? = null,
    val description: String? = null,
    val family: String? = null,
    val severity: Int = DEFAULT_SEVERITY,
    val reference: String? = null,
    val extras: Map<String, String> = emptyMap()
) {
    companion object {
        const val DEFAULT_SEVERITY: Int = 80
    }
}

/**
 * A single addressable string in `strings:`. The [identifier] is the `$xxx` token and is
 * what conditions reference. [matchableForms] is the **byte** representations the matcher
 * actually scans for (e.g. UTF-8 + UTF-16 LE for `wide`, or all-cases generated up-front
 * for `nocase` literal strings).
 *
 * For hex patterns, [matchableForms] always has exactly one element which is the hex
 * pattern compiled to a [HexBytePattern] wrapped in a [LiteralOrHex.Hex].
 */
internal data class YaraStringPattern(
    val identifier: String,
    val matchableForms: List<LiteralOrHex>
) {
    init {
        require(identifier.startsWith("$")) {
            "YARA string identifiers must start with '$', got: $identifier"
        }
        require(matchableForms.isNotEmpty()) {
            "Rule string $identifier has no compiled forms"
        }
    }
}

/**
 * A single concrete byte-pattern the matcher hunts for. Multiple of these can back the
 * same logical [YaraStringPattern] (UTF-8 + UTF-16 LE; lowercase + uppercase for nocase
 * short literals).
 */
internal sealed interface LiteralOrHex {
    /** Plain literal bytes (case-sensitive) — match-or-don't, no wildcards. */
    data class Literal(val bytes: ByteArray) : LiteralOrHex {
        override fun equals(other: Any?): Boolean =
            other is Literal && bytes.contentEquals(other.bytes)

        override fun hashCode(): Int = bytes.contentHashCode()
    }

    /** Hex pattern with optional `??` wildcards encoded via [HexBytePattern]. */
    data class Hex(val pattern: HexBytePattern) : LiteralOrHex
}

/**
 * Compiled hex pattern with a per-byte mask. `bytes[i]` is only meaningful where
 * `mask[i] == true`; positions where `mask[i] == false` are wildcards (`??`) and match
 * any byte at that offset.
 *
 * Length-zero patterns are forbidden at parse time (a rule with no strings is a parse
 * error caught upstream).
 */
internal data class HexBytePattern(
    val bytes: ByteArray,
    val mask: BooleanArray
) {
    init {
        require(bytes.size == mask.size) {
            "HexBytePattern bytes/mask size mismatch: ${bytes.size} vs ${mask.size}"
        }
        require(bytes.isNotEmpty()) { "HexBytePattern must not be empty" }
    }

    val size: Int get() = bytes.size

    override fun equals(other: Any?): Boolean =
        other is HexBytePattern &&
            bytes.contentEquals(other.bytes) &&
            mask.contentEquals(other.mask)

    override fun hashCode(): Int = 31 * bytes.contentHashCode() + mask.contentHashCode()
}

/**
 * Boolean expression tree for a rule's `condition:` clause. Evaluated against the *set*
 * of [YaraStringPattern.identifier]s that matched anywhere in the input.
 */
internal sealed interface YaraCondition {
    /** Single string reference: `$a`. True iff the matcher saw `$a` somewhere. */
    data class StringRef(val identifier: String) : YaraCondition

    /** `any of them`. */
    data object AnyOfThem : YaraCondition

    /** `all of them`. */
    data object AllOfThem : YaraCondition

    /** `N of them` with a literal integer count. */
    data class NOfThem(val n: Int) : YaraCondition

    data class Not(val inner: YaraCondition) : YaraCondition
    data class And(val left: YaraCondition, val right: YaraCondition) : YaraCondition
    data class Or(val left: YaraCondition, val right: YaraCondition) : YaraCondition
}
