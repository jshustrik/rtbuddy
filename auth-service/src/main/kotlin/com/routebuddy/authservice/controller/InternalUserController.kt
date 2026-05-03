package com.routebuddy.authservice.controller

import com.routebuddy.authservice.model.User
import com.routebuddy.authservice.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/internal/users")
class InternalUserController(private val userRepository: UserRepository) {

    @GetMapping("/by-username/{username}", produces = ["application/json"])
    fun getUserByUsername(@PathVariable username: String): ResponseEntity<User> {
        val user = userRepository.findByUsername(username)
        return if (user != null) ResponseEntity.ok(user)
        else ResponseEntity.notFound().build()
    }

    @PutMapping("/by-username/{username}/avatar")
    fun updateAvatar(
        @PathVariable username: String,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Void> {
        val user = userRepository.findByUsername(username) ?: return ResponseEntity.notFound().build()
        val updatedUser = user.copy(avatarUrl = body["avatarUrl"])
        userRepository.save(updatedUser)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/by-id/{id}", produces = ["application/json"])
    fun getUserById(@PathVariable id: Long): ResponseEntity<User> {
        return userRepository.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }
}
