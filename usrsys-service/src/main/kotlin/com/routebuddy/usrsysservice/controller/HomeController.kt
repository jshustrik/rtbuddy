package com.routebuddy.usrsysservice.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {
    @GetMapping("/")
    fun home(): String = "redirect:http://127.0.0.1:8081/login"
}
