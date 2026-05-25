package com.safeguard.security.layers.layer7.yara

/**
 * Tiny hand-rolled parser for a YARA-subset DSL. See [YaraRule] for the grammar we accept.
 * Hand-rolled (instead of ANTLR / generated) for three reasons:
 *
 *  1. Build-time cost: no extra Kotlin/Java codegen step on the security module.
 *  2. Failure modes: parse errors must surface during ruleset load with the exact line /
 *     identifier so a bad bundled rule is fixable from a logcat trace alone, not a
 *     stack-trace into a generated parser.
 *  3. Surface area: the subset is ~12 productions, generator overhead isn't worth it.
 *
 * The parser is **strict** — anything outside the documented subset is rejected so a rule
 * that *looks* like full-YARA but secretly never matches isn't shipped to production.
 */
internal object YaraRuleParser {

    /** Parse zero-or-more rules out of a single source string (asset file contents). */
    fun parseAll(source: String): List<YaraRule> {
        val tokens = Tokenizer(source).tokenize()
        val parser = ParserState(tokens)
        val rules = mutableListOf<YaraRule>()
        while (!parser.eof()) {
            rules += parser.parseRule()
        }
        // Reject duplicate rule names — silently shadowing a rule causes nasty
        // false-negatives that are very hard to debug at scan time.
        val dupes = rules.groupBy { it.name }.filterValues { it.size > 1 }.keys
        require(dupes.isEmpty()) {
            "Duplicate YARA rule names in source: ${dupes.joinToString()}"
        }
        return rules
    }

    fun parseSingle(source: String): YaraRule = parseAll(source).single()

    // ── tokenizer ──────────────────────────────────────────────────────────────────────

    private enum class TokenType {
        IDENT, STRING, NUMBER, HEX_BLOCK,
        LBRACE, RBRACE, LPAREN, RPAREN, EQ, COLON,
        EOF
    }

    /**
     * A single token with its source-position bounds. [start] / [end] (exclusive) are
     * absolute character offsets in the original source — used to slice raw hex content
     * out of the source between matching `{` / `}` braces (the hex value of a string
     * definition).
     */
    private data class Token(
        val type: TokenType,
        val text: String,
        val line: Int,
        val start: Int,
        val end: Int
    )

    private class Tokenizer(private val src: String) {
        private var pos = 0
        private var line = 1

        fun tokenize(): List<Token> {
            val out = mutableListOf<Token>()
            while (pos < src.length) {
                skipWhitespaceAndComments()
                if (pos >= src.length) break
                val tokStart = pos
                val c = src[pos]
                when {
                    // `{` is a hex-block opener iff the previous emitted token was `=`
                    // (i.e. we're in `$x = { ... }`). Otherwise it's the rule body brace.
                    // Looking-back at the last token (instead of carrying parser state)
                    // keeps the tokenizer pure but still context-correct for our subset.
                    c == '{' && out.lastOrNull()?.type == TokenType.EQ -> {
                        out += readHexBlock(tokStart)
                    }
                    c == '{' -> { out += Token(TokenType.LBRACE, "{", line, tokStart, ++pos) }
                    c == '}' -> { out += Token(TokenType.RBRACE, "}", line, tokStart, ++pos) }
                    c == '(' -> { out += Token(TokenType.LPAREN, "(", line, tokStart, ++pos) }
                    c == ')' -> { out += Token(TokenType.RPAREN, ")", line, tokStart, ++pos) }
                    c == ':' -> { out += Token(TokenType.COLON, ":", line, tokStart, ++pos) }
                    c == '=' -> { out += Token(TokenType.EQ, "=", line, tokStart, ++pos) }
                    c == '/' -> {
                        // Single '/' alone is reserved for regex strings, which are NOT
                        // part of our subset — surface a sharp error.
                        error("Regex strings are not supported by SafeGuard's YARA subset (line $line)")
                    }
                    c == '"' -> out += readString(tokStart)
                    c.isDigit() -> out += readNumber(tokStart)
                    isIdentStart(c) -> out += readIdent(tokStart)
                    else -> error("Unexpected character '$c' at line $line")
                }
            }
            out += Token(TokenType.EOF, "", line, src.length, src.length)
            return out
        }

        private fun readHexBlock(tokStart: Int): Token {
            val startLine = line
            // pos currently at '{'. Scan forward to the matching '}' (depth-tracked even
            // though our subset doesn't use nested braces — keeps us safe if it ever does).
            var depth = 1
            var p = pos + 1
            while (p < src.length && depth > 0) {
                val ch = src[p]
                if (ch == '{') depth++
                else if (ch == '}') depth--
                if (depth == 0) break
                if (ch == '\n') line++
                p++
            }
            require(p < src.length) { "Unterminated '{' at line $startLine" }
            val raw = src.substring(pos + 1, p)
            // p is at the matching '}'; advance past it.
            val end = p + 1
            pos = end
            return Token(TokenType.HEX_BLOCK, raw, startLine, tokStart, end)
        }

