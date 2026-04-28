package com.safeguard.core.domain.crash

/**
 * Abstraction for reporting crashes. Implement with file logging, Crashlytics, or no-op.
 * Do not include PII in reported data.
 */
interface CrashReporter {
    fun logException(thread: Thread, throwable: Throwable)
}
