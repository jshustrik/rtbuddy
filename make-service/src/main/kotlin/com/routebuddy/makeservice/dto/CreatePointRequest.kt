package com.routebuddy.makeservice.dto

data class CreatePointRequest(
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String,
    val type: String,
    val cost: Double?,
    val timeSpent: Int?,
    val orderIndex: Int
)