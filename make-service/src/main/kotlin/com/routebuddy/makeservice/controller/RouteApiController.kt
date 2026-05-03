package com.routebuddy.makeservice.controller

import com.routebuddy.makeservice.dto.*
import com.routebuddy.makeservice.service.RouteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/routes")
class RouteApiController(private val routeService: RouteService) {

    // ── List / search ────────────────────────────────────────────────────────

    @GetMapping
    fun listRoutes(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) my: Boolean?,
        authentication: Authentication?
    ): ResponseEntity<List<RouteDto>> {
        return if (my == true && authentication != null) {
            val userId = authentication.credentials as Long
            ResponseEntity.ok(routeService.getMyRoutes(userId, search))
        } else {
            ResponseEntity.ok(routeService.getPublicRoutes(search))
        }
    }

    @GetMapping("/my")
    fun myRoutes(
        @RequestParam(required = false) search: String?,
        authentication: Authentication
    ): ResponseEntity<List<RouteDto>> {
        val userId = authentication.credentials as Long
        return ResponseEntity.ok(routeService.getMyRoutes(userId, search))
    }

    // ── Single route ──────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    fun getRoute(@PathVariable id: Long): ResponseEntity<RouteDto> {
        return try {
            ResponseEntity.ok(routeService.getRoute(id))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    // ── Create / update / delete route ───────────────────────────────────────

    @PostMapping
    fun createRoute(
        @Valid @RequestBody req: CreateRouteRequest,
        authentication: Authentication
    ): ResponseEntity<RouteDto> {
        val userId = authentication.credentials as Long
        val username = authentication.name
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(routeService.createRoute(req, userId, username))
    }

    @PutMapping("/{id}")
    fun updateRoute(
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateRouteRequest,
        authentication: Authentication
    ): ResponseEntity<RouteDto> {
        return try {
            val userId = authentication.credentials as Long
            ResponseEntity.ok(routeService.updateRoute(id, req, userId))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteRoute(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        return try {
            val userId = authentication.credentials as Long
            routeService.deleteRoute(id, userId)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    // ── Days ──────────────────────────────────────────────────────────────────

    @PostMapping("/{routeId}/days")
    fun addDay(
        @PathVariable routeId: Long,
        @Valid @RequestBody req: CreateDayRequest,
        authentication: Authentication
    ): ResponseEntity<DayDto> {
        return try {
            val userId = authentication.credentials as Long
            ResponseEntity.status(HttpStatus.CREATED).body(routeService.addDay(routeId, req, userId))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @PutMapping("/{routeId}/days/{dayId}")
    fun updateDay(
        @PathVariable routeId: Long,
        @PathVariable dayId: Long,
        @Valid @RequestBody req: UpdateDayRequest,
        authentication: Authentication
    ): ResponseEntity<DayDto> {
        return try {
            val userId = authentication.credentials as Long
            ResponseEntity.ok(routeService.updateDay(routeId, dayId, req, userId))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @DeleteMapping("/{routeId}/days/{dayId}")
    fun deleteDay(
        @PathVariable routeId: Long,
        @PathVariable dayId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        return try {
            val userId = authentication.credentials as Long
            routeService.deleteDay(routeId, dayId, userId)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    // ── Points ────────────────────────────────────────────────────────────────

    @PostMapping("/{routeId}/days/{dayId}/points")
    fun addPoint(
        @PathVariable routeId: Long,
        @PathVariable dayId: Long,
        @Valid @RequestBody req: CreatePointRequest,
        authentication: Authentication
    ): ResponseEntity<RoutePointDto> {
        return try {
            val userId = authentication.credentials as Long
            ResponseEntity.status(HttpStatus.CREATED).body(routeService.addPoint(routeId, dayId, req, userId))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @PutMapping("/{routeId}/days/{dayId}/points/{pointId}")
    fun updatePoint(
        @PathVariable routeId: Long,
        @PathVariable dayId: Long,
        @PathVariable pointId: Long,
        @Valid @RequestBody req: UpdatePointRequest,
        authentication: Authentication
    ): ResponseEntity<RoutePointDto> {
        return try {
            val userId = authentication.credentials as Long
            ResponseEntity.ok(routeService.updatePoint(routeId, dayId, pointId, req, userId))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @DeleteMapping("/{routeId}/days/{dayId}/points/{pointId}")
    fun deletePoint(
        @PathVariable routeId: Long,
        @PathVariable dayId: Long,
        @PathVariable pointId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        return try {
            val userId = authentication.credentials as Long
            routeService.deletePoint(routeId, dayId, pointId, userId)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }
}
