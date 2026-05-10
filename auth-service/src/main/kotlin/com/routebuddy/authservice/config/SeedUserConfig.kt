package com.routebuddy.authservice.config

import com.routebuddy.authservice.model.User
import com.routebuddy.authservice.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class SeedUserConfig {

    @Bean
    fun seedUsers(userRepository: UserRepository, passwordEncoder: PasswordEncoder): CommandLineRunner =
        CommandLineRunner {
            if (!userRepository.existsByUsername("traveler")) {
                userRepository.save(
                    User(
                        username = "traveler",
                        passwordHash = passwordEncoder.encode("traveler123"),
                        role = "USER",
                        email = "traveler@routebuddy.local",
                        avatarUrl = "https://images.unsplash.com/photo-1502685104226-ee32379fefbe?auto=format&fit=crop&w=240&q=80"
                    )
                )
            }
            if (!userRepository.existsByUsername("guide_kazan")) {
                userRepository.save(
                    User(
                        username = "guide_kazan",
                        passwordHash = passwordEncoder.encode("guide123"),
                        role = "USER",
                        email = "guide@routebuddy.local",
                        avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=240&q=80"
                    )
                )
            }
        }
}
