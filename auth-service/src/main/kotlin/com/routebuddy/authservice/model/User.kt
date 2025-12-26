package com.routebuddy.authservice.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false, length = 50)
    val username: String,
    @Column(name = "password_hash", nullable = false, length = 255)
    val passwordHash: String,
    @Column(nullable = false, length = 20)
    val role: String,
    @Column(length = 100)
    val email: String?
)
