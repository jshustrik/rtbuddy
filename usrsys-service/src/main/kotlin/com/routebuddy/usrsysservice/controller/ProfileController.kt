package com.routebuddy.usrsysservice.controller

import com.routebuddy.usrsysservice.dto.ProfileDto
import com.routebuddy.usrsysservice.service.ProfileService
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ProfileController(
    private val profileService: ProfileService,
) {
    //private val logger = LoggerFactory.getLogger(ProfileController::class.java)

    @GetMapping("/profile/{username}")
    fun viewProfile(@PathVariable username: String, model: Model): String {
        // Если пользователь не аутентифицирован или пытается посмотреть чужой профиль, перенаправляем
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || auth.name != username) return "redirect:/login"

        // Получаем профиль
        val profile: ProfileDto = profileService.getProfile(username)
        //logger.info("Получен профиль для пользователя: ${profile.username}")


        // Добавляем все в модель
        model.addAttribute("user", profile)

        return "profile"
    }
}
