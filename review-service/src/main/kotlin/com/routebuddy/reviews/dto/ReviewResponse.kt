package com.routebuddy.reviews.dto

import com.routebuddy.reviews.entity.Review
import java.time.LocalDateTime

data class ReviewResponse(
    val id: String,
    val routeId: Long,
    val authorId: Long,
    val authorUsername: String,
    val comment: String,
    val rating: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromEntity(review: Review): ReviewResponse {
            return ReviewResponse(
                id = review.id.orEmpty(),
                routeId = review.routeId,
                authorId = review.userId,
                authorUsername = review.authorUsername,
                comment = review.text,
                rating = review.rating,
                createdAt = requireNotNull(review.createdAt),
                updatedAt = requireNotNull(review.updatedAt)
            )
        }
    }
}
