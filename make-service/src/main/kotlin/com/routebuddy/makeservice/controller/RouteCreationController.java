package com.routebuddy.makeservice.controller;

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
@RequestMapping("/api/routes")
class RouteCreationController(
        private val routeCreationService: RouteCreationService,
        private val jwtTokenProvider: JwtTokenProvider // для извлечения username из токена
) {

    @PostMapping
    fun createRoute(
            @RequestBody request: CreateRouteRequest,
            @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<RouteResponse> {
        // Извлекаем username из токена
        val token = authHeader.removePrefix("Bearer ")
        val username = jwtTokenProvider.getUsernameFromJWT(token)
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val authorId = username

        val createdRoute = routeCreationService.createFullRoute(request, authorId, username)

        val response = RouteResponse(
                id = createdRoute.id,
                title = createdRoute.title,
                description = createdRoute.description,
                shortDescription = createdRoute.shortDescription,
                authorId = createdRoute.authorId,
                authorUsername = createdRoute.authorUsername,
                createdAt = createdRoute.createdAt.toString()
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // Другие эндпоинты: добавление дня, точки
    @PostMapping("/{routeId}/days")
    fun addDay(
            @PathVariable routeId: String,
            @RequestBody request: CreateDayRequest,
            @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<RouteDay> {
        val token = authHeader.removePrefix("Bearer ")
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val authorId = username

        val day = routeCreationService.addDayToRoute(routeId, request, authorId)
        return ResponseEntity.ok(day)
    }

    @PostMapping("/days/{dayId}/points")
    fun addPoint(
            @PathVariable dayId: String,
            @RequestBody request: CreatePointRequest,
            @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<RoutePoint> {
        val token = authHeader.removePrefix("Bearer ")
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val authorId = username

        val point = routeCreationService.addPointToDay(dayId, request, authorId)
        return ResponseEntity.ok(point)
    }
}