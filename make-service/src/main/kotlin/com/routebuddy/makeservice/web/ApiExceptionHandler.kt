package com.routebuddy.makeservice.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ApiErrorBody(
    val error: String,
    val code: String,
)

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(e: IllegalArgumentException): ResponseEntity<ApiErrorBody> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorBody(error = e.message ?: "Некорректный запрос", code = "BAD_REQUEST"),
        )

    @ExceptionHandler(AccessDeniedException::class)
    fun forbidden(e: AccessDeniedException): ResponseEntity<ApiErrorBody> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiErrorBody(error = e.message ?: "Доступ запрещён", code = "FORBIDDEN"),
        )

    @ExceptionHandler(ResourceNotFoundException::class)
    fun notFound(e: ResourceNotFoundException): ResponseEntity<ApiErrorBody> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiErrorBody(error = e.message ?: "Не найдено", code = "NOT_FOUND"),
        )
}
