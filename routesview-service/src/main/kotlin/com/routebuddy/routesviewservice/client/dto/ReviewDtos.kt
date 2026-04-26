package com.routebuddy.routesviewservice.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

data class ReviewCreateRequest(
    val routeId: Long,
    val text: String,
    val rating: Int,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReviewResponse(
    val id: String,
    val routeId: Long,
    val authorId: Long,
    val authorUsername: String,
    val comment: String,
    val rating: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromMap(map: Map<*, *>): ReviewResponse {
            // Best-effort mapping from Jackson-decoded LinkedHashMap values.
            fun num(key: String): Long = (map[key] as Number).toLong()
            fun str(key: String): String = map[key] as String
            fun int(key: String): Int = (map[key] as Number).toInt()
            fun ldt(key: String): LocalDateTime = LocalDateTime.parse(map[key] as String)

            return ReviewResponse(
                id = str("id"),
                routeId = num("routeId"),
                authorId = num("authorId"),
                authorUsername = str("authorUsername"),
                comment = str("comment"),
                rating = int("rating"),
                createdAt = ldt("createdAt"),
                updatedAt = ldt("updatedAt"),
            )
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class RouteRatingResponse(
    val routeId: Long,
    val averageRating: Double,
    val reviewsCount: Long,
)

