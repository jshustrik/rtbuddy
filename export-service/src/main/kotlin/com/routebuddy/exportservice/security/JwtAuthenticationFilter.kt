package com.routebuddy.exportservice.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = extractToken(request)
        if (!token.isNullOrBlank()) {
            try {
                val userId = jwtTokenProvider.getUserIdFromJWT(token)
                val username = jwtTokenProvider.getUsernameFromJWT(token) ?: "user"
                val principal = PrincipalInfo(userId, username)
                val auth = UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_USER")),
                )
                auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = auth
            } catch (_: Exception) {
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization")
        if (!bearer.isNullOrBlank() && bearer.startsWith("Bearer ")) {
            return bearer.substring(7)
        }
        request.cookies?.forEach {
            if (it.name == "JWT") return it.value
        }
        return null
    }
}
