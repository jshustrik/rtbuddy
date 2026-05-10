package com.routebuddy.usrsysservice.client

import com.routebuddy.usrsysservice.dto.UserDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "auth-service", url = "\${auth.service.url}")
interface AuthServiceClient {
    @GetMapping("/internal/users/by-username/{username}")
    fun getUserByUsername(
        @RequestHeader("X-Internal-Token") internalToken: String,
        @PathVariable username: String
    ): UserDto
}
