package com.routebuddy.reviews.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ReviewRequest(
    @field:Min(value = 1, message = "Маршрут для отзыва не найден")
    val routeId: Long,
    @field:NotBlank(message = "Введите текст отзыва")
    @field:Size(min = 5, max = 500, message = "Текст отзыва должен содержать от 5 до 500 символов")
    val text: String,
    @field:Min(value = 1, message = "Оценка должна быть от 1 до 10")
    @field:Max(value = 10, message = "Оценка должна быть от 1 до 10")
    val rating: Int
)
