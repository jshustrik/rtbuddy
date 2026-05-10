package com.routebuddy.authservice.controller

import com.routebuddy.authservice.dto.InternalUserResponse
import com.routebuddy.authservice.dto.PublicUserResponse
import com.routebuddy.authservice.model.User
import com.routebuddy.authservice.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

class InternalUserControllerTest {

    private val userRepository = mock<UserRepository>()
    private val controller = InternalUserController(userRepository, "internal-test-token")

    @Test
    fun `getUserByUsername should return 200 and user if found`() {
        val user = User(id = 10, username = "testuser", passwordHash = "hash", role = "USER", email = "test@example.com")
        whenever(userRepository.findByUsername("testuser")).thenReturn(user)

        val response: ResponseEntity<InternalUserResponse> =
            controller.getUserByUsername("testuser", "internal-test-token")
        assertEquals(200, response.statusCodeValue)
        assertEquals("testuser", response.body?.username)
        assertEquals("test@example.com", response.body?.email)
    }

    @Test
    fun `getUserByUsername should return 404 if user not found`() {
        whenever(userRepository.findByUsername("notfound")).thenReturn(null)
        val response = controller.getUserByUsername("notfound", "internal-test-token")
        assertEquals(404, response.statusCodeValue)
        assertNull(response.getBody())
    }

    @Test
    fun `getUserByUsername should return 403 without internal token`() {
        val response = controller.getUserByUsername("testuser", null)
        assertEquals(403, response.statusCodeValue)
    }

    @Test
    fun `getUserById returns public user without password hash`() {
        val user = User(id = 10, username = "testuser", passwordHash = "hash", role = "USER", email = "test@example.com")
        whenever(userRepository.findById(10L)).thenReturn(java.util.Optional.of(user))

        val response: ResponseEntity<PublicUserResponse> = controller.getUserById(10)

        assertEquals(200, response.statusCodeValue)
        assertEquals("testuser", response.body?.username)
    }

    @Test
    fun `deleteAvatar clears avatar for existing user`() {
        val user = User(
            id = 10,
            username = "testuser",
            passwordHash = "hash",
            role = "USER",
            email = "test@example.com",
            avatarUrl = "data:image/png;base64,old"
        )
        whenever(userRepository.findByUsername("testuser")).thenReturn(user)

        val response = controller.deleteAvatar("testuser", "internal-test-token")

        assertEquals(204, response.statusCodeValue)
        org.mockito.kotlin.verify(userRepository).save(user.copy(avatarUrl = null))
    }
}
