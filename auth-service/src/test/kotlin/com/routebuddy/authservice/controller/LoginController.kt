package com.routebuddy.authservice.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LoginControllerTest {

    @Test
    fun `login should return login view`() {
        val controller = LoginController()
        val viewName = controller.login()
        assertEquals("login", viewName)
    }
}
