package com.routebuddy.authservice.service

import com.routebuddy.authservice.dto.RegistrationRequest
import com.routebuddy.authservice.model.User
import com.routebuddy.authservice.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var emailDomainValidator: EmailDomainValidator
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        passwordEncoder = mock()
        emailDomainValidator = mock()
        authService = AuthService(userRepository, passwordEncoder, emailDomainValidator)
    }

    @Test
    fun `register should throw RuntimeException if username already exists`() {
        val request = RegistrationRequest("existingUser", "pass", "USER", "ex@example.com")
        whenever(userRepository.existsByUsername("existingUser")).thenReturn(true)

        val ex = assertThrows(RuntimeException::class.java) {
            authService.register(request)
        }
        assertEquals("Имя пользователя уже занято", ex.message)
    }

    @Test
    fun `register should encode password and save user if username does not exist`() {
        val request = RegistrationRequest("newUser", "plainPass", "USER", "new@example.com")
        whenever(userRepository.existsByUsername("newUser")).thenReturn(false)
        whenever(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false)
        whenever(emailDomainValidator.hasResolvableDomain("new@example.com")).thenReturn(true)
        whenever(passwordEncoder.encode("plainPass")).thenReturn("encodedPass")
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

    @Test
    fun `register should throw RuntimeException if email already exists`() {
        val request = RegistrationRequest("newUser", "plainPass", "USER", "used@example.com")
        whenever(userRepository.existsByUsername("newUser")).thenReturn(false)
        whenever(userRepository.existsByEmailIgnoreCase("used@example.com")).thenReturn(true)

        val ex = assertThrows(RuntimeException::class.java) {
            authService.register(request)
        }

        assertEquals("Email уже используется", ex.message)
        verify(userRepository, never()).save(any<User>())
    }

    @Test
    fun `register should throw RuntimeException if email domain has no DNS records`() {
        val request = RegistrationRequest("newUser", "plainPass", "USER", "bad@example.invalid")
        whenever(userRepository.existsByUsername("newUser")).thenReturn(false)
        whenever(userRepository.existsByEmailIgnoreCase("bad@example.invalid")).thenReturn(false)
        whenever(emailDomainValidator.hasResolvableDomain("bad@example.invalid")).thenReturn(false)

        val ex = assertThrows(RuntimeException::class.java) {
            authService.register(request)
        }

        assertEquals("Домен email не найден в DNS", ex.message)
        verify(userRepository, never()).save(any<User>())
    }
}
