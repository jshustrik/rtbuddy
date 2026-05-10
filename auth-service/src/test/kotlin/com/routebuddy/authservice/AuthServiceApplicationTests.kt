package com.routebuddy.authservice

import com.routebuddy.authservice.dto.RegistrationRequest
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication

class AuthServiceApplicationTests {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `application class has Spring Boot marker`() {
        assertTrue(AuthServiceApplication::class.java.isAnnotationPresent(SpringBootApplication::class.java))
    }

    @Test
    fun `registration request accepts valid credentials`() {
        val request = RegistrationRequest(
            username = "guide_2026",
            password = "Password1",
            role = "USER",
            email = "guide@example.com"
        )

        assertTrue(validator.validate(request).isEmpty())
    }

    @Test
    fun `registration request rejects cyrillic username`() {
        val request = RegistrationRequest(
            username = "юлия",
            password = "Password1",
            role = "USER",
            email = "guide@example.com"
        )

        val properties = validator.validate(request).map { it.propertyPath.toString() }

        assertTrue("username" in properties)
    }

    @Test
    fun `registration request rejects password with special symbols`() {
        val request = RegistrationRequest(
            username = "guide_2026",
            password = "Password1!",
            role = "USER",
            email = "guide@example.com"
        )

        val properties = validator.validate(request).map { it.propertyPath.toString() }

        assertTrue("password" in properties)
    }

    @Test
    fun `registration request rejects malformed email`() {
        val request = RegistrationRequest(
            username = "guide_2026",
            password = "Password1",
            role = "USER",
            email = "not-email"
        )

        val properties = validator.validate(request).map { it.propertyPath.toString() }

        assertTrue("email" in properties)
    }
}
