package com.routebuddy.routesviewservice.repository

import com.routebuddy.routesviewservice.document.Route
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RouteRepository : MongoRepository<Route, String> {

    fun findByAuthorId(authorId: String): List<Route>

    fun findByAuthorUsername(authorUsername: String): List<Route>

    fun findByCity(city: String): List<Route>

    fun findByIsPublic(isPublic: Boolean): List<Route>

    fun findByDifficulty(difficulty: String): List<Route>

    @Query("{ '\$or': [ " +
            "{ 'title': { '\$regex': ?0, '\$options': 'i' } }, " +
            "{ 'description': { '\$regex': ?0, '\$options': 'i' } }, " +
            "{ 'city': { '\$regex': ?0, '\$options': 'i' } }, " +
            "{ 'authorUsername': { '\$regex': ?0, '\$options': 'i' } } " +
            "] }")
    fun searchByText(query: String): List<Route>

    @Query("{ 'tags': { '\$in': ?0 } }")
    fun findByTags(tags: List<String>): List<Route>

    @Query("{ 'createdAt': { '\$gte': ?0 } }")
    fun findRecentRoutes(since: java.time.LocalDateTime): List<Route>

    fun countByAuthorId(authorId: String): Long
}