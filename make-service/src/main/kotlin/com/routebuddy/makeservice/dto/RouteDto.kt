package com.routebuddy.makeservice.dto

import com.routebuddy.makeservice.model.Route
import java.time.LocalDateTime

data class RouteDto(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val authorId: Long,
    val authorUsername: String = "",
    val tags: String = "",
    val isPublic: Boolean = true,
    val durationDays: Int = 1,
    val totalCost: Double = 0.0,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val days: List<DayDto> = emptyList()
) {
    companion object {
        fun from(r: Route, includeDays: Boolean = true) = RouteDto(
            id = r.id,
            title = r.title,
            description = r.description,
            authorId = r.authorId,
            authorUsername = r.authorUsername,
            tags = r.tags,
            isPublic = r.isPublic,
            durationDays = r.durationDays,
            totalCost = r.totalCost,
            createdAt = r.createdAt,
            updatedAt = r.updatedAt,
            days = if (includeDays) r.days.map { DayDto.from(it) } else emptyList()
        )
    }
}
