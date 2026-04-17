package com.routebuddy.reviews.controller

import com.routebuddy.reviews.dto.ReviewRequest
import com.routebuddy.reviews.dto.ReviewResponse
import com.routebuddy.reviews.dto.ReviewUpdateRequest
import com.routebuddy.reviews.dto.RouteRatingResponse
import com.routebuddy.reviews.service.ReviewService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reviews")
class ApiReviewController(private val reviewService: ReviewService) {

    @PostMapping
    fun createReview(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestHeader("X-Username", defaultValue = "anonymous") username: String,
        @RequestBody @Valid request: ReviewRequest
    ): ReviewResponse {
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
        @RequestHeader("X-User-Id") userId: Long,
        @RequestBody @Valid request: ReviewUpdateRequest
    ): ReviewResponse {
        return reviewService.updateReview(reviewId, userId, request)
    }

    @DeleteMapping("/{reviewId}")
    fun deleteReview(
        @PathVariable reviewId: String,
        @RequestHeader("X-User-Id") userId: Long
    ) {
        reviewService.deleteReview(reviewId, userId)
    }
}
