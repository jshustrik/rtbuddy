package com.routebuddy.routesviewservice.client

import com.routebuddy.routesviewservice.client.dto.ReviewCreateRequest
import com.routebuddy.routesviewservice.client.dto.ReviewResponse
import com.routebuddy.routesviewservice.client.dto.RouteRatingResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ReviewServiceClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${routebuddy.review-service-url:http://127.0.0.1:8085}") reviewServiceBaseUrl: String,
) {
    private val client: RestClient = restClientBuilder.baseUrl(reviewServiceBaseUrl).build()

    fun getRouteRating(routeId: Long): RouteRatingResponse =
        client.get()
            .uri("/api/reviews/routes/{routeId}/rating", routeId)
            .retrieve()
            .body(RouteRatingResponse::class.java)!!

    /**
     * review-service returns Spring Page JSON. We map only the needed fields.
     */
    fun listReviews(routeId: Long, page: Int, size: Int): PageImpl<ReviewResponse> {
        val payload: Map<String, Any?> =
            client.get()
                .uri { b ->
                    b.path("/api/reviews/routes/{routeId}")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(routeId)
                }
                .retrieve()
                .body(object : ParameterizedTypeReference<Map<String, Any?>>() {})!!

        val content = (payload["content"] as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
        val reviews = content.map { ReviewResponse.fromMap(it) }

        val totalElements = (payload["totalElements"] as? Number)?.toLong() ?: reviews.size.toLong()
        return PageImpl(reviews, PageRequest.of(page, size), totalElements)
    }

    fun createReview(
        userId: Long,
        username: String,
        request: ReviewCreateRequest,
    ): ReviewResponse =
        client.post()
            .uri("/api/reviews")
            .header("X-User-Id", userId.toString())
            .header("X-Username", username)
            .body(request)
            .retrieve()
            .body(ReviewResponse::class.java)!!
}