        private fun skipWhitespaceAndComments() {
            while (pos < src.length) {
                val c = src[pos]
                if (c.isWhitespace()) {
                    if (c == '\n') line++
                    pos++
                } else if (c == '/' && pos + 1 < src.length && src[pos + 1] == '/') {
                    while (pos < src.length && src[pos] != '\n') pos++
                } else if (c == '/' && pos + 1 < src.length && src[pos + 1] == '*') {
                    pos += 2
                    while (pos + 1 < src.length && !(src[pos] == '*' && src[pos + 1] == '/')) {
                        if (src[pos] == '\n') line++
                        pos++
                    }
                    pos += 2
                } else break
            }
        }

        private fun readString(tokStart: Int): Token {
            val startLine = line
            pos++ // consume opening "
            val sb = StringBuilder()
            while (pos < src.length && src[pos] != '"') {
                val ch = src[pos]
                if (ch == '\\' && pos + 1 < src.length) {
                    sb.append(
                        when (val esc = src[pos + 1]) {
                            'n' -> '\n'; 'r' -> '\r'; 't' -> '\t'; '\\' -> '\\'; '"' -> '"'
                            // \xNN hex byte
                            'x' -> {
                                require(pos + 3 < src.length) { "Truncated \\x escape at line $line" }
                                val hex = src.substring(pos + 2, pos + 4)
                                pos += 2 // additional advance for the two hex digits
                                hex.toInt(16).toChar()
                            }
                            else -> error("Unknown string escape '\\$esc' at line $line")
                        }
                    )
                    pos += 2
                } else {
                    if (ch == '\n') line++
                    sb.append(ch)
                    pos++
                }
            }
            require(pos < src.length) { "Unterminated string at line $startLine" }
            pos++ // consume closing "
            return Token(TokenType.STRING, sb.toString(), startLine, tokStart, pos)
        }

        private fun readNumber(tokStart: Int): Token {
            while (pos < src.length && src[pos].isDigit()) pos++
            return Token(TokenType.NUMBER, src.substring(tokStart, pos), line, tokStart, pos)
        }

        private fun readIdent(tokStart: Int): Token {
            // Allow leading $ for string ids, otherwise standard ident chars.
            if (src[pos] == '$') pos++
            while (pos < src.length && isIdentPart(src[pos])) pos++
            return Token(TokenType.IDENT, src.substring(tokStart, pos), line, tokStart, pos)
        }

        private fun isIdentStart(c: Char): Boolean =
            c.isLetter() || c == '_' || c == '$'

        private fun isIdentPart(c: Char): Boolean =
            c.isLetterOrDigit() || c == '_'
    }

    // ── parser ─────────────────────────────────────────────────────────────────────────

    private class ParserState(private val tokens: List<Token>) {
        private var idx = 0

        fun eof(): Boolean = peek().type == TokenType.EOF

        fun parseRule(): YaraRule {
            expectIdent("rule")
            val name = expect(TokenType.IDENT).text
            require(name != "rule") { "Rule cannot be named 'rule'" }
            expect(TokenType.LBRACE)

            var meta = YaraMeta()
            var strings: List<YaraStringPattern> = emptyList()
            var condition: YaraCondition? = null

            while (peek().type != TokenType.RBRACE && !eof()) {
                val section = expect(TokenType.IDENT).text
                expect(TokenType.COLON)
                when (section) {
                    "meta" -> meta = parseMeta()
                    "strings" -> strings = parseStrings()
                    "condition" -> condition = parseCondition()
                    else -> error("Unknown rule section '$section' (line ${tokens[idx].line})")
                }
            }
            expect(TokenType.RBRACE)
            requireNotNull(condition) { "Rule '$name' is missing a condition: clause" }
            require(strings.isNotEmpty()) { "Rule '$name' has no strings: clause" }
            return YaraRule(name, meta, strings, condition)
        }

