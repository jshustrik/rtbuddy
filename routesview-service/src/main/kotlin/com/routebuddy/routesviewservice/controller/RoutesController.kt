package com.routebuddy.routesviewservice.controller

import com.routebuddy.routesviewservice.security.JwtTokenProvider
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import jakarta.servlet.http.HttpServletRequest

@Controller
class RoutesController(
    private val jwtTokenProvider: JwtTokenProvider
) {

    @GetMapping("/routes")
    fun viewRoutes(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) my: Boolean?,
        request: HttpServletRequest,
        model: Model
    ): Any {
        if (my == true && !hasValidToken(request)) return httpRedirect("/login")
        model.addAttribute("searchQuery", search ?: "")
        model.addAttribute("showMy", my == true)
        val loggedIn = hasValidToken(request)
        model.addAttribute("loggedIn", loggedIn)
        if (loggedIn) {
            extractToken(request)?.let { model.addAttribute("username", jwtTokenProvider.getUsernameFromJWT(it)) }
        }
        return "routes2"
    }

    @GetMapping("/")
    fun home() = httpRedirect("/routes")

    @GetMapping("/routes/create")
    fun viewCreateRoute(request: HttpServletRequest, model: Model): Any {
        if (!hasValidToken(request)) return httpRedirect("/login?redirectUrl=/routes/create")
        model.addAttribute("editMode", false)
        model.addAttribute("editRouteId", null)
        return "create1"
    }

    @GetMapping("/routes/createday")
    fun viewCreateRouteDay(request: HttpServletRequest): Any {
        if (!hasValidToken(request)) return httpRedirect("/login?redirectUrl=/routes/create")
        return httpRedirect("/routes/create")
    }

    @GetMapping("/routes/{id}/edit")
    fun viewEditRoute(@PathVariable id: Long, request: HttpServletRequest, model: Model): Any {
        if (!hasValidToken(request)) return httpRedirect("/login?redirectUrl=/routes/$id/edit")
        model.addAttribute("editMode", true)
        model.addAttribute("editRouteId", id)
        return "create1"
    }

    @GetMapping("/routes/{id}")
    fun viewRoute(@PathVariable id: Long, request: HttpServletRequest, model: Model): String {
        model.addAttribute("routeId", id)
        model.addAttribute("loggedIn", hasValidToken(request))
        return "route-view"
    }

    @GetMapping("/routes/{routeId}/days/{dayId}")
    fun viewRouteDay(
        @PathVariable routeId: Long,
        @PathVariable dayId: Long,
        request: HttpServletRequest,
        model: Model
    ): String {
        model.addAttribute("routeId", routeId)
        model.addAttribute("dayId", dayId)
        model.addAttribute("loggedIn", hasValidToken(request))
        return "day-view"
    }

    private fun extractToken(request: HttpServletRequest): String? {
        request.cookies?.forEach { if (it.name == "JWT") return it.value }
        val bearer = request.getHeader("Authorization") ?: return null
        return if (bearer.startsWith("Bearer ")) bearer.substring(7) else null
    }

    private fun hasValidToken(request: HttpServletRequest): Boolean {
        val token = extractToken(request) ?: return false
        return jwtTokenProvider.validateToken(token)
    }
}
