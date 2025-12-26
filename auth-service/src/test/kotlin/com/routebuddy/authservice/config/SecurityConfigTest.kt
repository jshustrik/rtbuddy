package com.routebuddy.authservice.config

import com.quizwhiz.authservice.service.CustomUserDetailsService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain

class SecurityConfigTest {
    private val customUserDetailsService = mock<CustomUserDetailsService>()
    private val customAuthenticationSuccessHandler = mock<CustomAuthenticationSuccessHandler>()
    private val securityConfig = SecurityConfig(customUserDetailsService, customAuthenticationSuccessHandler)

    @Test
    fun `passwordEncoder should return BCryptPasswordEncoder`() {
        val encoder = securityConfig.passwordEncoder()
        assertTrue(encoder is BCryptPasswordEncoder)
    }

    @Test
    fun `authenticationManager should be retrieved from config`() {
        val authConfig = mock<AuthenticationConfiguration>()
        val mockManager = mock<AuthenticationManager>()

        whenever(authConfig.authenticationManager).thenReturn(mockManager)

        val manager = securityConfig.authenticationManager(authConfig)
        assertNotNull(manager)
        assertEquals(mockManager, manager)
    }


    @Test
    fun `securityFilterChain should build without exceptions`() {
        val http = mock<HttpSecurity>()
        whenever(http.csrf(any())).thenReturn(http)
        whenever(http.authorizeHttpRequests(any())).thenReturn(http)
        whenever(http.formLogin(any())).thenReturn(http)
        whenever(http.logout(any())).thenReturn(http)
        whenever(http.userDetailsService(any())).thenReturn(http)

        val mockFilterChain = mock<DefaultSecurityFilterChain>()
        whenever(http.build()).thenReturn(mockFilterChain)

        val chain = securityConfig.securityFilterChain(http)
        assertNotNull(chain)
        assertEquals(mockFilterChain, chain)
    }
}
