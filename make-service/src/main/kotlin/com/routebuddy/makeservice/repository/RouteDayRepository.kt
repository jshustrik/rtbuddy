package com.routebuddy.makeservice.repository

import com.routebuddy.makeservice.domain.RouteDay
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RouteDayRepository : JpaRepository<RouteDay, Long> {
    @Query(
        """
        SELECT d FROM RouteDay d
        LEFT JOIN FETCH d.points p
        LEFT JOIN FETCH d.route r
        WHERE d.id = :id
        """,
    )
    fun findByIdWithPointsAndRoute(@Param("id") id: Long): RouteDay?
}

