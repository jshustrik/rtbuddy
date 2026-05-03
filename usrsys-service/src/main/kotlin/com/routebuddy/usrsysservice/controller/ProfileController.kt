package com.routebuddy.usrsysservice.controller

import com.routebuddy.usrsysservice.dto.ProfileDto
import com.routebuddy.usrsysservice.service.ProfileService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
class ProfileController(
    private val profileService: ProfileService,
) {
    @GetMapping("/profile")
    fun viewCurrentProfile(): String {
        val auth = SecurityContextHolder.getContext().authentication ?: return "redirect:/login"
        return "redirect:/profile/${auth.name}"
    }

    @GetMapping("/profile/{username}")
    fun viewProfile(@PathVariable username: String, model: Model): String {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || auth.name != username) return "redirect:/login"
        val profile: ProfileDto = profileService.getProfile(username)
        model.addAttribute("user", profile)
        return "profile"
    }

    @PostMapping("/profile/avatar")
    @ResponseBody
    fun updateAvatar(
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Map<String, String>> {
        val auth = SecurityContextHolder.getContext().authentication
            ?: return ResponseEntity.status(401).build()
        val avatarUrl = body["avatarUrl"] ?: return ResponseEntity.badRequest().build()
        profileService.updateAvatar(auth.name, avatarUrl)
        return ResponseEntity.ok(mapOf("avatarUrl" to avatarUrl))
    }
}
