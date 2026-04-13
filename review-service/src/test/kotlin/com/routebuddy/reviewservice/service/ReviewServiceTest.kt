package com.routebuddy.reviews.service

import com.routebuddy.reviews.dto.ReviewRequest
import com.routebuddy.reviews.dto.ReviewUpdateRequest
import com.routebuddy.reviews.entity.Review
import com.routebuddy.reviews.exception.DuplicateReviewException
import com.routebuddy.reviews.exception.ReviewNotFoundException
import com.routebuddy.reviews.repository.ReviewRepository
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

class ReviewServiceTest {

    private lateinit var reviewRepository: ReviewRepository
    private lateinit var reviewService: ReviewService

    @BeforeEach
    fun setUp() {
        reviewRepository = mockk()
        reviewService = ReviewService(reviewRepository)
    }

    @Test
    fun `createReview should save review when user has not reviewed route before`() {
        val userId = 1L
        val request = ReviewRequest(routeId = 10L, text = "Great route!", rating = 9)
        val review = Review(
            id = 1,
            routeId = request.routeId,
            userId = userId,
            text = request.text,
            rating = request.rating,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { reviewRepository.existsByRouteIdAndUserId(request.routeId, userId) } returns false
        every { reviewRepository.save(any()) } returns review

        val response = reviewService.createReview(userId, request)

        assertThat(response.id).isEqualTo(1)
        assertThat(response.rating).isEqualTo(9)
        assertThat(response.text).isEqualTo("Great route!")
        verify(exactly = 1) { reviewRepository.save(any()) }
    }

    @Test
    fun `createReview should throw DuplicateReviewException when user already reviewed route`() {
        val userId = 1L
        val request = ReviewRequest(routeId = 10L, text = "Great route!", rating = 9)

        every { reviewRepository.existsByRouteIdAndUserId(request.routeId, userId) } returns true

        assertThrows<DuplicateReviewException> {
            reviewService.createReview(userId, request)
        }
        verify(exactly = 0) { reviewRepository.save(any()) }
    }

    @Test
    fun `getReviewsByRouteId should return page of reviews`() {
        val routeId = 10L
        val pageable = PageRequest.of(0, 10)
        val reviews = listOf(
            Review(1, routeId, 1L, "text1", 8, LocalDateTime.now(), LocalDateTime.now()),
            Review(2, routeId, 2L, "text2", 9, LocalDateTime.now(), LocalDateTime.now())
        )
        val page = PageImpl(reviews, pageable, reviews.size.toLong())

        every { reviewRepository.findByRouteId(routeId, pageable) } returns page

        val result = reviewService.getReviewsByRouteId(routeId, pageable)

        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.content).hasSize(2)
        verify(exactly = 1) { reviewRepository.findByRouteId(routeId, pageable) }
    }

    @Test
    fun `getAverageRating should return 0 when no reviews`() {
        val routeId = 10L
        every { reviewRepository.findAverageRatingByRouteId(routeId) } returns null

        val avg = reviewService.getAverageRating(routeId)

        assertThat(avg).isEqualTo(0.0)
    }

    @Test
    fun `getAverageRating should return correct average`() {
        val routeId = 10L
        every { reviewRepository.findAverageRatingByRouteId(routeId) } returns 8.5

        val avg = reviewService.getAverageRating(routeId)

        assertThat(avg).isEqualTo(8.5)
    }

    @Test
    fun `updateReview should update when user is author`() {
        val reviewId = 1L
        val userId = 1L
        val existingReview = Review(
            id = reviewId,
            routeId = 10L,
            userId = userId,
            text = "old",
            rating = 5,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val updateRequest = ReviewUpdateRequest(text = "new text", rating = 9)

        every { reviewRepository.findById(reviewId) } returns Optional.of(existingReview)
        every { reviewRepository.save(any()) } answers { firstArg() }

        val updated = reviewService.updateReview(reviewId, userId, updateRequest)

        assertThat(updated.text).isEqualTo("new text")
        assertThat(updated.rating).isEqualTo(9)
        verify(exactly = 1) { reviewRepository.save(existingReview) }
    }

    @Test
    fun `updateReview should throw IllegalStateException when user is not author`() {
        val reviewId = 1L
        val userId = 1L
        val authorId = 2L
        val existingReview = Review(
            id = reviewId,
            routeId = 10L,
            userId = authorId,
            text = "old",
            rating = 5,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val updateRequest = ReviewUpdateRequest(text = "new", rating = 9)

        every { reviewRepository.findById(reviewId) } returns Optional.of(existingReview)

        assertThrows<IllegalStateException> {
            reviewService.updateReview(reviewId, userId, updateRequest)
        }
        verify(exactly = 0) { reviewRepository.save(any()) }
    }

    @Test
    fun `deleteReview should delete when user is author`() {
        val reviewId = 1L
        val userId = 1L
        val existingReview = Review(
            id = reviewId,
            routeId = 10L,
            userId = userId,
            text = "text",
            rating = 5,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { reviewRepository.findById(reviewId) } returns Optional.of(existingReview)
        justRun { reviewRepository.delete(existingReview) }

        reviewService.deleteReview(reviewId, userId)

        verify(exactly = 1) { reviewRepository.delete(existingReview) }
    }

    @Test
    fun `deleteReview should throw IllegalStateException when user is not author`() {
        val reviewId = 1L
        val userId = 1L
        val authorId = 2L
        val existingReview = Review(
            id = reviewId,
            routeId = 10L,
            userId = authorId,
            text = "text",
            rating = 5,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { reviewRepository.findById(reviewId) } returns Optional.of(existingReview)

        assertThrows<IllegalStateException> {
            reviewService.deleteReview(reviewId, userId)
        }
        verify(exactly = 0) { reviewRepository.delete(any()) }
    }

    @Test
    fun `deleteReview should throw ReviewNotFoundException when review not found`() {
        val reviewId = 1L
        val userId = 1L

        every { reviewRepository.findById(reviewId) } returns Optional.empty()

        assertThrows<ReviewNotFoundException> {
            reviewService.deleteReview(reviewId, userId)
        }
    }
}