package com.routebuddy.usrsysservice.service

import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap

@Service
class AvatarStorageService {
    private val avatarDir: Path = Path.of(System.getProperty("java.io.tmpdir"), "routebuddy-avatars")
    private val byUsername: MutableMap<String, String> = ConcurrentHashMap()

    init {
        Files.createDirectories(avatarDir)
    }

    fun getAvatarUrl(username: String): String? =
        byUsername[username]?.let { "/profile/avatar/$username" }

    fun store(username: String, avatar: MultipartFile): String {
        require(!avatar.isEmpty) { "Avatar file is empty" }
        val contentType = avatar.contentType?.lowercase()
        require(contentType == "image/jpeg" || contentType == "image/jpg" || contentType == "image/png") {
            "Unsupported avatar content-type: $contentType"
        }

        val ext = when (contentType) {
            "image/png" -> "png"
            else -> "jpg"
        }
        val target = avatarDir.resolve("${username}.${ext}")
        avatar.inputStream.use { input ->
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
        }
        byUsername[username] = target.toString()
        return "/profile/avatar/$username"
    }

    fun load(username: String): Resource? {
        val path = byUsername[username]?.let { Path.of(it) } ?: return null
        if (!Files.exists(path)) return null
        return FileSystemResource(path)
    }
}

