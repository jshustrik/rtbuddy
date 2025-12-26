package com.routebuddy.authservice.service

import com.quizwhiz.authservice.model.User
import com.quizwhiz.authservice.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomUserDetailsServiceTest {

    private val userRepository = mock<UserRepository>()
    private val userDetailsService = CustomUserDetailsService(userRepository)

    @Test
    fun `loadUserByUsername should return UserDetails if user found`() {
        val user = User(1, "testuser", "passHash", "ADMIN", "test@example.com")
        whenever(userRepository.findByUsername("testuser")).thenReturn(user)

        val userDetails = userDetailsService.loadUserByUsername("testuser")
        assertEquals("testuser", userDetails.username)
        // Roles автоматически преобразуются в [ROLE_ADMIN] по умолчанию
        assertTrue(userDetails.authorities.any { it.authority == "ROLE_ADMIN" })
    }

    @Test
    fun `loadUserByUsername should throw if user not found`() {
        whenever(userRepository.findByUsername("unknown")).thenReturn(null)
        assertThrows(UsernameNotFoundException::class.java) {
            userDetailsService.loadUserByUsername("unknown")
        }
    }
}
