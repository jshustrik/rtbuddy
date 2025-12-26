package com.routebuddy.authservice.config

import com.routebuddy.authservice.service.CustomUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customUserDetailsService: CustomUserDetailsService,
    private val customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/internal/users/**").permitAll()
                    .requestMatchers("/login", "/register", "/register/**").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successHandler(customAuthenticationSuccessHandler)
                    .permitAll()
            }
            .logout { logout ->
                logout.permitAll()
            }
            .userDetailsService(customUserDetailsService)
        return http.build()
    }
}
