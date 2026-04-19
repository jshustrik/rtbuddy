package com.routebuddy.makeservice.controller

import com.routebuddy.makeservice.dto.CreateDayRequest
import com.routebuddy.makeservice.dto.CreateRouteRequest
import com.routebuddy.makeservice.dto.ComposeRouteRequest
import com.routebuddy.makeservice.dto.PageResponse
import com.routebuddy.makeservice.dto.ReorderDaysRequest
import com.routebuddy.makeservice.dto.RouteResponse
import com.routebuddy.makeservice.dto.RouteSummaryResponse
import com.routebuddy.makeservice.dto.ShareLinkResponse
import com.routebuddy.makeservice.dto.UpdateRouteRequest
import com.routebuddy.makeservice.security.PrincipalInfo
import com.routebuddy.makeservice.service.RouteService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/routes")
class RouteController(
    private val routeService: RouteService,
) {

    @PostMapping
    fun createRoute(
        @AuthenticationPrincipal principal: PrincipalInfo,
        @RequestBody body: CreateRouteRequest,
    ): ResponseEntity<RouteResponse> {
        val route = routeService.createRoute(principal.userId, principal.username, body)
        return ResponseEntity.status(HttpStatus.CREATED).body(route)
    }

    @GetMapping("/my")
    fun listMyRoutes(
        @AuthenticationPrincipal principal: PrincipalInfo,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<RouteSummaryResponse> =
        routeService.listMine(principal.userId, page, size)

    @GetMapping("/{id}")
    fun getRoute(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: PrincipalInfo?,
        @RequestParam(required = false, name = "shareToken") shareToken: String?,
    ): RouteResponse =
        routeService.getRoute(id, principal?.userId, shareToken)

    @GetMapping
    fun listRoutes(
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<RouteSummaryResponse> =
        routeService.listPublished(q, page, size)

    @PutMapping("/{id}")
    fun updateRoute(
        @AuthenticationPrincipal principal: PrincipalInfo,
        @PathVariable id: Long,
        @RequestBody body: UpdateRouteRequest,
    ): RouteResponse =
        routeService.updateRoute(id, principal.userId, body)

    @DeleteMapping("/{id}")
    fun deleteRoute(
        @AuthenticationPrincipal principal: PrincipalInfo,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        routeService.deleteRoute(id, principal.userId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{routeId}/days")
    fun addDay(
        @AuthenticationPrincipal principal: PrincipalInfo,
        @PathVariable routeId: Long,
        @RequestBody body: CreateDayRequest,
    ): RouteResponse =
        routeService.addDay(routeId, principal.userId, body)

    @PutMapping("/{routeId}/days/{dayId}")
    fun updateDay(
        @AuthenticationPrincipal principal: PrincipalInfo,
        @PathVariable routeId: Long,
        @PathVariable dayId: Long,
        @RequestBody body: CreateDayRequest,
    ): RouteResponse =
        routeService.updateDay(routeId, dayId, principal.userId, body)

    @DeleteMapping("/{routeId}/days/{dayId}")
    fun deleteDay(
        @AuthenticationPrincipal principal: PrincipalInfo,
        @PathVariable routeId: Long,
        @PathVariable dayId: Long,
    ): RouteResponse =
        routeService.deleteDay(routeId, dayId, principal.userId)

    @PutMapping("/{routeId}/days/order")
    fun reorderDays(
        @AuthenticationPrincipal principal: PrincipalInfo,
        @PathVariable routeId: Long,
        @RequestBody body: ReorderDaysRequest,
    ): RouteResponse =
        routeService.reorderDays(routeId, principal.userId, body.dayIdsInOrder)

    @PostMapping("/{id}/share")
    fun enableShare(
        @AuthenticationPrincipal principal: PrincipalInfo,
        @PathVariable id: Long,
    ): ShareLinkResponse {
        val token = routeService.enableShare(id, principal.userId)
        return ShareLinkResponse(routeId = id, shareToken = token)
    }

    @DeleteMapping("/{id}/share")
    fun disableShare(
        @AuthenticationPrincipal principal: PrincipalInfo,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        routeService.disableShare(id, principal.userId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/share/{token}")
    fun getShared(@PathVariable token: String): RouteResponse =
        routeService.getRouteByShareToken(token)

    @PostMapping("/compose")
    fun compose(
        @AuthenticationPrincipal principal: PrincipalInfo,
        @RequestBody body: ComposeRouteRequest,
    ): ResponseEntity<RouteResponse> {
        val route = routeService.composeRouteFromDays(principal.userId, principal.username, body)
        return ResponseEntity.status(HttpStatus.CREATED).body(route)
    }
}
