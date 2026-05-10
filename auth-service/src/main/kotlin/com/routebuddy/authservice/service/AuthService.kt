package com.routebuddy.authservice.service

import com.routebuddy.authservice.dto.RegistrationRequest
import com.routebuddy.authservice.model.User
import com.routebuddy.authservice.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailDomainValidator: EmailDomainValidator
) {

    @Transactional
    fun register(request: RegistrationRequest) {
        val username = request.username.trim()
        if (userRepository.existsByUsername(username)) {
            throw RuntimeException("Имя пользователя уже занято")
        }
        val email = request.email?.trim()?.lowercase()
            ?: throw RuntimeException("Введите email")
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw RuntimeException("Email уже используется")
        }
        if (!emailDomainValidator.hasResolvableDomain(email)) {
            throw RuntimeException("Домен email не найден в DNS")
        }
        val user = User(
            username = username,
            passwordHash = passwordEncoder.encode(request.password),
            role = request.role,
            email = email
        )
        userRepository.save(user)
    }
}
