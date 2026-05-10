package com.routebuddy.usrsysservice.controller

import com.routebuddy.usrsysservice.dto.ProfileDto
import com.routebuddy.usrsysservice.service.ProfileService
import feign.FeignException
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AnonymousAuthenticationToken
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
        val username = currentUsername() ?: return "redirect:/login?redirectUrl=/profile"
        return "redirect:/profile/$username"
    }

    @GetMapping("/profile/{username}")
    fun viewProfile(@PathVariable username: String, model: Model, response: HttpServletResponse): String {
        val currentUsername = currentUsername() ?: return "redirect:/login?redirectUrl=/profile/$username"
        if (currentUsername != username) {
            if (!profileService.profileExists(username)) return notFound(username, model, response)
            return "redirect:/profile/$currentUsername"
        }
        val profile: ProfileDto = try {
            profileService.getProfile(username)
        } catch (e: FeignException.NotFound) {
            return notFound(username, model, response)
        }
        model.addAttribute("user", profile)
        return "profile"
    }

    private fun notFound(username: String, model: Model, response: HttpServletResponse): String {
        model.addAttribute("username", username)
        response.status = HttpServletResponse.SC_NOT_FOUND
        return "profile-not-found"
    }

    @PostMapping("/profile/avatar")
    @ResponseBody
    fun updateAvatar(
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Map<String, String>> {
        val username = currentUsername() ?: return ResponseEntity.status(401).build()
        val avatarUrl = body["avatarUrl"]?.trim() ?: return ResponseEntity.badRequest()
            .body(mapOf("error" to "Выберите JPG или PNG до 5 МБ"))
        if (!isValidAvatar(avatarUrl)) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Выберите JPG или PNG до 5 МБ"))
        }
        profileService.updateAvatar(username, avatarUrl)
        return ResponseEntity.ok(mapOf("avatarUrl" to avatarUrl))
    }

    @DeleteMapping("/profile/avatar")
    @ResponseBody
    fun deleteAvatar(): ResponseEntity<Map<String, String>> {
        val username = currentUsername() ?: return ResponseEntity.status(401).build()
        profileService.deleteAvatar(username)
        return ResponseEntity.ok(mapOf("message" to "Аватар удалён"))
    }

    private fun isValidAvatar(value: String): Boolean {
        if (value.length > 7_000_000) return false
        val dataImage = Regex("^data:image/(jpeg|png);base64,[A-Za-z0-9+/=\\r\\n]+$", RegexOption.IGNORE_CASE)
        return dataImage.matches(value)
    }

    private fun currentUsername(): String? {
        val auth = SecurityContextHolder.getContext().authentication ?: return null
        if (auth is AnonymousAuthenticationToken || !auth.isAuthenticated || auth.name == "anonymousUser") {
            return null
        }
        return auth.name
    }
}
