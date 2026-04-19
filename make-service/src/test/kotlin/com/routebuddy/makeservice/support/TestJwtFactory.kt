package com.routebuddy.makeservice.support

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.Date

object TestJwtFactory {
    private const val SECRET = "n4uB0oP1mQ6sT8yZ0xR3vL7eF2aD5jK9sU8iE4wQ1zC3bN6rT8vY0pL7kJ2dS9aF"

    fun token(userId: Long = 1L, username: String = "tester"): String {
        val key = Keys.hmacShaKeyFor(SECRET.toByteArray())
        val now = Date()
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("username", username)
            .claim("role", "USER")
            .setIssuedAt(now)
            .setExpiration(Date(now.time + 3600_000L))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }
}
