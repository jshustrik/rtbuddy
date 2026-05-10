package com.routebuddy.reviews.controller

import com.routebuddy.reviews.dto.ReviewRequest
import com.routebuddy.reviews.dto.ReviewResponse
import com.routebuddy.reviews.dto.ReviewUpdateRequest
import com.routebuddy.reviews.dto.RouteRatingResponse
import com.routebuddy.reviews.service.ReviewService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reviews")
class ApiReviewController(
    private val reviewService: ReviewService,
    @Value("\${service.internal-token}") private val internalToken: String
) {
    private fun requireInternalToken(headerToken: String?) {
        if (internalToken.isBlank() || headerToken != internalToken) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав")
        }
    }

    @PostMapping
    fun createReview(
        @RequestHeader("X-Internal-Token", required = false) headerToken: String?,
        @RequestHeader("X-User-Id") userId: Long,
        @RequestHeader("X-Username", defaultValue = "anonymous") username: String,
        @RequestBody @Valid request: ReviewRequest
    ): ReviewResponse {
        requireInternalToken(headerToken)
        return reviewService.createReview(userId, username, request)
    }

    @GetMapping("/routes/{routeId}")
    fun getReviewsByRoute(
        @PathVariable routeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<ReviewResponse> {
        return reviewService.getReviewsByRouteId(routeId, PageRequest.of(page, size))
    }

    @GetMapping("/routes/{routeId}/rating")
    fun getRouteRating(@PathVariable routeId: Long): RouteRatingResponse {
        return reviewService.getRouteRating(routeId)
    }

    @PutMapping("/{reviewId}")
    fun updateReview(
        @PathVariable reviewId: String,
        @RequestHeader("X-Internal-Token", required = false) headerToken: String?,
        @RequestHeader("X-User-Id") userId: Long,
        @RequestBody @Valid request: ReviewUpdateRequest
    ): ReviewResponse {
        requireInternalToken(headerToken)
        return reviewService.updateReview(reviewId, userId, request)
    }

    @DeleteMapping("/{reviewId}")
    fun deleteReview(
        @PathVariable reviewId: String,
        @RequestHeader("X-Internal-Token", required = false) headerToken: String?,
        @RequestHeader("X-User-Id") userId: Long
    ) {
        requireInternalToken(headerToken)
        reviewService.deleteReview(reviewId, userId)
    }
}
