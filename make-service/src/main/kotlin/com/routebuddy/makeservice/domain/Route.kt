package com.routebuddy.makeservice.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import org.hibernate.annotations.BatchSize
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "make_routes",
    indexes = [
        Index(name = "ix_make_routes_author", columnList = "author_user_id"),
        Index(name = "ix_make_routes_published_updated", columnList = "published,updated_at"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_make_routes_share_token", columnNames = ["share_token"]),
    ],
)
class Route(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "author_user_id", nullable = false)
    var authorUserId: Long = 0,

    @Column(name = "author_username", nullable = false, length = 64)
    var authorUsername: String = "",

    @Column(nullable = false, length = 100)
    var title: String = "",

    @Column(nullable = false, length = 1000)
    var description: String = "",

    @Column(name = "duration_days", nullable = false)
    var durationDays: Int = 1,

    @Column(name = "total_cost_rub", nullable = false, precision = 14, scale = 2)
    var totalCostRub: BigDecimal = BigDecimal.ZERO,

    @Column(name = "published", nullable = false)
    var published: Boolean = true,

    /**
     * Share-link token для приватного доступа к маршруту (делиться маршрутом из ТЗ).
     * Если `null`, маршрут доступен только автору (если не опубликован).
     */
    @Column(name = "share_token", length = 64, unique = true)
    var shareToken: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Version
    var version: Long = 0,

    @OneToMany(mappedBy = "route", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("dayOrder ASC")
    @BatchSize(size = 32)
    var days: MutableList<RouteDay> = mutableListOf(),
)