        private fun parseMeta(): YaraMeta {
            var author: String? = null
            var description: String? = null
            var family: String? = null
            var severity: Int = YaraMeta.DEFAULT_SEVERITY
            var reference: String? = null
            val extras = mutableMapOf<String, String>()
            // Stop at the start of the next section (`strings:` / `condition:`) or rule end.
            while (!isSectionStart() && peek().type != TokenType.RBRACE && !eof()) {
                val key = expect(TokenType.IDENT).text
                expect(TokenType.EQ)
                val valTok = next()
                val value = when (valTok.type) {
                    TokenType.STRING -> valTok.text
                    TokenType.NUMBER -> valTok.text
                    else -> error("Meta value must be string or number (line ${valTok.line})")
                }
                when (key) {
                    "author" -> author = value
                    "description" -> description = value
                    "family" -> family = value
                    "severity" -> severity = value.toIntOrNull()
                        ?.coerceIn(0, 100)
                        ?: error("severity must be int 0..100, got '$value'")
                    "reference" -> reference = value
                    else -> extras[key] = value
                }
            }
            return YaraMeta(author, description, family, severity, reference, extras)
        }

        private fun parseStrings(): List<YaraStringPattern> {
            val out = mutableListOf<YaraStringPattern>()
            while (!isSectionStart() && peek().type != TokenType.RBRACE && !eof()) {
                val ident = expect(TokenType.IDENT).text
                require(ident.startsWith("$")) {
                    "Strings entries must start with '$' (got '$ident')"
                }
                expect(TokenType.EQ)
                val tok = next()
                val pattern = when (tok.type) {
                    TokenType.STRING -> compileLiteral(ident, tok.text)
                    TokenType.HEX_BLOCK -> {
                        val hex = HexCompiler.compile(tok.text)
                        YaraStringPattern(ident, listOf(LiteralOrHex.Hex(hex)))
                    }
                    else -> error("Expected string literal or hex block after '$ident =' (line ${tok.line})")
                }
                out += pattern
            }
            require(out.map { it.identifier }.toSet().size == out.size) {
                "Duplicate string identifier in rule (one of: ${out.map { it.identifier }})"
            }
            return out
        }

        /** Compile `"foo" ascii nocase` into one or more matchable byte forms. */
        private fun compileLiteral(ident: String, raw: String): YaraStringPattern {
            val modifiers = mutableSetOf<String>()
            while (peek().type == TokenType.IDENT && !isSectionStart() && !isStringIdent()) {
                val mod = peek().text
                if (mod in MODIFIERS) {
                    modifiers += mod
                    idx++
                } else break
            }
            val ascii = !modifiers.contains("wide") || modifiers.contains("ascii")
            val wide = modifiers.contains("wide")
            val nocase = modifiers.contains("nocase")
            require(raw.isNotEmpty()) { "Empty literal string for $ident" }

            val forms = mutableListOf<LiteralOrHex>()
            // Generate the encodings the matcher will hunt for. For ASCII we add the
            // raw bytes; for `nocase` we additionally generate the lowercase variant
            // and the matcher itself does case-insensitive compares for short ASCII.
            if (ascii) {
                if (nocase) {
                    forms += LiteralOrHex.Literal(raw.lowercase().toByteArray(Charsets.ISO_8859_1))
                } else {
                    forms += LiteralOrHex.Literal(raw.toByteArray(Charsets.ISO_8859_1))
                }
            }
            if (wide) {
                if (nocase) {
                    forms += LiteralOrHex.Literal(raw.lowercase().toByteArray(Charsets.UTF_16LE))
                } else {
                    forms += LiteralOrHex.Literal(raw.toByteArray(Charsets.UTF_16LE))
                }
            }
            require(forms.isNotEmpty()) { "Literal $ident has no encoding (ascii / wide both off?)" }
            // Carry the nocase bit into the identifier suffix the matcher uses for
            // case-insensitive comparisons so we don't have to re-parse modifiers later.
            val taggedId = if (nocase) "$ident\u0001NOCASE" else ident
            return YaraStringPattern(taggedId, forms)
        }

        private fun parseCondition(): YaraCondition {
            // Section parsing scans until next section or }; conditions are a single expr
            // that may contain `and`/`or`/`not`/parens/`N of them`/`$id`.
            return parseOr()
        }

        // condition : or_expr
        // or_expr   : and_expr ('or' and_expr)*
        // and_expr  : unary ('and' unary)*
        // unary     : 'not' unary | atom
        // atom      : '(' or_expr ')' | quantifier | $ref
        // quantifier: ('any' | 'all' | NUMBER) 'of' 'them'
        private fun parseOr(): YaraCondition {
            var left = parseAnd()
            while (peekIdent("or")) { next(); left = YaraCondition.Or(left, parseAnd()) }
            return left
        }

        private fun parseAnd(): YaraCondition {
            var left = parseUnary()
            while (peekIdent("and")) { next(); left = YaraCondition.And(left, parseUnary()) }
            return left
        }

