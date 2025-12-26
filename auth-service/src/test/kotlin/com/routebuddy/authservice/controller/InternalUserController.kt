package com.routebuddy.authservice.controller

import com.quizwhiz.authservice.model.User
import com.quizwhiz.authservice.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

class InternalUserControllerTest {

    private val userRepository = mock<UserRepository>()
    private val controller = InternalUserController(userRepository)

    @Test
    fun `getUserByUsername should return 200 and user if found`() {
        val user = User(id = 10, username = "testuser", passwordHash = "hash", role = "USER", email = "test@example.com")
        whenever(userRepository.findByUsername("testuser")).thenReturn(user)

        val response: ResponseEntity<User> = controller.getUserByUsername("testuser")
        assertEquals(200, response.statusCodeValue)
        assertEquals(user, response.body)
    }

    @Test
    fun `getUserByUsername should return 404 if user not found`() {
        whenever(userRepository.findByUsername("notfound")).thenReturn(null)
        val response = controller.getUserByUsername("notfound")
        assertEquals(404, response.statusCodeValue)
        assertNull(response.body)
    }
}
