package com.routebuddy.usrsysservice.controller

import com.routebuddy.usrsysservice.dto.ProfileDto
import com.routebuddy.usrsysservice.service.AvatarStorageService
import com.routebuddy.usrsysservice.service.ProfileService
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@Controller
class ProfileController(
    private val profileService: ProfileService,
    private val avatarStorageService: AvatarStorageService,
    @Value("\${auth.service.url}")
    private val authServiceUrl: String
) {
    @GetMapping("/profile")
    fun myProfile(): String {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || auth.name.isNullOrBlank()) return loginRedirect()
        return "redirect:/profile/${auth.name}"
    }

    @GetMapping("/profile/{username}")
    fun viewProfile(@PathVariable username: String, model: Model): String {
        // Если пользователь не аутентифицирован или пытается посмотреть чужой профиль, перенаправляем
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || auth.name != username) return loginRedirect(username)

        // Получаем профиль
        val profile: ProfileDto = profileService.getProfile(username)

        // Добавляем все в модель
        model.addAttribute("user", profile)

        return "profile"
    }

    @PostMapping("/profile/avatar")
    @ResponseBody
    fun uploadAvatar(
        @RequestParam("avatar", required = false) avatar: MultipartFile?,
        @RequestParam("avatarUrl", required = false) avatarUrl: String?
    ): ResponseEntity<Void> {
        val auth = SecurityContextHolder.getContext().authentication
        val username = auth?.name?.takeIf { it.isNotBlank() } ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        if (avatar != null && !avatar.isEmpty) {
            avatarStorageService.store(username, avatar)
            return ResponseEntity.ok().build()
        }

        // Пока поддерживаем только загрузку файла (требование ТЗ: загрузка с устройства).
        if (!avatarUrl.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "avatarUrl is not supported yet; upload a file")
        }

        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "avatar is required")
    }

    @GetMapping("/profile/avatar/{username}")
    fun getAvatar(@PathVariable username: String): ResponseEntity<Resource> {
        val resource = avatarStorageService.load(username) ?: return ResponseEntity.notFound().build()
        val contentType = runCatching {
            resource.file?.let { java.nio.file.Files.probeContentType(it.toPath()) }
        }.getOrNull() ?: MediaType.IMAGE_JPEG_VALUE

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource)
    }

    private fun loginRedirect(username: String? = null): String {
        val target = if (username.isNullOrBlank()) "/profile" else "/profile/$username"
        return "redirect:${authServiceUrl.trimEnd('/')}/login?redirectUrl=http://127.0.0.1:8082$target"
    }
}