        private fun parseUnary(): YaraCondition {
            if (peekIdent("not")) { next(); return YaraCondition.Not(parseUnary()) }
            return parseAtom()
        }

        private fun parseAtom(): YaraCondition {
            val t = peek()
            return when {
                t.type == TokenType.LPAREN -> {
                    next()
                    val inner = parseOr()
                    expect(TokenType.RPAREN)
                    inner
                }
                t.type == TokenType.IDENT && t.text.startsWith("$") -> {
                    next()
                    YaraCondition.StringRef(t.text)
                }
                t.type == TokenType.IDENT && (t.text == "any" || t.text == "all") -> {
                    next()
                    expectIdent("of")
                    expectIdent("them")
                    if (t.text == "any") YaraCondition.AnyOfThem else YaraCondition.AllOfThem
                }
                t.type == TokenType.NUMBER -> {
                    next()
                    expectIdent("of")
                    expectIdent("them")
                    YaraCondition.NOfThem(t.text.toInt())
                }
                else -> error("Unexpected token '${t.text}' in condition (line ${t.line})")
            }
        }

        // ── token helpers ──────────────────────────────────────────────────────────────

        private fun peek(): Token = tokens[idx]
        private fun next(): Token = tokens[idx++]
        private fun expect(t: TokenType): Token {
            val tok = next()
            require(tok.type == t) { "Expected $t but got ${tok.type} '${tok.text}' (line ${tok.line})" }
            return tok
        }

        private fun expectIdent(name: String) {
            val t = next()
            require(t.type == TokenType.IDENT && t.text == name) {
                "Expected '$name' but got '${t.text}' (line ${t.line})"
            }
        }

        private fun peekIdent(name: String): Boolean =
            peek().type == TokenType.IDENT && peek().text == name

        /** True if the current token is a section-start identifier followed by ':'. */
        private fun isSectionStart(): Boolean {
            val t = peek()
            if (t.type != TokenType.IDENT) return false
            if (t.text !in SECTIONS) return false
            return idx + 1 < tokens.size && tokens[idx + 1].type == TokenType.COLON
        }

        /** True if the current token is a `$id` (used so we don't gobble it as a modifier). */
        private fun isStringIdent(): Boolean =
            peek().type == TokenType.IDENT && peek().text.startsWith("$")
    }

    private val SECTIONS = setOf("meta", "strings", "condition")
    private val MODIFIERS = setOf("ascii", "wide", "nocase")

    // ── hex compiler ───────────────────────────────────────────────────────────────────

    private object HexCompiler {
        /**
         * Parse hex content like `4D 5A ?? ?? 50 45` (whitespace insensitive, allows `??`
         * wildcards) into a [HexBytePattern]. Single-nibble wildcards (`?A`) are rejected
         * — the subset deliberately keeps things to whole-byte resolution to avoid
         * accidental false positives from over-loose patterns.
         */
        fun compile(raw: String): HexBytePattern {
            val cleaned = stripComments(raw).filter { !it.isWhitespace() }
            require(cleaned.length % 2 == 0) {
                "Hex body must have even nibble count, got '$cleaned' (${cleaned.length} chars)"
            }
            require(cleaned.isNotEmpty()) { "Empty hex body" }
            val bytes = ByteArray(cleaned.length / 2)
            val mask = BooleanArray(cleaned.length / 2)
            for (i in bytes.indices) {
                val hi = cleaned[i * 2]
                val lo = cleaned[i * 2 + 1]
                if (hi == '?' && lo == '?') {
                    bytes[i] = 0
                    mask[i] = false
                } else {
                    require(hi != '?' && lo != '?') {
                        "Half-byte wildcards (?A / A?) are not supported in SafeGuard's YARA subset"
                    }
                    bytes[i] = ((hi.digitToInt(16) shl 4) or lo.digitToInt(16)).toByte()
                    mask[i] = true
                }
            }
            // Pure-wildcard patterns would match anything → reject as a config error.
            require(mask.any { it }) {
                "Hex pattern is all-wildcard (would match every offset). Add at least one fixed byte."
            }
            return HexBytePattern(bytes, mask)
        }

        private fun stripComments(raw: String): String {
            // Allow `// trailing` comments inside hex bodies for readability.
            val sb = StringBuilder()
            var i = 0
            while (i < raw.length) {
                val c = raw[i]
                if (c == '/' && i + 1 < raw.length && raw[i + 1] == '/') {
                    while (i < raw.length && raw[i] != '\n') i++
                } else {
                    sb.append(c); i++
                }
            }
            return sb.toString()
        }
    }
}
