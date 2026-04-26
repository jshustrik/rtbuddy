package com.routebuddy.routesviewservice.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class MakePageResponse<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 20,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class MakeRouteSummaryResponse(
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class MakeRouteResponse(
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
    val days: List<MakeRouteDayResponse> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MakeRouteDayResponse(
    val id: Long,
    val dayOrder: Int,
    val theme: String,
    val description: String,
    val dayCostRub: BigDecimal,
    val travelTimeMinutes: Int,
    val points: List<MakeRoutePointResponse> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MakeRoutePointResponse(
    val id: Long,
    val orderIndex: Int,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    val timeStart: String? = null,
    val timeEnd: String? = null,
    val stayMinutes: Int? = null,
    val imageUrls: List<String> = emptyList(),
)

