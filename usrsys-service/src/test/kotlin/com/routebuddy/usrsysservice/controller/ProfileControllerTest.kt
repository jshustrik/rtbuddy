package com.routebuddy.usrsysservice.controller

import com.routebuddy.usrsysservice.service.ProfileService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class ProfileControllerTest {

    private val profileService = mock<ProfileService>()
    private val controller = ProfileController(profileService)

    @Test
    fun `viewCurrentProfile redirects to authenticated username`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("traveler", null)

        val view = controller.viewCurrentProfile()

        assertEquals("redirect:/profile/traveler", view)
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `updateAvatar delegates to service`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("traveler", null)

        val response = controller.updateAvatar(mapOf("avatarUrl" to "https://example.com/a.jpg"))

        assertEquals(200, response.statusCode.value())
        verify(profileService).updateAvatar("traveler", "https://example.com/a.jpg")
        SecurityContextHolder.clearContext()
    }
}
