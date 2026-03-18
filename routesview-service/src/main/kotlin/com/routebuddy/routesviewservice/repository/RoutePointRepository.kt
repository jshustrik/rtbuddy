package com.routebuddy.routesviewservice.repository

import com.routebuddy.routesviewservice.document.RoutePoint
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RoutePointRepository : MongoRepository<RoutePoint, String> {

    fun findByRouteId(routeId: String): List<RoutePoint>

    fun deleteByRouteId(routeId: String)
}