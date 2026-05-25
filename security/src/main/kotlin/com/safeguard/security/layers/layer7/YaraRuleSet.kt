package com.safeguard.security.layers.layer7

import android.content.Context
import com.safeguard.security.layers.layer7.yara.YaraRule
import com.safeguard.security.layers.layer7.yara.YaraRuleParser
import java.io.IOException

/**
 * Loads + caches the bundled YARA-subset rules for [YaraScanner]. Rules live as `*.yar`
 * files under `assets/yara/` in the security module.
 *
 * Parse failures are **fatal at load time** rather than silently skipped — a bad bundled
 * rule is a release-engineering bug we want to catch in QA, not a quietly-broken layer
 * in the wild. If the asset directory is missing entirely (e.g. a stripped build) we
 * fall back to an empty set and Layer 7 returns SAFE, so a misconfigured build never
 * blocks the whole pipeline.
 */
class YaraRuleSet private constructor(
    internal val rules: List<YaraRule>
) {
    /** Number of rules in this set — exposed to higher modules for telemetry / docs. */
    val ruleCount: Int get() = rules.size

    /** Parsed rule names (e.g. for an admin-style "what's loaded?" debug view). */
    val ruleNames: List<String> get() = rules.map { it.name }

    companion object {
        private const val ASSET_DIR = "yara"

        /** Load + parse every `*.yar` file under `assets/yara/`. */
        fun fromAssets(context: Context): YaraRuleSet {
            val files = try {
                context.assets.list(ASSET_DIR)?.filter { it.endsWith(".yar") }.orEmpty()
            } catch (_: IOException) {
                emptyList()
            }
            if (files.isEmpty()) return YaraRuleSet(emptyList())

            val rules = mutableListOf<YaraRule>()
            for (name in files) {
                val src = context.assets.open("$ASSET_DIR/$name").bufferedReader().use { it.readText() }
                rules += YaraRuleParser.parseAll(src)
            }
            // Reject duplicates across files for the same reason as within a single file.
            val dupes = rules.groupBy { it.name }.filterValues { it.size > 1 }.keys
            require(dupes.isEmpty()) {
                "Duplicate YARA rule names across asset files: ${dupes.joinToString()}"
            }
            return YaraRuleSet(rules)
        }

        /** Test-only entry point: parse a list of source strings instead of asset files. */
        internal fun fromSources(sources: List<String>): YaraRuleSet {
            val rules = sources.flatMap { YaraRuleParser.parseAll(it) }
            return YaraRuleSet(rules)
        }
    }
}
