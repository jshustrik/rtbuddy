package com.routebuddy.routesviewservice.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

// DEMO: Public route page with reviews — no JWT required
@Controller
class RoutePageController {

    @GetMapping("/route")
    fun routePage(): String = "route-demo"
}
