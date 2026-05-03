package com.routebuddy.makeservice.controller

import com.routebuddy.makeservice.service.RouteService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class RouteViewController(private val routeService: RouteService) {

    @GetMapping("/routes")
    fun routeList(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) my: Boolean?,
        authentication: Authentication?,
        model: Model
    ): String {
        val routes = if (my == true && authentication != null) {
            val userId = authentication.credentials as Long
            routeService.getMyRoutes(userId, search)
        } else {
            routeService.getPublicRoutes(search)
        }
        model.addAttribute("routes", routes)
        model.addAttribute("search", search ?: "")
        model.addAttribute("showMy", my == true)
        model.addAttribute("loggedIn", authentication != null)
        return "routes-list"
    }

    @GetMapping("/routes/{id}")
    fun routeDetail(
        @PathVariable id: Long,
        authentication: Authentication?,
        model: Model
    ): String {
        return try {
            val route = routeService.getRoute(id)
            model.addAttribute("route", route)
            model.addAttribute("loggedIn", authentication != null)
            val isAuthor = authentication != null && (authentication.credentials as? Long) == route.authorId
            model.addAttribute("isAuthor", isAuthor)
            "route-detail"
        } catch (e: NoSuchElementException) {
            "redirect:/routes"
        }
    }
}
