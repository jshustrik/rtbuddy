package com.routebuddy.reviews.controller

import com.routebuddy.reviews.exception.DuplicateReviewException
import com.routebuddy.reviews.exception.ReviewNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateReviewException::class)
    fun handleDuplicate(ex: DuplicateReviewException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to ex.message.orEmpty()))
    }

    @ExceptionHandler(ReviewNotFoundException::class)
    fun handleNotFound(ex: ReviewNotFoundException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message.orEmpty()))
    }

    @ExceptionHandler(IllegalStateException::class, IllegalArgumentException::class)
    fun handleBadRequest(ex: Exception): ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body(mapOf("error" to ex.message.orEmpty()))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val message = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Невалидные данные"
        return ResponseEntity.badRequest().body(mapOf("error" to message))
    }
}
