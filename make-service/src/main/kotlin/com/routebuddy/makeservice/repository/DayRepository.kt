package com.routebuddy.makeservice.repository

import com.routebuddy.makeservice.model.Day
import org.springframework.data.jpa.repository.JpaRepository

interface DayRepository : JpaRepository<Day, Long> {
    fun findByRouteIdOrderByDayNumberAsc(routeId: Long): List<Day>
}
