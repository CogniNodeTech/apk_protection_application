package com.apkprevention.auth.oauth

import com.apkprevention.auth.data.model.UserDto
import org.json.JSONObject
import android.util.Base64

object JwtPayloadParser {
    fun parseUser(token: String): UserDto? {
        val parts = token.split(".")
        if (parts.size != 3) return null
        return try {
            val payloadJson = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            )
            val o = JSONObject(payloadJson)
            val id = o.opt("id")?.toString() ?: return null
            val email = o.optString("email", "")
            val nameFromEmail = email.substringBefore("@").ifBlank { "User" }
            UserDto(id = id, fullName = nameFromEmail, email = email, phone = null)
        } catch (_: Exception) {
            null
        }
    }
}
