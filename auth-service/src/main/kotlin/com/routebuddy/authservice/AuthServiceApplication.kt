package com.routebuddy.authservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuthServiceApplication

fun main(args: Array<String>) {
    runApplication<com.routebuddy.authservice.AuthServiceApplication>(*args)
}