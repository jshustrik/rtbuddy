package com.routebuddy.routesviewservice.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class RouteDto(
    val id: Long,
    val title: String,
    val description: String,
    val authorId: Long,
    val authorUsername: String,
    val difficulty: String,
    val distance: Double,
    val duration: String,
    val city: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val points: List<RoutePointDto> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RoutePointDto(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String,
    val orderIndex: Int
)