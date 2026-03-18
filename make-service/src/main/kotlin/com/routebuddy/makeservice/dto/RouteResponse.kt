package com.routebuddy.makeservice.dto

data class RouteResponse(
    val id: String,
    val title: String,
    val description: String,
    val shortDescription: String,
    val authorId: String,
    val authorUsername: String,
    val createdAt: String,
    // другие поля
)