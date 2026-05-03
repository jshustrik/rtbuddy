package com.routebuddy.makeservice.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(e: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val message = e.bindingResult.fieldErrors
            .map(::fieldMessage)
            .distinct()
            .joinToString("; ")
            .ifBlank { "Проверьте поля формы" }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to message))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun accessDenied(e: AccessDeniedException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Нет прав на изменение этого маршрута"))

    @ExceptionHandler(NoSuchElementException::class)
    fun notFound(e: NoSuchElementException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Объект не найден"))

    private fun fieldMessage(error: FieldError): String =
        when (error.field) {
            "title" -> "Название должно содержать 3-100 символов и хотя бы одну букву"
            "description" -> "Описание должно быть нужной длины и без HTML-тегов"
            "durationDays", "dayNumber" -> "Длительность должна быть от 1 до 30 дней"
            "totalCost", "cost" -> "Стоимость должна быть неотрицательной"
            "tags" -> "Теги должны быть не длиннее 200 символов"
            "name" -> "Название точки должно содержать 2-100 символов"
            "lat" -> "Широта должна быть от -90 до 90"
            "lon" -> "Долгота должна быть от -180 до 180"
            "photoUrl" -> "Фото должно быть JPG/PNG до 5 МБ"
            "travelTime" -> "Время в пути слишком длинное"
            "timeStart", "timeEnd" -> "Время точки должно быть в корректном формате"
            else -> "Проверьте поле «${error.field}»"
        }
}
