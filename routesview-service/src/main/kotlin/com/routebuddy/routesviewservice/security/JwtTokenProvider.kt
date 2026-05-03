package com.routebuddy.routesviewservice.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val jwtSecret: String
) {
    private fun parseClaims(token: String) =
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body

    fun validateToken(token: String): Boolean =
        runCatching { parseClaims(token) }.isSuccess

    fun getUserIdFromJWT(token: String): Long {
        val claims = parseClaims(token)
        return claims.subject.toLong()
    }

    fun getUsernameFromJWT(token: String): String? {
        val claims = parseClaims(token)
        return claims["username"] as? String
    }
}
