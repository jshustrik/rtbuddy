package com.routebuddy.routesviewservice.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

// Redirect old demo URL to real routes catalog
@Controller
class RoutePageController {

    @GetMapping("/route")
    fun routePage() = httpRedirect("/routes")
}
