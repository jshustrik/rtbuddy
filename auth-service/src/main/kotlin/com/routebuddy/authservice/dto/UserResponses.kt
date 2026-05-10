package com.routebuddy.authservice.dto

import com.routebuddy.authservice.model.User

data class PublicUserResponse(
    val id: Long,
    val username: String,
    val avatarUrl: String?
) {
    companion object {
        fun from(user: User) = PublicUserResponse(
            id = user.id,
            username = user.username,
            avatarUrl = user.avatarUrl
        )
    }
}

data class InternalUserResponse(
    val id: Long,
    val username: String,
    val email: String?,
    val role: String,
    val avatarUrl: String?
) {
    companion object {
        fun from(user: User) = InternalUserResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            role = user.role,
            avatarUrl = user.avatarUrl
        )
    }
}
