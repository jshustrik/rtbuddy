package com.routebuddy.makeservice.service

import com.routebuddy.makeservice.domain.Route
import com.routebuddy.makeservice.domain.RouteDay
import com.routebuddy.makeservice.domain.RoutePoint
import com.routebuddy.makeservice.dto.RouteDayResponse
import com.routebuddy.makeservice.dto.RoutePointResponse
import com.routebuddy.makeservice.dto.RouteResponse
import com.routebuddy.makeservice.dto.RouteSummaryResponse
import java.time.Instant

object RouteMapper {

    fun toSummary(route: Route): RouteSummaryResponse =
        RouteSummaryResponse(
            id = route.id,
            authorUserId = route.authorUserId,
            authorUsername = route.authorUsername,
            title = route.title,
            description = route.description,
            durationDays = route.durationDays,
            totalCostRub = route.totalCostRub,
            published = route.published,
            createdAt = route.createdAt,
            updatedAt = route.updatedAt,
            dayCount = route.days.size,
        )

    fun toResponse(route: Route): RouteResponse {
        val days = route.days.sortedBy { it.dayOrder }.map { toDayResponse(it) }
        return RouteResponse(
            id = route.id,
            authorUserId = route.authorUserId,
            authorUsername = route.authorUsername,
            title = route.title,
            description = route.description,
            durationDays = route.durationDays,
            totalCostRub = route.totalCostRub,
            published = route.published,
            shareToken = route.shareToken,
            createdAt = route.createdAt,
            updatedAt = route.updatedAt,
            days = days,
        )
    }

    fun toDayResponse(day: RouteDay): RouteDayResponse {
        val points = day.points.sortedBy { it.orderIndex }.map { toPointResponse(it) }
        return RouteDayResponse(
            id = day.id,
            dayOrder = day.dayOrder,
            theme = day.theme,
            description = day.description,
            dayCostRub = day.dayCostRub,
            travelTimeMinutes = day.travelTimeMinutes,
            points = points,
        )
    }

    fun toPointResponse(p: RoutePoint): RoutePointResponse =
        RoutePointResponse(
            id = p.id,
            orderIndex = p.orderIndex,
            title = p.title,
            latitude = p.latitude,
            longitude = p.longitude,
            description = p.description,
            timeStart = p.timeStart,
            timeEnd = p.timeEnd,
            stayMinutes = p.stayMinutes,
            imageUrls = p.imageUrls.toList(),
        )

    fun touch(route: Route) {
        route.updatedAt = Instant.now()
    }
}
