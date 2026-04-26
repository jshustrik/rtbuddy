package com.routebuddy.makeservice.service

import com.routebuddy.makeservice.domain.Route
import com.routebuddy.makeservice.domain.RouteDay
import com.routebuddy.makeservice.domain.RoutePoint
import com.routebuddy.makeservice.dto.ComposeRouteRequest
import com.routebuddy.makeservice.dto.CreateDayRequest
import com.routebuddy.makeservice.dto.CreatePointRequest
import com.routebuddy.makeservice.dto.CreateRouteRequest
import com.routebuddy.makeservice.dto.PageResponse
import com.routebuddy.makeservice.dto.RouteResponse
import com.routebuddy.makeservice.dto.RouteSummaryResponse
import com.routebuddy.makeservice.dto.UpdateRouteRequest
import com.routebuddy.makeservice.repository.RouteDayRepository
import com.routebuddy.makeservice.repository.RouteRepository
import com.routebuddy.makeservice.util.TravelTimeEstimator
import com.routebuddy.makeservice.validation.TzConstraints
import com.routebuddy.makeservice.web.ResourceNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.security.access.AccessDeniedException
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64

@Service
class RouteService(
    private val routeRepository: RouteRepository,
    private val routeDayRepository: RouteDayRepository,
) {

    @Transactional
    fun createRoute(authorUserId: Long, authorUsername: String, req: CreateRouteRequest): RouteResponse {
        validateRouteMeta(req.title, req.description, req.durationDays, req.totalCostRub)
        require(req.days.isNotEmpty()) { "Добавьте хотя бы один день маршрута" }
        require(req.durationDays == req.days.size) {
            "Длительность маршрута (${req.durationDays} дн.) должна совпадать с количеством дней (${req.days.size})"
        }

        val route = Route(
            authorUserId = authorUserId,
            authorUsername = authorUsername,
            title = req.title.trim(),
            description = req.description.trim(),
            durationDays = req.durationDays,
            totalCostRub = req.totalCostRub.stripTrailingZeros(),
            published = req.published,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

        val sortedDays = req.days
            .mapIndexed { idx, d -> idx to d }
            .sortedBy { (idx, d) -> d.dayOrder ?: (idx + 1) }
        sortedDays.forEachIndexed { i, (_, d) ->
            val dayEntity = buildDayEntity(route, i + 1, d)
            route.days.add(dayEntity)
        }

        val saved = routeRepository.save(route)
        return RouteMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun getRoute(id: Long, userId: Long? = null, shareToken: String? = null): RouteResponse {
        val route = routeRepository.findById(id).orElseThrow { notFoundRoute() }
        ensureReadable(route, userId, shareToken)
        return RouteMapper.toResponse(route)
    }

    @Transactional(readOnly = true)
    fun listPublished(q: String?, page: Int, size: Int): PageResponse<RouteSummaryResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))
        val result = if (q.isNullOrBlank()) {
            routeRepository.findByPublishedTrue(pageable)
        } else {
            routeRepository.searchPublished(q.trim(), pageable)
        }
        return PageResponse(
            content = result.content.map { RouteMapper.toSummary(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    @Transactional(readOnly = true)
    fun listMine(authorUserId: Long, page: Int, size: Int): PageResponse<RouteSummaryResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))
        val result = routeRepository.findByAuthorUserIdOrderByUpdatedAtDesc(authorUserId, pageable)
        return PageResponse(
            content = result.content.map { RouteMapper.toSummary(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    @Transactional
    fun updateRoute(routeId: Long, userId: Long, req: UpdateRouteRequest): RouteResponse {
        val route = routeRepository.findById(routeId).orElseThrow { notFoundRoute() }
        require(route.authorUserId == userId) { forbidden() }
        validateRouteMeta(req.title, req.description, req.durationDays, req.totalCostRub)
        require(req.durationDays == route.days.size) {
            "Длительность (${req.durationDays} дн.) должна совпадать с числом дней в маршруте (${route.days.size}). Измените порядок дней или отредактируйте дни отдельно."
        }
        route.title = req.title.trim()
        route.description = req.description.trim()
        route.durationDays = req.durationDays
        route.totalCostRub = req.totalCostRub.stripTrailingZeros()
        route.published = req.published
        RouteMapper.touch(route)
        return RouteMapper.toResponse(routeRepository.save(route))
    }

    @Transactional
    fun deleteRoute(routeId: Long, userId: Long) {
        val route = routeRepository.findById(routeId).orElseThrow { notFoundRoute() }
        require(route.authorUserId == userId) { forbidden() }
        routeRepository.delete(route)
    }

    @Transactional
    fun addDay(routeId: Long, userId: Long, req: CreateDayRequest): RouteResponse {
        val route = routeRepository.findById(routeId).orElseThrow { notFoundRoute() }
        require(route.authorUserId == userId) { forbidden() }
        val nextOrder = (route.days.maxOfOrNull { it.dayOrder } ?: 0) + 1
        val order = req.dayOrder ?: nextOrder
        val dayEntity = buildDayEntity(route, order, req)
        route.days.add(dayEntity)
        route.durationDays = route.days.size
        RouteMapper.touch(route)
        return RouteMapper.toResponse(routeRepository.save(route))
    }

    @Transactional
    fun updateDay(routeId: Long, dayId: Long, userId: Long, req: CreateDayRequest): RouteResponse {
        val route = routeRepository.findById(routeId).orElseThrow { notFoundRoute() }
        require(route.authorUserId == userId) { forbidden() }
        val day = route.days.find { it.id == dayId } ?: throw ResourceNotFoundException("День не найден")
        day.dayOrder = req.dayOrder ?: day.dayOrder
        applyDayContent(day, req)
        RouteMapper.touch(route)
        return RouteMapper.toResponse(routeRepository.save(route))
    }

    @Transactional
    fun deleteDay(routeId: Long, dayId: Long, userId: Long): RouteResponse {
        val route = routeRepository.findById(routeId).orElseThrow { notFoundRoute() }
        require(route.authorUserId == userId) { forbidden() }
        require(route.days.size > 1) { "Нельзя удалить единственный день маршрута" }
        val day = route.days.find { it.id == dayId } ?: throw ResourceNotFoundException("День не найден")
        route.days.remove(day)
        route.durationDays = route.days.size
        normalizeDayOrders(route)
        RouteMapper.touch(route)
        return RouteMapper.toResponse(routeRepository.save(route))
    }

    @Transactional
    fun reorderDays(routeId: Long, userId: Long, dayIdsInOrder: List<Long>): RouteResponse {
        val route = routeRepository.findById(routeId).orElseThrow { notFoundRoute() }
        require(route.authorUserId == userId) { forbidden() }
        require(dayIdsInOrder.toSet().size == dayIdsInOrder.size) { "Порядок дней содержит дубликаты" }
        require(dayIdsInOrder.toSet() == route.days.map { it.id }.toSet()) {
            "Список идентификаторов дней должен совпадать с днями маршрута"
        }
        val map = route.days.associateBy { it.id }
        dayIdsInOrder.forEachIndexed { index, id ->
            map[id]!!.dayOrder = index + 1
        }
        route.durationDays = route.days.size
        RouteMapper.touch(route)
        return RouteMapper.toResponse(routeRepository.save(route))
    }

    @Transactional
    fun enableShare(routeId: Long, userId: Long): String {
        val route = routeRepository.findById(routeId).orElseThrow { notFoundRoute() }
        require(route.authorUserId == userId) { forbidden() }
        val token = route.shareToken ?: generateShareToken()
        route.shareToken = token
        RouteMapper.touch(route)
        routeRepository.save(route)
        return token
    }

    @Transactional
    fun disableShare(routeId: Long, userId: Long) {
        val route = routeRepository.findById(routeId).orElseThrow { notFoundRoute() }
        require(route.authorUserId == userId) { forbidden() }
        route.shareToken = null
        RouteMapper.touch(route)
        routeRepository.save(route)
    }

    @Transactional(readOnly = true)
    fun getRouteByShareToken(token: String): RouteResponse {
        val route = routeRepository.findByShareToken(token) ?: throw ResourceNotFoundException("Маршрут не найден")
        return RouteMapper.toResponse(route)
    }

    @Transactional
    fun composeRouteFromDays(authorUserId: Long, authorUsername: String, req: ComposeRouteRequest): RouteResponse {
        validateRouteMeta(req.title, req.description, req.dayIds.size, java.math.BigDecimal.ZERO)
        require(req.dayIds.isNotEmpty()) { "Выберите хотя бы один день" }
        require(req.dayIds.toSet().size == req.dayIds.size) { "Список dayIds содержит дубликаты" }

        val sourceDays = req.dayIds.map { dayId ->
            val day = routeDayRepository.findByIdWithPointsAndRoute(dayId) ?: throw ResourceNotFoundException("День не найден")
            val route = day.route ?: throw ResourceNotFoundException("Маршрут дня не найден")
            val allowed = route.published || route.authorUserId == authorUserId
            require(allowed) { "Недостаточно прав для использования выбранного дня" }
            day
        }

        val route = Route(
            authorUserId = authorUserId,
            authorUsername = authorUsername,
            title = req.title.trim(),
            description = req.description.trim(),
            durationDays = sourceDays.size,
            totalCostRub = sourceDays.fold(java.math.BigDecimal.ZERO) { acc, d -> acc + d.dayCostRub }.stripTrailingZeros(),
            published = req.published,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

        sourceDays.forEachIndexed { idx, src ->
            val clonedDay = RouteDay(
                route = route,
                dayOrder = idx + 1,
                theme = src.theme,
                description = src.description,
                dayCostRub = src.dayCostRub,
                travelTimeMinutes = src.travelTimeMinutes,
            )
            src.points.sortedBy { it.orderIndex }.forEach { sp ->
                val cp = RoutePoint(
                    day = clonedDay,
                    orderIndex = sp.orderIndex,
                    title = sp.title,
                    latitude = sp.latitude,
                    longitude = sp.longitude,
                    description = sp.description,
                    timeStart = sp.timeStart,
                    timeEnd = sp.timeEnd,
                    stayMinutes = sp.stayMinutes,
                    imageUrls = sp.imageUrls.toMutableList(),
                )
                clonedDay.points.add(cp)
            }
            route.days.add(clonedDay)
        }

        val saved = routeRepository.save(route)
        return RouteMapper.toResponse(saved)
    }

    private fun normalizeDayOrders(route: Route) {
        route.days.sortedBy { it.dayOrder }.forEachIndexed { i, d -> d.dayOrder = i + 1 }
    }

    private fun buildDayEntity(route: Route, dayOrder: Int, dr: CreateDayRequest): RouteDay {
        TzConstraints.validateOrderIndex(dayOrder, "Порядок дня")
        val day = RouteDay(
            route = route,
            dayOrder = dayOrder,
            theme = "",
            description = "",
            dayCostRub = dr.dayCostRub.stripTrailingZeros(),
            travelTimeMinutes = 0,
        )
        applyDayContent(day, dr)
        return day
    }

    private fun applyDayContent(day: RouteDay, dr: CreateDayRequest) {
        TzConstraints.validateDayTheme(dr.theme)
        TzConstraints.validateDayDescription(dr.description)
        TzConstraints.validateCostNonNegative(dr.dayCostRub)
        TzConstraints.validateOrderIndex(dr.dayOrder, "Порядок дня")
        require(dr.points.isNotEmpty()) { "В каждом дне должна быть хотя бы одна точка" }

        day.theme = dr.theme.trim()
        day.description = dr.description.trim()
        day.dayCostRub = dr.dayCostRub.stripTrailingZeros()

        day.points.clear()
        val explicitPointOrders = dr.points.mapNotNull { it.orderIndex }
        require(explicitPointOrders.toSet().size == explicitPointOrders.size) {
            "Порядок точек содержит дубликаты"
        }
        val sortedPoints = dr.points.withIndex().sortedBy { it.value.orderIndex ?: (it.index + 1) }
        sortedPoints.forEachIndexed { idx, indexed ->
            val pr = indexed.value
            val p = buildPoint(day, idx + 1, pr)
            day.points.add(p)
        }
        val coords = day.points.sortedBy { it.orderIndex }.map { it.latitude to it.longitude }
        day.travelTimeMinutes = TravelTimeEstimator.totalTravelMinutes(coords)
    }

    private fun buildPoint(day: RouteDay, orderIndex: Int, pr: CreatePointRequest): RoutePoint {
        TzConstraints.validatePointTitle(pr.title)
        TzConstraints.validatePointDescription(pr.description)
        TzConstraints.validateCoordinates(pr.latitude, pr.longitude)
        TzConstraints.validateTimeHm(pr.timeStart)
        TzConstraints.validateTimeHm(pr.timeEnd)
        TzConstraints.validateTimeRange(pr.timeStart, pr.timeEnd)
        TzConstraints.validateStayMinutes(pr.stayMinutes)
        TzConstraints.validateOrderIndex(pr.orderIndex, "Порядок точки")
        TzConstraints.validateImageUrls(pr.imageUrls)
        val p = RoutePoint(
            day = day,
            orderIndex = pr.orderIndex ?: orderIndex,
            title = pr.title.trim(),
            latitude = pr.latitude,
            longitude = pr.longitude,
            description = pr.description?.trim(),
            timeStart = pr.timeStart?.takeIf { it.isNotBlank() },
            timeEnd = pr.timeEnd?.takeIf { it.isNotBlank() },
            stayMinutes = pr.stayMinutes,
            imageUrls = pr.imageUrls.toMutableList(),
        )
        return p
    }

    private fun validateRouteMeta(title: String, description: String, durationDays: Int, totalCostRub: java.math.BigDecimal) {
        TzConstraints.validateRouteTitle(title)
        TzConstraints.validateRouteDescription(description)
        TzConstraints.validateDurationDays(durationDays)
        TzConstraints.validateCostNonNegative(totalCostRub)
    }

    private fun ensureReadable(route: Route, userId: Long?, shareToken: String?) {
        if (route.published) return
        if (userId != null && route.authorUserId == userId) return
        if (!shareToken.isNullOrBlank() && route.shareToken != null && route.shareToken == shareToken) return
        throw ResourceNotFoundException("Маршрут не найден")
    }

    private fun generateShareToken(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun notFoundRoute(): Nothing = throw ResourceNotFoundException("Маршрут не найден")

    private fun forbidden(): Nothing = throw AccessDeniedException("Недостаточно прав для изменения маршрута")
}
