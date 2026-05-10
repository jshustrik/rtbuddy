package com.routebuddy.authservice.config

import com.routebuddy.authservice.model.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private val secret = "test-secret-test-secret-test-secret-test-secret-test-secret-test-secret-test-secret"
    private val expiration = 3600000L // 1 час

    @BeforeEach
    fun setUp() {
        // Имитируем, как будто мы получили настройки через @Value
        jwtTokenProvider = JwtTokenProvider(secret, expiration)
    }

    @Test
    fun `generateToken should return a valid JWT`() {
        val user = User(id = 100, username = "testuser", passwordHash = "hash", role = "USER", email = "test@example.com")
        val token = jwtTokenProvider.generateToken(user)
        assertNotNull(token)

        val key = Keys.hmacShaKeyFor(secret.toByteArray())
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        assertEquals("100", claims.subject)
        assertEquals("testuser", claims["username"])
        assertEquals("USER", claims["role"])
        assertTrue(claims.issuedAt.before(Date()))
        assertTrue(claims.expiration.after(Date()))
    }

    @Test
    fun `getJwtExpirationInMs should return correct expiration`() {
        assertEquals(expiration, jwtTokenProvider.getJwtExpirationInMs())
    }
}
