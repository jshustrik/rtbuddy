package com.routebuddy.usrsysservice.service

import com.routebuddy.usrsysservice.client.AuthServiceClient
import com.routebuddy.usrsysservice.dto.ProfileDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ProfileService(
    private val authServiceClient: AuthServiceClient,
    @Value("\${auth.service.url}") private val authServiceUrl: String
) {
    private val restTemplate = RestTemplate()

    fun getProfile(username: String): ProfileDto {
        val user = authServiceClient.getUserByUsername(username)
        return ProfileDto(
            id = user.id,
            username = user.username,
            email = user.email,
            role = user.role,
            avatarUrl = user.avatarUrl
        )
    }

    fun updateAvatar(username: String, avatarUrl: String) {
        restTemplate.put(
            "$authServiceUrl/internal/users/by-username/$username/avatar",
            mapOf("avatarUrl" to avatarUrl)
        )
    }
}
