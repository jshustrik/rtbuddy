package com.routebuddy.exportservice.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ExportPageController {
    @GetMapping("/", "/export")
    fun exportPage(): String = "export"
}
