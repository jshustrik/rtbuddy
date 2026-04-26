package com.routebuddy.routesviewservice.controller

import com.routebuddy.routesviewservice.client.ExportServiceClient
import com.routebuddy.routesviewservice.client.MakeServiceClient
import com.routebuddy.routesviewservice.client.ReviewServiceClient
import com.routebuddy.routesviewservice.client.dto.MakeComposeRouteRequest
import com.routebuddy.routesviewservice.client.dto.MakeRouteDayWriteRequest
import com.routebuddy.routesviewservice.client.dto.MakeRoutePointWriteRequest
import com.routebuddy.routesviewservice.client.dto.MakeRouteWriteRequest
import com.routebuddy.routesviewservice.client.dto.ReviewCreateRequest
import com.routebuddy.routesviewservice.security.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.math.BigDecimal

@Controller
class RoutesController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val makeServiceClient: MakeServiceClient,
    private val reviewServiceClient: ReviewServiceClient,
    private val exportServiceClient: ExportServiceClient,
    @Value("\${routebuddy.make-service-url:http://127.0.0.1:8084}") private val makeServiceBaseUrl: String,
) {
    @GetMapping("/")
    fun home(): String = "redirect:/routes"

    @GetMapping("/routes")
    fun viewRoutes(
        request: HttpServletRequest,
        @RequestParam(required = false, name = "search") searchQuery: String?,
        model: Model,
    ): String {
        val token = extractToken(request)
        val username = token?.let { jwtTokenProvider.getUsernameFromJWT(it) }

        val page = makeServiceClient.listPublishedRoutes(searchQuery, page = 0, size = 50)
        val routes = page.content.map { s ->
            val rating = runCatching { reviewServiceClient.getRouteRating(s.id) }.getOrNull()
            RouteCardVm(
                id = s.id,
                title = s.title,
                description = s.description,
                authorUsername = s.authorUsername,
                durationDays = s.durationDays,
                totalCostRub = s.totalCostRub.toPlainString(),
                averageRating = rating?.averageRating,
                reviewsCount = rating?.reviewsCount,
            )
        }

        model.addAttribute("routes", routes)
        model.addAttribute("searchQuery", searchQuery ?: "")
        model.addAttribute("isAuthenticated", token != null && username != null)
        model.addAttribute("username", username ?: "")
        return "routes2"
    }

    @GetMapping("/routes/{routeId}")
    fun viewRoute(
        request: HttpServletRequest,
        @PathVariable routeId: Long,
        @RequestParam(required = false, name = "shareToken") shareToken: String?,
        model: Model,
    ): String {
        val token = extractToken(request)
        val username = token?.let { jwtTokenProvider.getUsernameFromJWT(it) }
        val userId = token?.let { runCatching { jwtTokenProvider.getUserIdFromJWT(it) }.getOrNull() }

        val route = makeServiceClient.getRoute(routeId, bearerToken = token, shareToken = shareToken)
        val rating = runCatching { reviewServiceClient.getRouteRating(routeId) }.getOrNull()
        val reviewsPage = runCatching { reviewServiceClient.listReviews(routeId, page = 0, size = 50) }.getOrNull()

        val reviews = reviewsPage?.content ?: emptyList()
        val userReview = userId?.let { uid -> reviews.firstOrNull { it.authorId == uid } }

        model.addAttribute(
            "route",
            RouteVm(
                id = route.id,
                title = route.title,
                description = route.description,
                shortDescription = route.description,
                authorUsername = route.authorUsername,
                createdAt = route.createdAt,
                durationDays = route.durationDays,
                totalCost = route.totalCostRub,
                rating = rating?.averageRating ?: 0.0,
                reviewsCount = rating?.reviewsCount ?: 0,
                city = "",
                difficulty = "",
                views = 0,
                distance = 0.0,
                days = route.days.sortedBy { it.dayOrder }.map { d ->
                    DayVm(
                        dayNumber = d.dayOrder,
                        title = d.theme,
                        description = d.description,
                        cost = d.dayCostRub,
                        points = d.points.sortedBy { it.orderIndex }.map { p ->
                            PointVm(
                                id = p.id,
                                title = p.title,
                                description = p.description ?: "",
                                latitude = p.latitude,
                                longitude = p.longitude,
                                startTime = p.timeStart,
                                endTime = p.timeEnd,
                                imageUrl = p.imageUrls.firstOrNull(),
                            )
                        }
                    )
                }
            )
        )
        model.addAttribute("reviews", reviews)
        model.addAttribute("userReview", userReview)
        model.addAttribute("isAuthenticated", token != null && username != null)
        model.addAttribute("username", username ?: "")
        return "route"
    }

    @PostMapping("/routes/{routeId}/reviews")
    fun addReview(
        request: HttpServletRequest,
        @PathVariable routeId: Long,
        @RequestParam rating: Int,
        @RequestParam(name = "comment") comment: String,
    ): String {
        val token = extractToken(request) ?: return "redirect:/routes/$routeId"
        val userId = jwtTokenProvider.getUserIdFromJWT(token)
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: "anonymous"
        runCatching {
            reviewServiceClient.createReview(
                userId = userId,
                username = username,
                request = ReviewCreateRequest(routeId = routeId, text = comment, rating = rating),
            )
        }
        return "redirect:/routes/$routeId"
    }

    @GetMapping("/routes/{routeId}/days/{dayOrder}")
    fun viewRouteDay(
        request: HttpServletRequest,
        @PathVariable routeId: Long,
        @PathVariable dayOrder: Int,
        model: Model,
    ): String {
        val token = extractToken(request)
        val username = token?.let { jwtTokenProvider.getUsernameFromJWT(it) }

        val route = makeServiceClient.getRoute(routeId, bearerToken = token)
        val day = route.days.firstOrNull { it.dayOrder == dayOrder } ?: return "redirect:/routes/$routeId"

        model.addAttribute("routeId", routeId)
        model.addAttribute("username", username ?: "")
        model.addAttribute("isEditable", false)
        model.addAttribute("totalDays", route.days.size)
        model.addAttribute(
            "day",
            DayPageVm(
                dayNumber = day.dayOrder,
                title = day.theme,
                shortDescription = day.description,
                description = day.description,
                totalCost = day.dayCostRub,
                totalDuration = minutesToHhMm(day.travelTimeMinutes),
                points = day.points.sortedBy { it.orderIndex }.map { p ->
                    DayPointVm(
                        id = p.id,
                        title = p.title,
                        description = p.description ?: "",
                        latitude = p.latitude,
                        longitude = p.longitude,
                        startTime = p.timeStart,
                        endTime = p.timeEnd,
                        imageUrl = p.imageUrls.firstOrNull(),
                    )
                },
            )
        )

        return "daytemplate"
    }

    @GetMapping("/routes/create")
    fun viewCreateRoute(request: HttpServletRequest, model: Model): String {
        val token = extractToken(request) ?: throw RuntimeException("Invalid token")
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: throw RuntimeException("Invalid token")
        model.addAttribute("username", username)
        model.addAttribute("jwtToken", token)
        model.addAttribute("makeServiceBaseUrl", makeServiceBaseUrl)
        return "create1"
    }

    @GetMapping("/routes/createday")
    fun viewCreateRouteDay(
        request: HttpServletRequest,
        @RequestParam(required = false) routeId: Long?,
        model: Model,
    ): String {
        val token = extractToken(request) ?: throw RuntimeException("Invalid token")
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: throw RuntimeException("Invalid token")
        model.addAttribute("username", username)
        model.addAttribute("jwtToken", token)
        model.addAttribute("makeServiceBaseUrl", makeServiceBaseUrl)
        model.addAttribute("routeId", routeId)
        return "create2"
    }

    @GetMapping("/routes/compose")
    fun viewCompose(request: HttpServletRequest, model: Model): String {
        val token = requireToken(request)
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: "user"
        val availableRoutes = makeServiceClient.listPublishedRoutes(query = null, page = 0, size = 50).content
        model.addAttribute("availableRoutes", availableRoutes)
        model.addAttribute("username", username)
        return "compose"
    }

    @PostMapping("/routes/compose")
    fun composeRoute(
        request: HttpServletRequest,
        @RequestParam title: String,
        @RequestParam description: String,
        @RequestParam(name = "dayIds", required = false) dayIds: List<Long>?,
    ): String {
        val token = requireToken(request)
        val ids = dayIds ?: emptyList()
        val result = makeServiceClient.composeRoute(
            token,
            MakeComposeRouteRequest(
                title = title.trim(),
                description = description.trim(),
                published = true,
                dayIds = ids,
            ),
        )
        return "redirect:/routes/${result.id}"
    }

    @GetMapping("/routes/export")
    fun viewExportPage(request: HttpServletRequest, model: Model): String {
        val token = requireToken(request)
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: "user"
        val routes = exportServiceClient.listMyRoutes(token)
        model.addAttribute("username", username)
        model.addAttribute("routes", routes)
        return "export-routes"
    }

    @GetMapping("/routes/export/route/{routeId}")
    fun exportRoute(
        request: HttpServletRequest,
        @PathVariable routeId: Long,
    ): ResponseEntity<ByteArray> {
        val token = requireToken(request)
        val bytes = exportServiceClient.exportRoutePdf(routeId, token)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("route_${routeId}.pdf").build().toString())
            .body(bytes)
    }

    @GetMapping("/routes/export/day/{dayId}")
    fun exportDay(
        request: HttpServletRequest,
        @PathVariable dayId: Long,
    ): ResponseEntity<ByteArray> {
        val token = requireToken(request)
        val bytes = exportServiceClient.exportDayPdf(dayId, token)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("day_${dayId}.pdf").build().toString())
            .body(bytes)
    }

    @GetMapping("/routes/{routeId}/edit")
    fun editRoutePage(
        request: HttpServletRequest,
        @PathVariable routeId: Long,
        model: Model,
    ): String {
        val token = requireToken(request)
        val route = makeServiceClient.getRoute(routeId, bearerToken = token)
        model.addAttribute("route", route)
        return "edit-route"
    }

    @PostMapping("/routes/{routeId}/edit")
    fun editRouteSubmit(
        request: HttpServletRequest,
        @PathVariable routeId: Long,
        @RequestParam title: String,
        @RequestParam description: String,
        @RequestParam totalCostRub: BigDecimal,
        @RequestParam(defaultValue = "true") published: Boolean,
    ): String {
        val token = requireToken(request)
        val current = makeServiceClient.getRoute(routeId, bearerToken = token)
        makeServiceClient.updateRoute(
            routeId = routeId,
            bearerToken = token,
            body = MakeRouteWriteRequest(
                title = title.trim(),
                description = description.trim(),
                durationDays = current.days.size,
                totalCostRub = totalCostRub,
                published = published,
            ),
        )
        return "redirect:/routes/$routeId"
    }

    @GetMapping("/routes/{routeId}/days/{dayOrder}/edit")
    fun editDayPage(
        request: HttpServletRequest,
        @PathVariable routeId: Long,
        @PathVariable dayOrder: Int,
        model: Model,
    ): String {
        val token = requireToken(request)
        val route = makeServiceClient.getRoute(routeId, bearerToken = token)
        val day = route.days.firstOrNull { it.dayOrder == dayOrder } ?: return "redirect:/routes/$routeId"
        model.addAttribute("route", route)
        model.addAttribute("day", day)
        return "edit-day"
    }

    @PostMapping("/routes/{routeId}/days/{dayOrder}/edit")
    fun editDaySubmit(
        request: HttpServletRequest,
        @PathVariable routeId: Long,
        @PathVariable dayOrder: Int,
        @RequestParam theme: String,
        @RequestParam description: String,
        @RequestParam dayCostRub: BigDecimal,
    ): String {
        val token = requireToken(request)
        val route = makeServiceClient.getRoute(routeId, bearerToken = token)
        val day = route.days.firstOrNull { it.dayOrder == dayOrder } ?: return "redirect:/routes/$routeId"
        makeServiceClient.updateDay(
            routeId = routeId,
            dayId = day.id,
            bearerToken = token,
            body = MakeRouteDayWriteRequest(
                theme = theme.trim(),
                description = description.trim(),
                dayCostRub = dayCostRub,
                dayOrder = day.dayOrder,
                points = day.points.sortedBy { it.orderIndex }.map { p ->
                    MakeRoutePointWriteRequest(
                        title = p.title,
                        latitude = p.latitude,
                        longitude = p.longitude,
                        description = p.description,
                        timeStart = p.timeStart,
                        timeEnd = p.timeEnd,
                        stayMinutes = p.stayMinutes,
                        orderIndex = p.orderIndex,
                        imageUrls = p.imageUrls,
                    )
                },
            ),
        )
        return "redirect:/routes/$routeId/days/$dayOrder"
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (!bearerToken.isNullOrEmpty() && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        request.cookies?.forEach {
            if (it.name == "JWT") return it.value
        }
        return null
    }

    private fun requireToken(request: HttpServletRequest): String =
        extractToken(request) ?: throw IllegalStateException("Authentication token required")

    private fun minutesToHhMm(minutes: Int): String {
        if (minutes <= 0) return "0:00"
        val h = minutes / 60
        val m = minutes % 60
        return "%d:%02d".format(h, m)
    }

}

