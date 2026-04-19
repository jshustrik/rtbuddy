package com.routebuddy.makeservice.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.Instant

data class CreateRouteRequest(
    val title: String,
    val description: String,
    val durationDays: Int,
    val totalCostRub: BigDecimal,
    val published: Boolean = true,
    val days: List<CreateDayRequest> = emptyList(),
)

data class CreateDayRequest(
    val theme: String,
    val description: String,
    val dayCostRub: BigDecimal,
    val dayOrder: Int? = null,
    val points: List<CreatePointRequest>,
)

data class CreatePointRequest(
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    val timeStart: String? = null,
    val timeEnd: String? = null,
    val stayMinutes: Int? = null,
    val orderIndex: Int? = null,
    val imageUrls: List<String> = emptyList(),
)

data class UpdateRouteRequest(
    val title: String,
    val description: String,
    val durationDays: Int,
    val totalCostRub: BigDecimal,
    val published: Boolean = true,
)

data class ReorderDaysRequest(
    val dayIdsInOrder: List<Long>,
)

data class ShareLinkResponse(
    val routeId: Long,
    val shareToken: String?,
)

data class ComposeRouteRequest(
    val title: String,
    val description: String,
    val published: Boolean = true,
    val dayIds: List<Long>,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RouteResponse(
    val id: Long,
    val authorUserId: Long,
    val authorUsername: String,
    val title: String,
    val description: String,
    val durationDays: Int,
    val totalCostRub: BigDecimal,
    val published: Boolean,
    val shareToken: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val days: List<RouteDayResponse>,
)

data class RouteDayResponse(
    val id: Long,
    val dayOrder: Int,
    val theme: String,
    val description: String,
    val dayCostRub: BigDecimal,
    val travelTimeMinutes: Int,
    val points: List<RoutePointResponse>,
)

data class RoutePointResponse(
    val id: Long,
    val orderIndex: Int,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val timeStart: String?,
    val timeEnd: String?,
    val stayMinutes: Int?,
    val imageUrls: List<String>,
)

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RouteSummaryResponse(
    val id: Long,
    val authorUserId: Long,
    val authorUsername: String,
    val title: String,
    val description: String,
    val durationDays: Int,
    val totalCostRub: BigDecimal,
    val published: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val dayCount: Int,
)
