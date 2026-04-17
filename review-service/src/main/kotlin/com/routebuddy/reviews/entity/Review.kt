package com.routebuddy.reviews.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "reviews")
@CompoundIndex(name = "route_user_unique", def = "{'routeId': 1, 'userId': 1}", unique = true)
data class Review(
    @Id
    val id: String? = null,
    val routeId: Long,
    val userId: Long,
    val authorUsername: String,
    var text: String,
    var rating: Int,
    @CreatedDate
    val createdAt: LocalDateTime? = null,
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
)
