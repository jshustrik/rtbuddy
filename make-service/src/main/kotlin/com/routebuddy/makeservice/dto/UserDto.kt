package com.routebuddy.makeservice.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserDto(
    val id: Long,
    val username: String,
    val email: String?,
    val role: String
)
