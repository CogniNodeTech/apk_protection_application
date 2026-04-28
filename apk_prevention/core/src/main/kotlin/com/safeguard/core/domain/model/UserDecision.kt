package com.safeguard.core.domain.model

/**
 * User's decision when overriding a warning.
 */
enum class UserDecision {
    ALLOW_ANYWAY,
    BLOCK,
    QUARANTINE,
    REPORT_FALSE_POSITIVE,
    PENDING
}
