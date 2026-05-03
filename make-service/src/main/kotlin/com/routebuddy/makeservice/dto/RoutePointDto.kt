package com.routebuddy.makeservice.dto

import com.routebuddy.makeservice.model.RoutePoint
import jakarta.validation.constraints.NotBlank

data class RoutePointDto(
    val id: Long = 0,
    val orderIndex: Int = 0,
    @field:NotBlank val name: String,
    val description: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val photoUrl: String? = null,
    val timeStart: String? = null,
    val timeEnd: String? = null
) {
    companion object {
        fun from(p: RoutePoint) = RoutePointDto(
            id = p.id,
            orderIndex = p.orderIndex,
            name = p.name,
            description = p.description,
            lat = p.lat,
            lon = p.lon,
            photoUrl = p.photoUrl,
            timeStart = p.timeStart,
            timeEnd = p.timeEnd
        )
    }
}
