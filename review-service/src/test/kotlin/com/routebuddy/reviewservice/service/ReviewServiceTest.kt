package com.routebuddy.reviews.service

import com.routebuddy.reviews.dto.ReviewRequest
import com.routebuddy.reviews.dto.ReviewUpdateRequest
import com.routebuddy.reviews.entity.Review
import com.routebuddy.reviews.exception.DuplicateReviewException
import com.routebuddy.reviews.exception.ReviewNotFoundException
import com.routebuddy.reviews.repository.ReviewRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.Optional

class ReviewServiceTest {

    private lateinit var reviewRepository: ReviewRepository
    private lateinit var reviewService: ReviewService

    @BeforeEach
    fun setUp() {
        reviewRepository = mock(ReviewRepository::class.java)
        reviewService = ReviewService(reviewRepository)
    }

    @Test
    fun `createReview should save review when valid`() {
        val now = LocalDateTime.now()
        `when`(reviewRepository.existsByRouteIdAndUserId(10L, 1L)).thenReturn(false)
        `when`(reviewRepository.save(any(Review::class.java))).thenReturn(
            Review(
                id = "r1",
                routeId = 10L,
                userId = 1L,
                authorUsername = "ivan",
                text = "Отличный маршрут",
                rating = 9,
                createdAt = now,
                updatedAt = now
            )
        )

        val result = reviewService.createReview(1L, "ivan", ReviewRequest(10L, "Отличный маршрут", 9))

        assertThat(result.id).isEqualTo("r1")
        assertThat(result.comment).isEqualTo("Отличный маршрут")
        verify(reviewRepository, times(1)).save(any(Review::class.java))
    }

    @Test
    fun `createReview should fail when duplicate`() {
        `when`(reviewRepository.existsByRouteIdAndUserId(10L, 1L)).thenReturn(true)

        assertThrows<DuplicateReviewException> {
            reviewService.createReview(1L, "ivan", ReviewRequest(10L, "Отличный маршрут", 9))
        }
    }

    @Test
    fun `createReview should fail when text has links`() {
        assertThrows<IllegalArgumentException> {
            reviewService.createReview(1L, "ivan", ReviewRequest(10L, "Смотрите https://spam", 8))
        }
    }

    @Test
    fun `getReviewsByRouteId should map entity page`() {
        val now = LocalDateTime.now()
        val page = PageImpl(
            listOf(
                Review("r1", 10L, 1L, "u1", "ok text", 8, now, now),
                Review("r2", 10L, 2L, "u2", "nice text", 9, now, now)
            ),
            PageRequest.of(0, 10),
            2
        )
        `when`(reviewRepository.findByRouteId(10L, PageRequest.of(0, 10))).thenReturn(page)

        val result = reviewService.getReviewsByRouteId(10L, PageRequest.of(0, 10))

        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.content.first().comment).isEqualTo("ok text")
    }

    @Test
    fun `updateReview should fail if not author`() {
        val now = LocalDateTime.now()
        val review = Review("r1", 10L, 2L, "u2", "old text", 5, now, now)
        `when`(reviewRepository.findById("r1")).thenReturn(Optional.of(review))

        assertThrows<IllegalStateException> {
            reviewService.updateReview("r1", 1L, ReviewUpdateRequest("new text", 9))
        }
    }

    @Test
    fun `deleteReview should fail if missing`() {
        `when`(reviewRepository.findById("missing")).thenReturn(Optional.empty())

        assertThrows<ReviewNotFoundException> {
            reviewService.deleteReview("missing", 1L)
        }
    }
}