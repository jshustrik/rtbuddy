package com.routebuddy.makeservice.domain

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "make_route_points")
class RoutePoint(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    var day: RouteDay? = null,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 1,

    @Column(nullable = false, length = 100)
    var title: String = "",

    @Column(nullable = false)
    var latitude: Double = 0.0,

    @Column(nullable = false)
    var longitude: Double = 0.0,

    @Column(length = 300)
    var description: String? = null,

    /** Формат чч:мм по ТЗ */
    @Column(name = "time_start", length = 5)
    var timeStart: String? = null,

    @Column(name = "time_end", length = 5)
    var timeEnd: String? = null,

    @Column(name = "stay_minutes")
    var stayMinutes: Int? = null,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "make_route_point_photos", joinColumns = [JoinColumn(name = "point_id")])
    @Column(name = "url", length = 2048)
    var imageUrls: MutableList<String> = mutableListOf(),
)
