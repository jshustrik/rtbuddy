package com.routebuddy.reviews.service

import com.routebuddy.reviews.dto.ReviewRequest
import com.routebuddy.reviews.dto.ReviewResponse
import com.routebuddy.reviews.dto.ReviewUpdateRequest
import com.routebuddy.reviews.dto.RouteRatingResponse
import com.routebuddy.reviews.entity.Review
import com.routebuddy.reviews.exception.DuplicateReviewException
import com.routebuddy.reviews.exception.ReviewNotFoundException
import com.routebuddy.reviews.repository.ReviewRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReviewService(private val reviewRepository: ReviewRepository) {

    private val blockedTokens = setOf("http://", "https://", "www.", "блин", "черт")

    @Transactional
    fun createReview(userId: Long, username: String, request: ReviewRequest): ReviewResponse {
        validateReviewText(request.text)
        if (reviewRepository.existsByRouteIdAndUserId(request.routeId, userId)) {
            throw DuplicateReviewException("Пользователь уже оставил отзыв на этот маршрут")
        }

        val now = LocalDateTime.now()
        val saved = reviewRepository.save(
            Review(
                routeId = request.routeId,
                userId = userId,
                authorUsername = username,
                text = request.text.trim(),
                rating = request.rating,
                createdAt = now,
                updatedAt = now
            )
        )
        return ReviewResponse.fromEntity(saved)
    }

    fun getReviewsByRouteId(routeId: Long, pageable: Pageable): Page<ReviewResponse> {
        return reviewRepository.findByRouteId(routeId, pageable).map(ReviewResponse::fromEntity)
    }

    fun getRouteRating(routeId: Long): RouteRatingResponse {
        val avg = reviewRepository.findAverageRatingByRouteId(routeId) ?: 0.0
        val rounded = kotlin.math.round(avg * 10) / 10.0
        val count = reviewRepository.countByRouteId(routeId)
        return RouteRatingResponse(routeId = routeId, averageRating = rounded, reviewsCount = count)
    }

    @Transactional
    fun updateReview(reviewId: String, userId: Long, request: ReviewUpdateRequest): ReviewResponse {
        validateReviewText(request.text)
        val review = reviewRepository.findById(reviewId).orElseThrow {
            ReviewNotFoundException("Отзыв с id=$reviewId не найден")
        }
        if (review.userId != userId) {
            throw IllegalStateException("Редактировать можно только свой отзыв")
        }

        review.text = request.text.trim()
        review.rating = request.rating
        review.updatedAt = LocalDateTime.now()
        return ReviewResponse.fromEntity(reviewRepository.save(review))
    }

    @Transactional
    fun deleteReview(reviewId: String, userId: Long) {
        val review = reviewRepository.findById(reviewId).orElseThrow {
            ReviewNotFoundException("Отзыв с id=$reviewId не найден")
        }
        if (review.userId != userId) {
            throw IllegalStateException("Удалять можно только свой отзыв")
        }
        reviewRepository.delete(review)
    }

    private fun validateReviewText(text: String) {
        val normalized = text.trim().lowercase()
        require(normalized.length in 5..500) { "Текст отзыва должен содержать от 5 до 500 символов" }
        if (blockedTokens.any { normalized.contains(it) }) {
            throw IllegalArgumentException("Текст отзыва содержит запрещенные слова или ссылки")
        }
    }
}
