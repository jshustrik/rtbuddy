package com.routebuddy.makeservice.model

import jakarta.persistence.*

@Entity
@Table(name = "ms_days")
data class Day(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    var route: Route,

    @Column(nullable = false)
    var dayNumber: Int,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String = "",

    @Column(name = "photo_url", columnDefinition = "TEXT")
    var photoUrl: String? = null,

    @Column(nullable = false)
    var cost: Double = 0.0,

    @Column
    var travelTime: String = "",

    @OneToMany(mappedBy = "day", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    var points: MutableList<RoutePoint> = mutableListOf()
)
