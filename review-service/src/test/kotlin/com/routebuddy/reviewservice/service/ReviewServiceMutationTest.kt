package com.routebuddy.reviews.service

import com.routebuddy.reviews.dto.ReviewUpdateRequest
import com.routebuddy.reviews.entity.Review
import com.routebuddy.reviews.repository.ReviewRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.Optional

class ReviewServiceMutationTest {

    private lateinit var reviewRepository: ReviewRepository
    private lateinit var reviewService: ReviewService

    @BeforeEach
    fun setUp() {
        reviewRepository = mock(ReviewRepository::class.java)
        reviewService = ReviewService(reviewRepository)
    }

    @Test
    fun `updateReview changes own comment and rating`() {
        val now = LocalDateTime.now()
        val review = Review("r1", 10L, 1L, "alice", "old text", 5, now, now)
        `when`(reviewRepository.findById("r1")).thenReturn(Optional.of(review))
        `when`(reviewRepository.save(any(Review::class.java))).thenAnswer { it.arguments[0] as Review }

        val result = reviewService.updateReview("r1", 1L, ReviewUpdateRequest("new useful text", 9))

        assertThat(result.comment).isEqualTo("new useful text")
        assertThat(result.rating).isEqualTo(9)
    }

    @Test
    fun `deleteReview removes own review`() {
        val now = LocalDateTime.now()
        val review = Review("r1", 10L, 1L, "alice", "old text", 5, now, now)
        `when`(reviewRepository.findById("r1")).thenReturn(Optional.of(review))

        reviewService.deleteReview("r1", 1L)

        verify(reviewRepository).delete(review)
    }

    @Test
    fun `getRouteRating rounds average to one digit`() {
        `when`(reviewRepository.findAverageRatingByRouteId(10L)).thenReturn(8.666)
        `when`(reviewRepository.countByRouteId(10L)).thenReturn(3L)

        val result = reviewService.getRouteRating(10L)

        assertThat(result.averageRating).isEqualTo(8.7)
        assertThat(result.reviewsCount).isEqualTo(3)
    }
}
