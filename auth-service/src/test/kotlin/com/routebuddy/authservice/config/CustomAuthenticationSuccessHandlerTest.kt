package com.routebuddy.authservice.config

import com.routebuddy.authservice.model.User
import com.routebuddy.authservice.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomAuthenticationSuccessHandlerTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var userRepository: UserRepository
    private lateinit var successHandler: CustomAuthenticationSuccessHandler

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = mock()
        userRepository = mock()
        successHandler = CustomAuthenticationSuccessHandler(
            jwtTokenProvider,
            userRepository,
            JwtCookieFactory(jwtTokenProvider),
            RedirectUrlValidator()
        )
    }

    @Test
    fun `onAuthenticationSuccess should add JWT cookie and redirect to local url`() {
        val username = "testuser"
        val token = "dummyToken"
        val user = User(id = 1, username = username, passwordHash = "hash", role = "USER", email = "test@example.com")

        whenever(userRepository.findByUsername(username)).thenReturn(user)
        whenever(jwtTokenProvider.generateToken(user)).thenReturn(token)
        whenever(jwtTokenProvider.getJwtExpirationInMs()).thenReturn(3600000L)

        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val auth = mock<Authentication>()

        whenever(request.getParameter("redirectUrl")).thenReturn("/constructor")
        whenever(request.getHeader("X-Forwarded-Proto")).thenReturn("https")
        whenever(auth.name).thenReturn(username)

        successHandler.onAuthenticationSuccess(request, response, auth)

        verify(response).addHeader(eq("Set-Cookie"), check {
            assertTrue(it.contains("JWT=$token"))
            assertTrue(it.contains("HttpOnly"))
            assertTrue(it.contains("Path=/"))
            assertTrue(it.contains("Max-Age=3600"))
            assertTrue(it.contains("SameSite=Lax"))
            assertTrue(it.contains("Secure"))
        })
        verify(response).sendRedirect("/constructor")
    }

    @Test
    fun `onAuthenticationSuccess should ignore external redirectUrl`() {
        val username = "testuser"
        val token = "dummyToken"
        val user = User(id = 1, username = username, passwordHash = "hash", role = "USER", email = "test@example.com")

        whenever(userRepository.findByUsername(username)).thenReturn(user)
        whenever(jwtTokenProvider.generateToken(user)).thenReturn(token)
        whenever(jwtTokenProvider.getJwtExpirationInMs()).thenReturn(3600000L)

        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val auth = mock<Authentication>()

        whenever(request.getParameter("redirectUrl")).thenReturn("https://example.com/redirect")
        whenever(auth.name).thenReturn(username)

        successHandler.onAuthenticationSuccess(request, response, auth)

        verify(response).sendRedirect("/profile/$username")
    }

    @Test
    fun `onAuthenticationSuccess should redirect to default url when no redirectUrl provided`() {
        val username = "testuser"
        val token = "dummyToken"
        val user = User(id = 1, username = username, passwordHash = "hash", role = "USER", email = "test@example.com")

        whenever(userRepository.findByUsername(username)).thenReturn(user)
        whenever(jwtTokenProvider.generateToken(user)).thenReturn(token)
        whenever(jwtTokenProvider.getJwtExpirationInMs()).thenReturn(3600000L)

        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val auth = mock<Authentication>()

        whenever(request.getParameter("redirectUrl")).thenReturn(null)
        whenever(auth.name).thenReturn(username)

        successHandler.onAuthenticationSuccess(request, response, auth)

        verify(response).sendRedirect("/profile/$username")
    }

    @Test
    fun `onAuthenticationSuccess should throw UsernameNotFoundException if user doesn't exist`() {
        val username = "unknown"
        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val auth = mock<Authentication>()

        whenever(auth.name).thenReturn(username)
        whenever(userRepository.findByUsername(username)).thenReturn(null)

        assertThrows(UsernameNotFoundException::class.java) {
            successHandler.onAuthenticationSuccess(request, response, auth)
        }
    }
}
