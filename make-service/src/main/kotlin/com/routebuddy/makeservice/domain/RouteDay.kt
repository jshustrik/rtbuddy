package com.routebuddy.makeservice.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import java.math.BigDecimal

@Entity
@Table(name = "make_route_days")
class RouteDay(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    var route: Route? = null,

    @Column(name = "day_order", nullable = false)
    var dayOrder: Int = 1,

    @Column(nullable = false, length = 100)
    var theme: String = "",

    @Column(nullable = false, length = 500)
    var description: String = "",

    @Column(name = "day_cost_rub", nullable = false, precision = 14, scale = 2)
    var dayCostRub: BigDecimal = BigDecimal.ZERO,

    /** Суммарное время в пути между точками (минуты), согласно ТЗ — пересчёт при сохранении. */
    @Column(name = "travel_time_minutes", nullable = false)
    var travelTimeMinutes: Int = 0,

    @OneToMany(mappedBy = "day", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    @BatchSize(size = 64)
    var points: MutableList<RoutePoint> = mutableListOf(),
)
