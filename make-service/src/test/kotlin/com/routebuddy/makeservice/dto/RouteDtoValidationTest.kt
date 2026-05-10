package com.routebuddy.makeservice.dto

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RouteDtoValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `day validation rejects short description`() {
        val violations = validator.validate(
            CreateDayRequest(
                dayNumber = 1,
                title = "Утро",
                description = "коротко",
                cost = 0.0,
                travelTime = "20 мин"
            )
        )

        assertTrue(violations.any { it.propertyPath.toString() == "description" })
    }

    @Test
    fun `point validation rejects invalid time format`() {
        val violations = validator.validate(
            CreatePointRequest(
                name = "Музей",
                description = "Главная точка маршрута",
                lat = 59.93,
                lon = 30.31,
                timeStart = "25:90",
                timeEnd = "12:30"
            )
        )

        assertTrue(violations.any { it.propertyPath.toString() == "timeStart" })
    }
}
