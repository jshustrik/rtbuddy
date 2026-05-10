package com.routebuddy.exportservice.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime
import java.time.OffsetDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExportRouteDto(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val authorId: Long = 0,
    val authorUsername: String = "",
    val tags: String = "",
    val isPublic: Boolean = true,
    val durationDays: Int = 1,
    val totalCost: Double = 0.0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val days: List<ExportDayDto> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExportDayDto(
    val id: Long = 0,
    val dayNumber: Int = 1,
    val title: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val cost: Double = 0.0,
    val travelTime: String = "",
    val points: List<ExportPointDto> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExportPointDto(
    val id: Long = 0,
    val orderIndex: Int = 0,
    val name: String = "",
    val description: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val photoUrl: String? = null,
    val timeStart: String? = null,
    val timeEnd: String? = null
)

data class ExportRouteResponse(
    val route: ExportRouteDto,
    val routeStaticMapUrl: String?,
    val dayStaticMapUrls: Map<Long, String>,
    val generatedAt: OffsetDateTime = OffsetDateTime.now()
)
