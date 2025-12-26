package com.routebuddy.authservice.controller

import com.routebuddy.authservice.model.User
import com.routebuddy.authservice.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/users")
class InternalUserController(private val userRepository: UserRepository) {

    @GetMapping("/by-username/{username}", produces = ["application/json"])
    fun getUserByUsername(@PathVariable username: String): ResponseEntity<User> {
        val user = userRepository.findByUsername(username)
        return if (user != null) ResponseEntity.ok(user)
        else ResponseEntity.notFound().build()
    }
}