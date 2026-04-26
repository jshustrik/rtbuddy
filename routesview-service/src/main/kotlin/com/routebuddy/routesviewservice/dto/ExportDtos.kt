package com.routebuddy.routesviewservice.dto

data class ExportRouteListItem(
    val id: Long,
    val title: String,
    val days: List<ExportDayListItem> = emptyList(),
)

data class ExportDayListItem(
    val id: Long,
    val orderIndex: Int,
    val theme: String,
)
