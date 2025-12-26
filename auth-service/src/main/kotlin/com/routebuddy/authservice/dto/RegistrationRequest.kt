package com.routebuddy.authservice.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class RegistrationRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,
    @field:NotBlank(message = "Password is required")
    val password: String,
    val role: String = "USER",
    @field:Email(message = "Email should be valid")
    val email: String?
)
