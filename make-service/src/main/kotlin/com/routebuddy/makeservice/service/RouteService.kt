package com.routebuddy.makeservice.service

import com.routebuddy.makeservice.dto.*
import com.routebuddy.makeservice.model.Day
import com.routebuddy.makeservice.model.Route
import com.routebuddy.makeservice.model.RoutePoint
import com.routebuddy.makeservice.repository.DayRepository
import com.routebuddy.makeservice.repository.RoutePointRepository
import com.routebuddy.makeservice.repository.RouteRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.LocalDateTime

@Service
@Transactional
class RouteService(
    private val routeRepository: RouteRepository,
    private val dayRepository: DayRepository,
    private val pointRepository: RoutePointRepository
) {

    // ── Routes ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    fun getPublicRoutes(search: String? = null): List<RouteDto> =
        routeRepository.searchPublic(search).map { RouteDto.from(it, includeDays = false) }

    @Transactional(readOnly = true)
    fun getMyRoutes(userId: Long, search: String? = null): List<RouteDto> =
        routeRepository.searchByAuthor(userId, search).map { RouteDto.from(it, includeDays = false) }

    @Transactional(readOnly = true)
    fun getRoute(id: Long): RouteDto {
        val route = routeRepository.findById(id).orElseThrow { NoSuchElementException("Route $id not found") }
        return RouteDto.from(route)
    }

    fun createRoute(req: CreateRouteRequest, authorId: Long, authorUsername: String): RouteDto {
        val route = Route(
            title = req.title,
            description = req.description,
            authorId = authorId,
            authorUsername = authorUsername,
            tags = req.tags,
            isPublic = req.isPublic,
            durationDays = req.durationDays,
            totalCost = req.totalCost
        )
        return RouteDto.from(routeRepository.save(route), includeDays = false)
    }

    fun updateRoute(id: Long, req: UpdateRouteRequest, userId: Long): RouteDto {
        val route = routeRepository.findById(id).orElseThrow { NoSuchElementException("Route $id not found") }
        if (route.authorId != userId) throw AccessDeniedException("Not the author")
        route.title = req.title
        route.description = req.description
        route.tags = req.tags
        route.isPublic = req.isPublic
        route.durationDays = req.durationDays
        route.totalCost = req.totalCost
        route.updatedAt = LocalDateTime.now()
        return RouteDto.from(routeRepository.save(route), includeDays = false)
    }

    fun deleteRoute(id: Long, userId: Long) {
        val route = routeRepository.findById(id).orElseThrow { NoSuchElementException("Route $id not found") }
        if (route.authorId != userId) throw AccessDeniedException("Not the author")
        routeRepository.delete(route)
    }

    // ── Days ─────────────────────────────────────────────────────────────────

    fun addDay(routeId: Long, req: CreateDayRequest, userId: Long): DayDto {
        validatePhotoUrl(req.photoUrl)
        val route = routeRepository.findById(routeId).orElseThrow { NoSuchElementException("Route $routeId not found") }
        if (route.authorId != userId) throw AccessDeniedException("Not the author")
        val day = Day(
            route = route,
            dayNumber = req.dayNumber,
            title = req.title,
            description = req.description,
            photoUrl = req.photoUrl,
            cost = req.cost,
            travelTime = req.travelTime
        )
        val saved = dayRepository.save(day)
        route.updatedAt = LocalDateTime.now()
        routeRepository.save(route)
        return DayDto.from(saved)
    }

    fun updateDay(routeId: Long, dayId: Long, req: UpdateDayRequest, userId: Long): DayDto {
        validatePhotoUrl(req.photoUrl)
        val route = routeRepository.findById(routeId).orElseThrow { NoSuchElementException("Route $routeId not found") }
        if (route.authorId != userId) throw AccessDeniedException("Not the author")
        val day = dayRepository.findById(dayId).orElseThrow { NoSuchElementException("Day $dayId not found") }
        ensureDayBelongsToRoute(day, route)
        day.dayNumber = req.dayNumber
        day.title = req.title
        day.description = req.description
        day.photoUrl = req.photoUrl
        day.cost = req.cost
        day.travelTime = req.travelTime
        route.updatedAt = LocalDateTime.now()
        routeRepository.save(route)
        return DayDto.from(dayRepository.save(day))
    }

    fun deleteDay(routeId: Long, dayId: Long, userId: Long) {
        val route = routeRepository.findById(routeId).orElseThrow { NoSuchElementException("Route $routeId not found") }
        if (route.authorId != userId) throw AccessDeniedException("Not the author")
        val day = dayRepository.findById(dayId).orElseThrow { NoSuchElementException("Day $dayId not found") }
        ensureDayBelongsToRoute(day, route)
        dayRepository.delete(day)
        route.updatedAt = LocalDateTime.now()
        routeRepository.save(route)
    }

    // ── Points ────────────────────────────────────────────────────────────────

    fun addPoint(routeId: Long, dayId: Long, req: CreatePointRequest, userId: Long): RoutePointDto {
        validatePhotoUrl(req.photoUrl)
        val route = routeRepository.findById(routeId).orElseThrow { NoSuchElementException("Route $routeId not found") }
        if (route.authorId != userId) throw AccessDeniedException("Not the author")
        val day = dayRepository.findById(dayId).orElseThrow { NoSuchElementException("Day $dayId not found") }
        ensureDayBelongsToRoute(day, route)
        val point = RoutePoint(
            day = day,
            orderIndex = req.orderIndex,
            name = req.name,
            description = req.description,
            lat = req.lat,
            lon = req.lon,
            photoUrl = req.photoUrl,
            timeStart = req.timeStart,
            timeEnd = req.timeEnd
        )
        val saved = pointRepository.save(point)
        route.updatedAt = LocalDateTime.now()
        routeRepository.save(route)
        return RoutePointDto.from(saved)
    }

    fun updatePoint(routeId: Long, dayId: Long, pointId: Long, req: UpdatePointRequest, userId: Long): RoutePointDto {
        validatePhotoUrl(req.photoUrl)
        val route = routeRepository.findById(routeId).orElseThrow { NoSuchElementException("Route $routeId not found") }
        if (route.authorId != userId) throw AccessDeniedException("Not the author")
        val point = pointRepository.findById(pointId).orElseThrow { NoSuchElementException("Point $pointId not found") }
        ensurePointBelongsToDayAndRoute(point, dayId, route)
        point.orderIndex = req.orderIndex
        point.name = req.name
        point.description = req.description
        point.lat = req.lat
        point.lon = req.lon
        point.photoUrl = req.photoUrl
        point.timeStart = req.timeStart
        point.timeEnd = req.timeEnd
        route.updatedAt = LocalDateTime.now()
        routeRepository.save(route)
        return RoutePointDto.from(pointRepository.save(point))
    }

    fun deletePoint(routeId: Long, dayId: Long, pointId: Long, userId: Long) {
        val route = routeRepository.findById(routeId).orElseThrow { NoSuchElementException("Route $routeId not found") }
        if (route.authorId != userId) throw AccessDeniedException("Not the author")
        val point = pointRepository.findById(pointId).orElseThrow { NoSuchElementException("Point $pointId not found") }
        ensurePointBelongsToDayAndRoute(point, dayId, route)
        pointRepository.delete(point)
        route.updatedAt = LocalDateTime.now()
        routeRepository.save(route)
    }

    private fun ensureDayBelongsToRoute(day: Day, route: Route) {
        if (day.route.id != route.id) throw AccessDeniedException("Day does not belong to route")
    }

    private fun ensurePointBelongsToDayAndRoute(point: RoutePoint, dayId: Long, route: Route) {
        if (point.day.id != dayId || point.day.route.id != route.id) {
            throw AccessDeniedException("Point does not belong to route day")
        }
    }

    private fun validatePhotoUrl(value: String?) {
        if (value.isNullOrBlank()) return
        val dataImage = Regex("^data:image/(jpeg|png);base64,[A-Za-z0-9+/=\\r\\n]+$", RegexOption.IGNORE_CASE)
        require(dataImage.matches(value) || isHttpImageUrl(value)) {
            "Фото должно быть ссылкой http(s) или JPG/PNG до 5 МБ"
        }
    }

    private fun isHttpImageUrl(value: String): Boolean =
        runCatching {
            require(!value.any { it.isISOControl() })
            val uri = URI(value.trim())
            val scheme = uri.scheme?.lowercase()
            scheme in setOf("http", "https") && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
}
