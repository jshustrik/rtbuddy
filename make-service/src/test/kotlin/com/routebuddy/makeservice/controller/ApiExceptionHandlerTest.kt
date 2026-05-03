package com.routebuddy.makeservice.controller

import com.routebuddy.makeservice.dto.CreateRouteRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

class ApiExceptionHandlerTest {
    private val handler = ApiExceptionHandler()

    @Test
    fun `validation errors return bad request with Russian route field messages`() {
        val exception = validationException(
            FieldError("createRouteRequest", "title", "123", false, null, null, "invalid"),
            FieldError("createRouteRequest", "description", "123", false, null, null, "invalid")
        )

        val response = handler.validation(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body?.get("error")?.contains("Название должно содержать 3-100 символов") == true)
        assertTrue(response.body?.get("error")?.contains("Описание должно быть нужной длины") == true)
    }

    @Suppress("unused")
    private fun dummy(req: CreateRouteRequest) = Unit

    private fun validationException(vararg errors: FieldError): MethodArgumentNotValidException {
        val method = this::class.java.getDeclaredMethod("dummy", CreateRouteRequest::class.java)
        val binding = BeanPropertyBindingResult(Any(), "createRouteRequest")
        errors.forEach(binding::addError)
        return MethodArgumentNotValidException(MethodParameter(method, 0), binding)
    }
}
