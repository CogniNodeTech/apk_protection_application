package com.safeguard.core.domain.model

/**
 * Final verdict from a protection layer or the zero-trust decision engine.
 */
enum class Verdict {
    SAFE,
    SUSPICIOUS,
    MALICIOUS,
    UNKNOWN
}
