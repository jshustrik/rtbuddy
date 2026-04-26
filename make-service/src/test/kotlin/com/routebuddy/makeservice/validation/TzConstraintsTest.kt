package com.routebuddy.makeservice.validation

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TzConstraintsTest {

    @Test
    fun `route title too short throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            TzConstraints.validateRouteTitle("Аб")
        }
    }

    @Test
    fun `route title ok`() {
        assertDoesNotThrow { TzConstraints.validateRouteTitle("Золотое кольцо России") }
    }

    @Test
    fun `route description rejects html`() {
        assertThrows(IllegalArgumentException::class.java) {
            TzConstraints.validateRouteDescription("<p>".padEnd(12, 'a'))
        }
    }

    @Test
    fun `time format`() {
        assertDoesNotThrow { TzConstraints.validateTimeHm("14:30") }
        assertThrows(IllegalArgumentException::class.java) { TzConstraints.validateTimeHm("25:00") }
    }

    @Test
    fun `cost non negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            TzConstraints.validateCostNonNegative(BigDecimal("-1"))
        }
    }

    @Test
    fun `time range end must be after start`() {
        assertDoesNotThrow { TzConstraints.validateTimeRange("09:00", "10:30") }
        assertThrows(IllegalArgumentException::class.java) {
            TzConstraints.validateTimeRange("10:30", "10:30")
        }
        assertThrows(IllegalArgumentException::class.java) {
            TzConstraints.validateTimeRange("11:00", "10:30")
        }
    }

    @Test
    fun `stay minutes must be non negative`() {
        assertDoesNotThrow { TzConstraints.validateStayMinutes(0) }
        assertThrows(IllegalArgumentException::class.java) {
            TzConstraints.validateStayMinutes(-5)
        }
    }

    @Test
    fun `image urls must be jpg or png`() {
        assertDoesNotThrow {
            TzConstraints.validateImageUrls(
                listOf(
                    "https://cdn.example.com/img/photo.jpg",
                    "https://cdn.example.com/another.png?cache=1",
                ),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            TzConstraints.validateImageUrls(listOf("https://cdn.example.com/file.webp"))
        }
    }
}
