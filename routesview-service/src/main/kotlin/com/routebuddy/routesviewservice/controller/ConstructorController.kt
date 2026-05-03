package com.routebuddy.routesviewservice.controller

import jakarta.servlet.http.HttpServletRequest
import com.routebuddy.routesviewservice.security.JwtTokenProvider
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ConstructorController(
    private val jwtTokenProvider: JwtTokenProvider
) {

    @GetMapping("/constructor")
    fun constructorPage(request: HttpServletRequest): Any {
        val token = request.cookies?.firstOrNull { it.name == "JWT" && it.value.isNotBlank() }?.value
        return if (token != null && jwtTokenProvider.validateToken(token)) {
            "constructor"
        } else {
            httpRedirect("/login?redirectUrl=/constructor")
        }
    }
}
