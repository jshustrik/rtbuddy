package com.routebuddy.reviews.repository

import com.routebuddy.reviews.entity.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository

interface ReviewRepository : MongoRepository<Review, String> {
    fun findByRouteId(routeId: Long, pageable: Pageable): Page<Review>
    fun existsByRouteIdAndUserId(routeId: Long, userId: Long): Boolean
    fun findByRouteIdAndUserId(routeId: Long, userId: Long): Review?
    fun countByRouteId(routeId: Long): Long

    @Aggregation(pipeline = [
        "{ '\$match': { 'routeId': ?0 } }",
        "{ '\$group': { '_id': null, 'avg': { '\$avg': '\$rating' } } }"
    ])
    fun findAverageRatingByRouteId(routeId: Long): Double?
}
