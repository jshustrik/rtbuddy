package com.routebuddy.routesviewservice.client.dto

import java.math.BigDecimal

data class MakeRouteWriteRequest(
    val title: String,
    val description: String,
    val durationDays: Int,
    val totalCostRub: BigDecimal,
    val published: Boolean = true,
)

data class MakeRouteDayWriteRequest(
    val theme: String,
    val description: String,
    val dayCostRub: BigDecimal,
    val dayOrder: Int? = null,
    val points: List<MakeRoutePointWriteRequest>,
)

data class MakeRoutePointWriteRequest(
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

data class MakeComposeRouteRequest(
    val title: String,
    val description: String,
    val published: Boolean = true,
    val dayIds: List<Long>,
)
