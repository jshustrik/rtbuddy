package com.routebuddy.usrsysservice.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HomeControllerTest {

    private val controller = HomeController()

    @Test
    fun `home should redirect to login`() {
        val view = controller.home()
        assertEquals("redirect:http://127.0.0.1:8081/login", view)
    }
}
