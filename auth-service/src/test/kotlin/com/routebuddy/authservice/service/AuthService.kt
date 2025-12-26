package com.routebuddy.authservice.service

import com.quizwhiz.authservice.dto.RegistrationRequest
import com.quizwhiz.authservice.model.User
import com.quizwhiz.authservice.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        passwordEncoder = mock()
        authService = AuthService(userRepository, passwordEncoder)
    }

    @Test
    fun `register should throw RuntimeException if username already exists`() {
        val request = RegistrationRequest("existingUser", "pass", "USER", "ex@example.com")
        whenever(userRepository.existsByUsername("existingUser")).thenReturn(true)

        val ex = assertThrows(RuntimeException::class.java) {
            authService.register(request)
        }
        assertEquals("Username already exists", ex.message)
    }

    @Test
    fun `register should encode password and save user if username does not exist`() {
        val request = RegistrationRequest("newUser", "plainPass", "USER", "new@example.com")
        whenever(userRepository.existsByUsername("newUser")).thenReturn(false)
        whenever(passwordEncoder.encode("plainPass")).thenReturn("encodedPass")
        // При сохранении пользователя можно вернуть тот же user, либо создать новый
        val savedUser = User(1L, "newUser", "encodedPass", "USER", "new@example.com")
        whenever(userRepository.save(any<User>())).thenReturn(savedUser)

        authService.register(request)

        verify(passwordEncoder).encode("plainPass")
        verify(userRepository).save(argThat { user ->
            user.username == "newUser" &&
                    user.passwordHash == "encodedPass" &&
                    user.email == "new@example.com"
        })
    }
}
