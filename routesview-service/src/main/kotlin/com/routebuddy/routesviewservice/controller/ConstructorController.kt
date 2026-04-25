package com.routebuddy.routesviewservice.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

// DEMO: Route constructor page — no JWT required, all data is hardcoded/localStorage
@Controller
class ConstructorController {

    @GetMapping("/constructor")
    fun constructorPage(): String = "constructor"
}
