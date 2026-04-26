package com.routebuddy.exportservice.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class MakePageResponse<T>(
    val content: List<T> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MakeRouteSummaryResponse(
    val id: Long,
    val title: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MakeRouteResponse(
    val id: Long,
    val authorUserId: Long,
    val authorUsername: String,
    val title: String,
    val description: String,
    val durationDays: Int,
    val totalCostRub: BigDecimal,
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
    val imageUrls: List<String> = emptyList(),
)
