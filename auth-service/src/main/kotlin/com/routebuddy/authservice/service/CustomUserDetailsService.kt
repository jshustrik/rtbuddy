package com.routebuddy.authservice.service

import com.routebuddy.authservice.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): org.springframework.security.core.userdetails.UserDetails {
        val userEntity = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found with username: $username")
        return User.withUsername(userEntity.username)
            .password(userEntity.passwordHash)
            .roles(userEntity.role)
            .build()
    }
}
