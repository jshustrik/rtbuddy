package com.routebuddy.usrsysservice.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserDto(
    val id: Long,
    val username: String,
    val email: String?,
    val role: String
)
