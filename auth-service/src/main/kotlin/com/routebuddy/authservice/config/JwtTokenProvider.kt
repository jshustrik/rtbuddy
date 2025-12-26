package com.routebuddy.authservice.config

import com.routebuddy.authservice.model.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val jwtSecret: String,
    @Value("\${jwt.expiration}")
    private val jwtExpirationInMs: Long
) {

    fun generateToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)
        val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        return Jwts.builder()
            .setSubject(user.id.toString())
            .claim("username", user.username)
            .claim("role", user.role)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getJwtExpirationInMs(): Long = jwtExpirationInMs
}
