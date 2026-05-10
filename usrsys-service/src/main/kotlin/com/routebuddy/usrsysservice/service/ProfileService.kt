package com.routebuddy.usrsysservice.service

import com.routebuddy.usrsysservice.client.AuthServiceClient
import com.routebuddy.usrsysservice.dto.ProfileDto
import feign.FeignException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ProfileService(
    private val authServiceClient: AuthServiceClient,
    @Value("\${auth.service.url}") private val authServiceUrl: String,
    @Value("\${service.internal-token:}") private val internalToken: String
) {
    private val restTemplate = RestTemplate()

    fun getProfile(username: String): ProfileDto {
        val user = authServiceClient.getUserByUsername(internalToken, username)
        return ProfileDto(
            id = user.id,
            username = user.username,
            email = user.email,
            role = user.role,
            avatarUrl = user.avatarUrl
        )
    }

    fun profileExists(username: String): Boolean =
        try {
            authServiceClient.getUserByUsername(internalToken, username)
            true
        } catch (e: FeignException.NotFound) {
            false
        }

    fun updateAvatar(username: String, avatarUrl: String) {
        val headers = HttpHeaders().apply {
            set("X-Internal-Token", internalToken)
        }
        restTemplate.exchange(
            "$authServiceUrl/internal/users/by-username/$username/avatar",
            HttpMethod.PUT,
            HttpEntity(mapOf("avatarUrl" to avatarUrl), headers),
            Void::class.java
        )
    }

    fun deleteAvatar(username: String) {
        val headers = HttpHeaders().apply {
            set("X-Internal-Token", internalToken)
        }
        restTemplate.exchange(
            "$authServiceUrl/internal/users/by-username/$username/avatar",
            HttpMethod.DELETE,
            HttpEntity<Void>(headers),
            Void::class.java
        )
    }
}
