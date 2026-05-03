package com.routebuddy.makeservice.repository

import com.routebuddy.makeservice.model.Route
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RouteRepository : JpaRepository<Route, Long> {

    fun findByAuthorId(authorId: Long): List<Route>

    fun findByIsPublicTrue(): List<Route>

    @Query("""
        SELECT r FROM Route r
        WHERE r.isPublic = true
          AND (:search IS NULL OR :search = ''
               OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(r.tags) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    fun searchPublic(@Param("search") search: String?): List<Route>

    @Query("""
        SELECT r FROM Route r
        WHERE r.authorId = :authorId
          AND (:search IS NULL OR :search = ''
               OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(r.tags) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    fun searchByAuthor(@Param("authorId") authorId: Long, @Param("search") search: String?): List<Route>
}
