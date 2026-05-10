package com.routebuddy.authservice.config

import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class RedirectUrlValidator {
    fun sanitize(rawUrl: String?): String? {
        val url = rawUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (url.length > 200) return null
        if (!url.startsWith("/") || url.startsWith("//")) return null
        if (url.contains('\\') || url.contains('\r') || url.contains('\n')) return null
        return url
    }

    fun withNickname(url: String, username: String): String {
        val separator = if (url.contains("?")) "&" else "?"
        return "$url${separator}nickname=${encode(username)}"
    }

    fun profileUrl(username: String): String = "/profile/${encode(username)}"

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
}
