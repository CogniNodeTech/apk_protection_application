package com.safeguard.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun Context.openExternalUrl(url: String) {
    val trimmed = url.trim()
    if (trimmed.isEmpty() || !trimmed.startsWith("http")) {
        Toast.makeText(this, "Legal link is not configured. Update app URLs in build settings.", Toast.LENGTH_LONG).show()
        return
    }
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)))
    } catch (_: Exception) {
        Toast.makeText(this, "Could not open link.", Toast.LENGTH_SHORT).show()
    }
}
