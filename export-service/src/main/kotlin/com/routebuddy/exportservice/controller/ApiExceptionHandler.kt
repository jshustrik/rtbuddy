package com.routebuddy.exportservice.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(ex: IllegalArgumentException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message ?: "Bad request")

    @ExceptionHandler(NoSuchElementException::class)
    fun notFound(ex: NoSuchElementException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message ?: "Not found")
}
