package com.routebuddy.exportservice.dto

data class CurrentUserResponse(
    val userId: Long,
    val username: String,
)

data class ExportRouteListItem(
    val id: Long,
    val title: String,
    val days: List<ExportDayListItem>,
)

data class ExportDayListItem(
    val id: Long,
    val orderIndex: Int,
    val theme: String,
)