data class RouteCardVm(
    val id: Long,
    val title: String,
    val description: String,
    val authorUsername: String,
    val durationDays: Int,
    val totalCostRub: String,
    val averageRating: Double?,
    val reviewsCount: Long?,
)

data class RouteVm(
    val id: Long,
    val title: String,
    val shortDescription: String,
    val description: String,
    val authorUsername: String,
    val createdAt: java.time.Instant,
    val durationDays: Int,
    val totalCost: java.math.BigDecimal,
    val rating: Double,
    val reviewsCount: Long,
    val city: String,
    val difficulty: String,
    val views: Int,
    val distance: Double,
    val days: List<DayVm>,
)

data class DayVm(
    val dayNumber: Int,
    val title: String,
    val description: String,
    val cost: java.math.BigDecimal,
    val points: List<PointVm>,
)

data class PointVm(
    val id: Long,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val startTime: String?,
    val endTime: String?,
    val imageUrl: String?,
    val type: String = "OTHER",
    val timeSpent: String? = null,
)

data class DayPageVm(
    val dayNumber: Int,
    val title: String,
    val shortDescription: String?,
    val description: String?,
    val totalCost: java.math.BigDecimal,
    val totalDuration: String,
    val points: List<DayPointVm>,
    val totalDistance: Double? = null,
    val mode: String? = null,
    val foodCost: java.math.BigDecimal = java.math.BigDecimal.ZERO,
)

data class DayPointVm(
    val id: Long,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val startTime: String?,
    val endTime: String?,
    val duration: String? = null,
    val imageUrl: String?,
    val cost: java.math.BigDecimal? = null,
    val icon: String? = "map-marker-alt",
)