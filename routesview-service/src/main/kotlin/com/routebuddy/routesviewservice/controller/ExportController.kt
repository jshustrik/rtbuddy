package com.routebuddy.routesviewservice.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ExportController {

    @GetMapping("/export")
    fun exportPage(): String = "export-demo"
}
