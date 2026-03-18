package com.routebuddy.makeservice.dto

data class CreateRouteRequest(
    val title: String,
    val description: String,
    val shortDescription: String,
    val difficulty: String,
    val distance: Double,
    val durationDays: Int,
    val totalCost: Double,
    val city: String,
    val isPublic: Boolean = true,
    val tags: List<String> = emptyList(),
    val days: List<CreateDayRequest> = emptyList()
)