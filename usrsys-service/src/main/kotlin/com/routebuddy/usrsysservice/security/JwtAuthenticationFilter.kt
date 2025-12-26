package com.routebuddy.usrsysservice.security

import com.routebuddy.usrsysservice.config.JwtTokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)
        if (!token.isNullOrEmpty()) {
            try {
                val username = jwtTokenProvider.getUsernameFromJWT(token)
                if (username != null && SecurityContextHolder.getContext().authentication == null) {
                    val auth = UsernamePasswordAuthenticationToken(
                        username, null, listOf(SimpleGrantedAuthority("ROLE_USER"))
                    )
                    SecurityContextHolder.getContext().authentication = auth
                    logger.debug("Authenticated user: $username")
                }
            } catch (ex: Exception) {
                logger.error("Error validating token", ex)
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (!bearerToken.isNullOrEmpty() && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        request.cookies?.forEach {
            if (it.name == "JWT") return it.value
        }
        return null
    }
}
