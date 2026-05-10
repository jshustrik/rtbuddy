package com.routebuddy.authservice.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class JwtCookieFactory(
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun addCookie(request: HttpServletRequest, response: HttpServletResponse, token: String) {
        val secure = request.isSecure ||
            request.getHeader("X-Forwarded-Proto").equals("https", ignoreCase = true)

        val cookie = ResponseCookie.from("JWT", token)
            .httpOnly(true)
            .secure(secure)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofMillis(jwtTokenProvider.getJwtExpirationInMs()))
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }
}
