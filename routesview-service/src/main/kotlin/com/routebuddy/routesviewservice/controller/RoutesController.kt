package com.routebuddy.routesviewservice.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import com.routebuddy.routesviewservice.security.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest

@Controller
class RoutesController (private val jwtTokenProvider: JwtTokenProvider)  {
    @GetMapping("/routes")
    fun viewRoutes(request: HttpServletRequest, model: Model): String {
        val token = extractToken(request) ?: throw RuntimeException("Invalid token")
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: throw RuntimeException("Invalid token")
        //model.addAttribute("routes", routes)
        //model.addAttribute("searchQuery", searchQuery)
        //model.addAttribute("isAuthenticated", auth != null && auth.isAuthenticated)
        model.addAttribute("username", username)
        return "routes2"
    }

    @GetMapping("/routes/1")
    fun viewRoute1(request: HttpServletRequest, model: Model): String {
        val token = extractToken(request) ?: throw RuntimeException("Invalid token")
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: throw RuntimeException("Invalid token")
        return "example1"
    }

    @GetMapping("/routes/1/day1")
    fun viewRouteDay2(request: HttpServletRequest, model: Model): String {
        val token = extractToken(request) ?: throw RuntimeException("Invalid token")
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: throw RuntimeException("Invalid token")
        return "day1"
    }

    @GetMapping("/routes/2/day1")
    fun viewRouteDay1(request: HttpServletRequest, model: Model): String {
        val token = extractToken(request) ?: throw RuntimeException("Invalid token")
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: throw RuntimeException("Invalid token")
        return "day2"
    }

    @GetMapping("/routes/2")
    fun viewRoute2(request: HttpServletRequest, model: Model): String {
        val token = extractToken(request) ?: throw RuntimeException("Invalid token")
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: throw RuntimeException("Invalid token")
        return "example2"
    }

    @GetMapping("/routes/create")
    fun viewCreateRoute(request: HttpServletRequest, model: Model): String {
        val token = extractToken(request) ?: throw RuntimeException("Invalid token")
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: throw RuntimeException("Invalid token")
        return "create1"
    }

    @GetMapping("/routes/createday")
    fun viewCreateRouteDay(request: HttpServletRequest, model: Model): String {
        val token = extractToken(request) ?: throw RuntimeException("Invalid token")
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: throw RuntimeException("Invalid token")
        return "create2"
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

}