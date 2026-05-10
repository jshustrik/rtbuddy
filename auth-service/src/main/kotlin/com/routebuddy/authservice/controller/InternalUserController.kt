package com.routebuddy.authservice.controller

import com.routebuddy.authservice.dto.InternalUserResponse
import com.routebuddy.authservice.dto.PublicUserResponse
import com.routebuddy.authservice.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/internal/users")
class InternalUserController(
    private val userRepository: UserRepository,
    @Value("\${service.internal-token:}") private val internalToken: String
) {

    @GetMapping("/by-username/{username}", produces = ["application/json"])
    fun getUserByUsername(
        @PathVariable username: String,
        @RequestHeader("X-Internal-Token", required = false) headerToken: String?
    ): ResponseEntity<InternalUserResponse> {
        if (!hasInternalAccess(headerToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val user = userRepository.findByUsername(username)
        return if (user != null) ResponseEntity.ok(InternalUserResponse.from(user))
        else ResponseEntity.notFound().build()
    }

    @PutMapping("/by-username/{username}/avatar")
    fun updateAvatar(
        @PathVariable username: String,
        @RequestHeader("X-Internal-Token", required = false) headerToken: String?,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Void> {
        if (!hasInternalAccess(headerToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val user = userRepository.findByUsername(username) ?: return ResponseEntity.notFound().build()
        val updatedUser = user.copy(avatarUrl = body["avatarUrl"])
        userRepository.save(updatedUser)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/by-username/{username}/avatar")
    fun deleteAvatar(
        @PathVariable username: String,
        @RequestHeader("X-Internal-Token", required = false) headerToken: String?
    ): ResponseEntity<Void> {
        if (!hasInternalAccess(headerToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val user = userRepository.findByUsername(username) ?: return ResponseEntity.notFound().build()
        userRepository.save(user.copy(avatarUrl = null))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/by-id/{id}", produces = ["application/json"])
    fun getUserById(@PathVariable id: Long): ResponseEntity<PublicUserResponse> {
        return userRepository.findById(id)
            .map { ResponseEntity.ok(PublicUserResponse.from(it)) }
            .orElse(ResponseEntity.notFound().build())
    }

    private fun hasInternalAccess(headerToken: String?): Boolean =
        internalToken.isNotBlank() && headerToken == internalToken
}
