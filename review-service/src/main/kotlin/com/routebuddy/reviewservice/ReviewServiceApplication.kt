package com.routebuddy.reviewservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication(scanBasePackages = ["com.routebuddy"])
@EnableMongoRepositories(basePackages = ["com.routebuddy.reviews.repository"])
class ReviewServiceApplication

fun main(args: Array<String>) {
    runApplication<com.routebuddy.reviewservice.ReviewServiceApplication>(*args)
}
