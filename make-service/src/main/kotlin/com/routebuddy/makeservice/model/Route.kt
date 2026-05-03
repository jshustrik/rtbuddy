package com.routebuddy.makeservice.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "ms_routes")
data class Route(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String = "",

    @Column(nullable = false)
    var authorId: Long,

    @Column
    var authorUsername: String = "",

    @Column
    var tags: String = "",

    @Column(nullable = false)
    var isPublic: Boolean = true,

    @Column(nullable = false)
    var durationDays: Int = 1,

    @Column(nullable = false)
    var totalCost: Double = 0.0,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "route", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("dayNumber ASC")
    var days: MutableList<Day> = mutableListOf()
)
