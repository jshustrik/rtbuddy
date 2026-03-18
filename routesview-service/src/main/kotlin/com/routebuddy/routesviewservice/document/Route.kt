package com.routebuddy.routesviewservice.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "routes")
data class Route(
    @Id
    val id: String? = null,

    @Field("title")
    val title: String,

    @Field("description")
    val description: String,

    @Field("author_id")
    val authorId: String,

    @Field("author_username")
    val authorUsername: String,

    @Field("difficulty")
    val difficulty: RouteDifficulty = RouteDifficulty.MEDIUM,

    @Field("distance")
    val distance: Double,

    @Field("duration")
    val duration: String,

    @Field("city")
    val city: String,

    @Field("points")
    val points: List<RoutePoint> = emptyList(),

    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Field("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Field("is_public")
    val isPublic: Boolean = true,

    @Field("views")
    val views: Int = 0,

    @Field("tags")
    val tags: List<String> = emptyList()
)

@Document(collection = "route_points")
data class RoutePoint(
    @Id
    val id: String? = null,

    @Field("route_id")
    val routeId: String,

    @Field("latitude")
    val latitude: Double,

    @Field("longitude")
    val longitude: Double,

    @Field("title")
    val title: String,

    @Field("description")
    val description: String,

    @Field("order_index")
    val orderIndex: Int,

    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class RouteDifficulty {
    BEGINNER,
    EASY,
    MEDIUM,
    HARD,
    EXPERT
}