package com.routebuddy.reviews.dto

data class RouteRatingResponse(
    val routeId: Long,
    val averageRating: Double,
    val reviewsCount: Long
)
