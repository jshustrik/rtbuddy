package com.routebuddy.routesviewservice.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

// DEMO: Export/preview page — reads route from localStorage, renders Leaflet maps
@Controller
class ExportController {

    @GetMapping("/export")
    fun exportPage(): String = "export-demo"
}
