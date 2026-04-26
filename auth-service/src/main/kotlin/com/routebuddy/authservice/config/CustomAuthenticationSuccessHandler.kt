package com.routebuddy.authservice.config

import com.routebuddy.authservice.repository.UserRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.net.URI

@Component
class CustomAuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val username = authentication.name
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found with username: $username")
        val token = jwtTokenProvider.generateToken(user)
        val cookie = Cookie("JWT", token).apply {
            setHttpOnly(true)
            path = "/"
            maxAge = (jwtTokenProvider.getJwtExpirationInMs() / 1000).toInt()
        }
        response.addCookie(cookie)
        val scheme = request.scheme ?: "http"
        val host = request.serverName ?: "127.0.0.1"

        val rawRedirectUrl = request.getParameter("redirectUrl")
        val redirectTarget = rawRedirectUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { candidate ->
                // Keep redirects on the same host so the JWT cookie is sent (localhost vs 127.0.0.1).
                runCatching { URI(candidate) }.getOrNull()?.let { uri ->
                    when {
                        uri.isAbsolute -> URI(
                            uri.scheme ?: scheme,
                            uri.userInfo,
                            host,
                            uri.port,
                            uri.path,
                            uri.query,
                            uri.fragment
                        ).toString()

                        candidate.startsWith("/") -> "$scheme://$host:${request.serverPort}$candidate"

                        else -> candidate
                    }
                } ?: candidate
            }
            ?: "$scheme://$host:8082/profile/$username"

        response.sendRedirect(redirectTarget)
    }
}
