package com.routebuddy.usrsysservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients(basePackages = ["com.routebuddy.usrsysservice.client"])
class UsrsysServiceApplication

fun main(args: Array<String>) {
    runApplication<UsrsysServiceApplication>(*args)
}
