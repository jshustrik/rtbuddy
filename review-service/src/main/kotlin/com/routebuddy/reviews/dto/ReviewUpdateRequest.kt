package com.routebuddy.reviews.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ReviewUpdateRequest(
    @field:NotBlank
    @field:Size(min = 5, max = 500)
    val text: String,
    @field:Min(1)
    @field:Max(10)
    val rating: Int
)
