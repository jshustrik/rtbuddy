package com.routebuddy.authservice.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegistrationRequest(
    @field:NotBlank(message = "Введите имя пользователя")
    @field:Size(min = 4, max = 20, message = "Имя пользователя должно содержать от 4 до 20 символов")
    @field:Pattern(
        regexp = "^[A-Za-zА-Яа-яЁё0-9_-]{4,20}$",
        message = "Имя пользователя может содержать только буквы, цифры, подчёркивание и дефис"
    )
    val username: String,
    @field:NotBlank(message = "Введите пароль")
    @field:Pattern(
        regexp = "^(?=.*[A-ZА-ЯЁ])(?=.*\\d)[A-Za-zА-Яа-яЁё\\d]{6,}$",
        message = "Пароль должен содержать минимум 6 символов, одну заглавную букву, одну цифру и не должен содержать специальные символы"
    )
    val password: String,
    val role: String = "USER",
    @field:NotBlank(message = "Введите email")
    @field:Email(message = "Введите корректный email")
    val email: String?
)
