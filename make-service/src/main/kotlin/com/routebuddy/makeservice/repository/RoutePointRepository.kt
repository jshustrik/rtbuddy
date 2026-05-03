package com.routebuddy.makeservice.repository

import com.routebuddy.makeservice.model.RoutePoint
import org.springframework.data.jpa.repository.JpaRepository

interface RoutePointRepository : JpaRepository<RoutePoint, Long> {
    fun findByDayIdOrderByOrderIndexAsc(dayId: Long): List<RoutePoint>
}
