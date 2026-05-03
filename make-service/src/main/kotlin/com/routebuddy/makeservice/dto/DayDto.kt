package com.routebuddy.makeservice.dto

import com.routebuddy.makeservice.model.Day
import jakarta.validation.constraints.NotBlank

data class DayDto(
    val id: Long = 0,
    val dayNumber: Int = 1,
    @field:NotBlank val title: String,
    val description: String = "",
    val photoUrl: String? = null,
    val cost: Double = 0.0,
    val travelTime: String = "",
    val points: List<RoutePointDto> = emptyList()
) {
    companion object {
        fun from(d: Day) = DayDto(
            id = d.id,
            dayNumber = d.dayNumber,
            title = d.title,
            description = d.description,
            photoUrl = d.photoUrl,
            cost = d.cost,
            travelTime = d.travelTime,
            points = d.points.map { RoutePointDto.from(it) }
        )
    }
}
