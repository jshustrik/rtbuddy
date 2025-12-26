package com.routebuddy.usrsysservice.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private val secret = "MySecretKeyForUserProfileJWTsMustBeAtLeast32Chars!"
    private val expiration = 3600000L

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = JwtTokenProvider(secret, expiration)
    }

    @Test
    fun `getUsernameFromJWT should return username when token is valid`() {
        val key = Keys.hmacShaKeyFor(secret.toByteArray())
        val token = Jwts.builder()
            .claim("username", "testUser")
            .signWith(key)
            .compact()
        val username = jwtTokenProvider.getUsernameFromJWT(token)
        assertEquals("testUser", username)
    }

    @Test
    fun `getUsernameFromJWT should throw exception if token is invalid`() {
        val invalidToken = "invalid.token.data"
        assertThrows(Exception::class.java) {
            jwtTokenProvider.getUsernameFromJWT(invalidToken)
        }
    }
}
