package com.routebuddy.authservice.config

import com.routebuddy.authservice.repository.UserRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

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
        val redirectUrl = request.getParameter("redirectUrl")
        if (!redirectUrl.isNullOrEmpty()) {
            response.sendRedirect(redirectUrl)
        } else {
            response.sendRedirect("http://127.0.0.1:8082/profile/$username")
        }
    }
}
