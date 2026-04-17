package com.routebuddy.reviewservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.routebuddy"])
class ReviewServiceApplication

fun main(args: Array<String>) {
    runApplication<com.routebuddy.reviewservice.ReviewServiceApplication>(*args)
}
