package com.routebuddy.makeservice.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Оценка времени в пути между точками (как на фронтенде create2): расстояние по дуге + скорость 5 км/ч.
 */
object TravelTimeEstimator {

    private const val EARTH_RADIUS_M = 6371000.0
    private const val WALK_SPEED_M_PER_MIN = 5000.0 / 60.0 // 5 км/ч

    fun totalTravelMinutes(points: List<Pair<Double, Double>>): Int {
        if (points.size < 2) return 0
        var totalMeters = 0.0
        for (i in 0 until points.size - 1) {
            totalMeters += haversineMeters(points[i].first, points[i].second, points[i + 1].first, points[i + 1].second)
        }
        val minutes = totalMeters / WALK_SPEED_M_PER_MIN
        return minutes.toInt().coerceAtLeast(0)
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val Δφ = Math.toRadians(lat2 - lat1)
        val Δλ = Math.toRadians(lon2 - lon1)
        val a = sin(Δφ / 2).pow(2) + cos(φ1) * cos(φ2) * sin(Δλ / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }
}
