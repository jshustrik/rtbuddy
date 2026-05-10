package com.routebuddy.usrsysservice.controller

import com.routebuddy.usrsysservice.service.ProfileService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.ui.ConcurrentModel

class ProfileControllerTest {

    private val profileService = mock<ProfileService>()
    private val controller = ProfileController(profileService)

    @Test
    fun `viewCurrentProfile redirects to authenticated username`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("traveler", null, listOf(SimpleGrantedAuthority("ROLE_USER")))

        try {
            val view = controller.viewCurrentProfile()
            assertEquals("redirect:/profile/traveler", view)
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    fun `updateAvatar delegates to service`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("traveler", null, listOf(SimpleGrantedAuthority("ROLE_USER")))
        val avatar = "data:image/png;base64,iVBORw0KGgo="

        try {
            val response = controller.updateAvatar(mapOf("avatarUrl" to avatar))
            assertEquals(200, response.statusCode.value())
            verify(profileService).updateAvatar("traveler", avatar)
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    fun `updateAvatar rejects invalid avatar value`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("traveler", null, listOf(SimpleGrantedAuthority("ROLE_USER")))

        try {
            val response = controller.updateAvatar(mapOf("avatarUrl" to "javascript:alert(1)"))
            assertEquals(400, response.statusCode.value())
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    fun `deleteAvatar delegates to service`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("traveler", null, listOf(SimpleGrantedAuthority("ROLE_USER")))

        try {
            val response = controller.deleteAvatar()
            assertEquals(200, response.statusCode.value())
            verify(profileService).deleteAvatar("traveler")
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    fun `viewProfile returns 404 page for unknown requested profile`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("traveler", null, listOf(SimpleGrantedAuthority("ROLE_USER")))
        whenever(profileService.profileExists("missing")).thenReturn(false)

        try {
            val response = MockHttpServletResponse()
            val model = ConcurrentModel()
            val view = controller.viewProfile("missing", model, response)
            assertEquals("profile-not-found", view)
            assertEquals(404, response.status)
            assertEquals("missing", model.getAttribute("username"))
        } finally {
            SecurityContextHolder.clearContext()
        }
    }
}
