package com.routebuddy.routesviewservice.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

internal fun httpRedirect(location: String): ResponseEntity<Void> =
    ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION, location)
        .build()
