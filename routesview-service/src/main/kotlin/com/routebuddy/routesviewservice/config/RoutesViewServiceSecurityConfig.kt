package com.routebuddy.routesviewservice.config

import com.routebuddy.routesviewservice.security.JwtAuthenticationFilter
import com.routebuddy.routesviewservice.security.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class RoutesViewServiceSecurityConfig (
    private val jwtTokenProvider: JwtTokenProvider) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    // public pages
                    .requestMatchers(HttpMethod.GET, "/", "/routes", "/routes/**").permitAll()
                    // actions that require auth
                    .requestMatchers(HttpMethod.POST, "/routes/*/reviews").authenticated()
                    .requestMatchers("/routes/create", "/routes/createday", "/routes/compose", "/routes/export/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/routes/**").authenticated()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )
        return http.build()
    }
}