package com.routebuddy.reviewservice.controller

import com.routebuddy.reviews.controller.ApiReviewController
import com.routebuddy.reviews.dto.ReviewRequest
import com.routebuddy.reviews.dto.ReviewResponse
import com.routebuddy.reviews.dto.RouteRatingResponse
import com.routebuddy.reviews.service.ReviewService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.mockito.Mockito.mock
import java.time.LocalDateTime

class ReviewServiceControllerTest {

    private val reviewService = mock(ReviewService::class.java)
    private val controller = ApiReviewController(reviewService)

    @Test
    fun `create review delegates to service`() {
        val now = LocalDateTime.now()
        val response = ReviewResponse("r1", 10L, 1L, "ivan", "Хорошо", 8, now, now)
        `when`(reviewService.createReview(1L, "ivan", ReviewRequest(10L, "Хорошо", 8))).thenReturn(response)

        val result = controller.createReview(1L, "ivan", ReviewRequest(10L, "Хорошо", 8))

        assertThat(result.id).isEqualTo("r1")
        assertThat(result.authorUsername).isEqualTo("ivan")
        verify(reviewService).createReview(1L, "ivan", ReviewRequest(10L, "Хорошо", 8))
    }

    @Test
    fun `route rating returns value`() {
        `when`(reviewService.getRouteRating(10L)).thenReturn(RouteRatingResponse(10L, 8.5, 2))
        val result = controller.getRouteRating(10L)

        assertThat(result.averageRating).isEqualTo(8.5)
        assertThat(result.reviewsCount).isEqualTo(2)
    }

    @Test
    fun `route reviews returns page content`() {
        val now = LocalDateTime.now()
        val page = PageImpl(
            listOf(ReviewResponse("r1", 10L, 1L, "ivan", "Комментарий", 9, now, now)),
            PageRequest.of(0, 20),
            1
        )
        `when`(reviewService.getReviewsByRouteId(10L, PageRequest.of(0, 20))).thenReturn(page)

        val result = controller.getReviewsByRoute(10L, 0, 20)
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().rating).isEqualTo(9)
    }
}