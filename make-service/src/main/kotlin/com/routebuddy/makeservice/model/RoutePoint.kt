package com.routebuddy.makeservice.model

import jakarta.persistence.*

@Entity
@Table(name = "ms_route_points")
data class RoutePoint(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    var day: Day,

    @Column(nullable = false)
    var orderIndex: Int = 0,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String = "",

    @Column(nullable = false)
    var lat: Double = 0.0,

    @Column(nullable = false)
    var lon: Double = 0.0,

    @Column(columnDefinition = "TEXT")
    var photoUrl: String? = null,

    @Column
    var timeStart: String? = null,

    @Column
    var timeEnd: String? = null
)
