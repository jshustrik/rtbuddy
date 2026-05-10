package com.routebuddy.authservice.config

import com.routebuddy.authservice.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    private val jwtCookieFactory: JwtCookieFactory,
    private val redirectUrlValidator: RedirectUrlValidator
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
        jwtCookieFactory.addCookie(request, response, token)
        val redirectUrl = redirectUrlValidator.sanitize(request.getParameter("redirectUrl"))
        response.sendRedirect(redirectUrl ?: redirectUrlValidator.profileUrl(username))
    }
}
