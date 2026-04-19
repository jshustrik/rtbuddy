package com.routebuddy.makeservice.repository

import com.routebuddy.makeservice.domain.Route
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RouteRepository : JpaRepository<Route, Long> {

    fun findByAuthorUserIdOrderByUpdatedAtDesc(authorUserId: Long, pageable: Pageable): Page<Route>

    fun findByShareToken(shareToken: String): Route?

    @Query(
        """
        SELECT r FROM Route r
        WHERE r.published = true
          AND (
            LOWER(r.title) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(r.description) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        """,
    )
    fun searchPublished(@Param("q") q: String, pageable: Pageable): Page<Route>

    fun findByPublishedTrue(pageable: Pageable): Page<Route>
}
