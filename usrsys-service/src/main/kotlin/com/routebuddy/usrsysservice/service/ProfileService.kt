package com.routebuddy.usrsysservice.service

import com.routebuddy.usrsysservice.client.AuthServiceClient
import com.routebuddy.usrsysservice.dto.ProfileDto
import org.springframework.stereotype.Service

@Service
class ProfileService(private val authServiceClient: AuthServiceClient) {
    fun getProfile(username: String): ProfileDto {
        val user = authServiceClient.getUserByUsername(username)
        return ProfileDto(
            id = user.id,
            username = user.username,
            email = user.email,
            role = user.role
        )
    }
}
