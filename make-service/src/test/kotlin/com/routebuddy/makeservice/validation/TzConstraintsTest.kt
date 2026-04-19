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
}
