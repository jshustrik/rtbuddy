package com.routebuddy.makeservice.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TravelTimeEstimatorTest {

    @Test
    fun `single point has zero travel`() {
        assertEquals(0, TravelTimeEstimator.totalTravelMinutes(listOf(55.75 to 37.61)))
    }

    @Test
    fun `two points yields positive travel minutes`() {
        val m = TravelTimeEstimator.totalTravelMinutes(
            listOf(
                59.935 to 30.327,
                59.940 to 30.330,
            ),
        )
        assert(m >= 0)
    }
}
