package com.safeguard.crash

import android.content.Context
import android.util.Log
import com.safeguard.core.domain.crash.CrashReporter
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Writes crash reports to app filesDir. For production, replace binding with Crashlytics implementation.
 */
class FileCrashReporter @Inject constructor(
    @ApplicationContext private val context: Context
) : CrashReporter {

    override fun logException(thread: Thread, throwable: Throwable) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val file = File(context.filesDir, "crash_$date.txt")
            file.writeText(
                "Thread: ${thread.name}\n" +
                "Time: ${System.currentTimeMillis()}\n" +
                stackTraceToString(throwable)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write crash log", e)
        }
    }

    private fun stackTraceToString(throwable: Throwable): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }

    companion object {
        private const val TAG = "SafeGuardCrash"
    }
}
