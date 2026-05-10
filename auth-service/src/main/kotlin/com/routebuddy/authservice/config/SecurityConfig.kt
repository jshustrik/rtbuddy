package com.routebuddy.authservice.config

import com.routebuddy.authservice.service.CustomUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import java.time.Duration

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
                logout.logoutUrl("/logout")
                    .logoutSuccessHandler { request, response, _ ->
                        val secure = request.isSecure ||
                            request.getHeader("X-Forwarded-Proto").equals("https", ignoreCase = true)
                        val expiredCookie = ResponseCookie.from("JWT", "")
                            .httpOnly(true)
                            .secure(secure)
                            .sameSite("Lax")
                            .path("/")
                            .maxAge(Duration.ZERO)
                            .build()

                        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                        response.sendRedirect("/login?logout")
                    }
                    .permitAll()
            }
            .userDetailsService(customUserDetailsService)
        return http.build()
    }
}
