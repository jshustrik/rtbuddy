package com.routebuddy.makeservice.dto

data class CreateDayRequest(
    val dayNumber: Int,
    val title: String,
    val description: String,
    val cost: Double,
    val points: List<CreatePointRequest> = emptyList()
)