package com.routebuddy.usrsysservice.config

import com.routebuddy.usrsysservice.config.JwtTokenProvider
import com.routebuddy.usrsysservice.security.JwtAuthenticationFilter
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Configuration
@EnableWebSecurity
class ProfileSecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.GET, "/profile").permitAll()
                    .requestMatchers("/profile/**").authenticated()
                    .anyRequest().permitAll()
            }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint { request, response, _ ->
                    response.sendRedirect(loginUrl(request))
                }
                exceptions.accessDeniedHandler { request, response, _ ->
                    response.sendRedirect(loginUrl(request))
                }
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )
        return http.build()
    }

    private fun loginUrl(request: HttpServletRequest): String {
        val query = request.queryString?.let { "?$it" } ?: ""
        val target = "${request.requestURI}$query"
        val encodedTarget = URLEncoder.encode(target, StandardCharsets.UTF_8)
        return "/login?redirectUrl=$encodedTarget"
    }
}
