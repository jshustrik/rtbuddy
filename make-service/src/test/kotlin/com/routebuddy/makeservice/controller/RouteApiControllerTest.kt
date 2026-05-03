package com.routebuddy.makeservice.controller

import com.routebuddy.makeservice.dto.*
import com.routebuddy.makeservice.service.RouteService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import java.time.LocalDateTime

/**
 * Модульный тест контроллера RouteApiController.
 * Тестирует логику контроллера без поднятия Spring-контекста — быстро и изолированно.
 */
class RouteApiControllerTest {

    private lateinit var routeService: RouteService
    private lateinit var controller: RouteApiController
    private lateinit var auth: Authentication

    private fun sampleDto(id: Long = 1L) = RouteDto(
        id = id,
        title = "Маршрут по Байкалу",
        description = "Озёра, горы, тайга",
        authorId = 10L,
        authorUsername = "tester",
        tags = "природа,озёра",
        isPublic = true,
        durationDays = 5,
        totalCost = 15000.0,
        createdAt = LocalDateTime.of(2025, 6, 1, 12, 0),
        updatedAt = LocalDateTime.of(2025, 6, 1, 12, 0),
        days = emptyList()
    )

    @BeforeEach
    fun setUp() {
        routeService = mock()
        controller = RouteApiController(routeService)
        auth = mock {
            on { name } doReturn "tester"
            on { credentials } doReturn 10L
        }
    }

    // ── GET /api/routes ──────────────────────────────────────────────────────

    @Test
    fun `listRoutes returns all public routes when no filters`() {
        val routes = listOf(sampleDto(1), sampleDto(2))
        whenever(routeService.getPublicRoutes(null)).thenReturn(routes)

        val resp = controller.listRoutes(search = null, my = null, authentication = null)

        assertEquals(HttpStatus.OK, resp.statusCode)
        assertEquals(2, resp.body?.size)
        verify(routeService).getPublicRoutes(null)
    }

    @Test
    fun `listRoutes filters by search query`() {
        whenever(routeService.getPublicRoutes("Байкал")).thenReturn(listOf(sampleDto()))

        val resp = controller.listRoutes(search = "Байкал", my = null, authentication = null)

        assertEquals(HttpStatus.OK, resp.statusCode)
        assertEquals(1, resp.body?.size)
        verify(routeService).getPublicRoutes("Байкал")
    }

    @Test
    fun `listRoutes returns my routes when my=true and authenticated`() {
        whenever(routeService.getMyRoutes(10L, null)).thenReturn(listOf(sampleDto()))

        val resp = controller.listRoutes(search = null, my = true, authentication = auth)

        assertEquals(HttpStatus.OK, resp.statusCode)
        verify(routeService).getMyRoutes(10L, null)
        verify(routeService, never()).getPublicRoutes(any())
    }

    // ── GET /api/routes/{id} ─────────────────────────────────────────────────

    @Test
    fun `getRoute returns route when found`() {
        whenever(routeService.getRoute(1L)).thenReturn(sampleDto(1L))

        val resp = controller.getRoute(1L)

        assertEquals(HttpStatus.OK, resp.statusCode)
        assertEquals("Маршрут по Байкалу", resp.body?.title)
    }

    @Test
    fun `getRoute returns 404 when not found`() {
        whenever(routeService.getRoute(99L)).thenThrow(NoSuchElementException("not found"))

        val resp = controller.getRoute(99L)

        assertEquals(HttpStatus.NOT_FOUND, resp.statusCode)
    }

    // ── POST /api/routes ─────────────────────────────────────────────────────

    @Test
    fun `createRoute returns 201 with created route`() {
        val req = CreateRouteRequest(
            title = "Новый маршрут",
            description = "Описание",
            tags = "",
            isPublic = true,
            durationDays = 3,
            totalCost = 5000.0
        )
        val created = sampleDto(5L).copy(title = "Новый маршрут")
        whenever(routeService.createRoute(req, 10L, "tester")).thenReturn(created)

        val resp = controller.createRoute(req, auth)

        assertEquals(HttpStatus.CREATED, resp.statusCode)
        assertEquals(5L, resp.body?.id)
        assertEquals("Новый маршрут", resp.body?.title)
    }

    // ── PUT /api/routes/{id} ──────────────────────────────────────────────────

    @Test
    fun `updateRoute returns updated route`() {
        val req = UpdateRouteRequest(title = "Обновлённый", description = "Новое", tags = "", isPublic = true, durationDays = 7, totalCost = 0.0)
        val updated = sampleDto(1L).copy(title = "Обновлённый")
        whenever(routeService.updateRoute(1L, req, 10L)).thenReturn(updated)

        val resp = controller.updateRoute(1L, req, auth)

        assertEquals(HttpStatus.OK, resp.statusCode)
        assertEquals("Обновлённый", resp.body?.title)
    }

    @Test
    fun `updateRoute returns 404 when route not found`() {
        val req = UpdateRouteRequest(title = "X", description = "", tags = "", isPublic = true, durationDays = 1, totalCost = 0.0)
        whenever(routeService.updateRoute(99L, req, 10L)).thenThrow(NoSuchElementException())

        val resp = controller.updateRoute(99L, req, auth)

        assertEquals(HttpStatus.NOT_FOUND, resp.statusCode)
    }

    @Test
    fun `updateRoute returns 403 when not author`() {
        val req = UpdateRouteRequest(title = "X", description = "", tags = "", isPublic = true, durationDays = 1, totalCost = 0.0)
        whenever(routeService.updateRoute(1L, req, 10L))
            .thenThrow(org.springframework.security.access.AccessDeniedException("forbidden"))

        val resp = controller.updateRoute(1L, req, auth)

        assertEquals(HttpStatus.FORBIDDEN, resp.statusCode)
    }

    // ── DELETE /api/routes/{id} ───────────────────────────────────────────────

    @Test
    fun `deleteRoute returns 204 on success`() {
        doNothing().whenever(routeService).deleteRoute(1L, 10L)

        val resp = controller.deleteRoute(1L, auth)

        assertEquals(HttpStatus.NO_CONTENT, resp.statusCode)
    }

    @Test
    fun `deleteRoute returns 404 when not found`() {
        whenever(routeService.deleteRoute(99L, 10L)).thenThrow(NoSuchElementException())

        val resp = controller.deleteRoute(99L, auth)

        assertEquals(HttpStatus.NOT_FOUND, resp.statusCode)
    }

    @Test
    fun `deleteRoute returns 403 when not author`() {
        whenever(routeService.deleteRoute(1L, 10L))
            .thenThrow(org.springframework.security.access.AccessDeniedException("forbidden"))

        val resp = controller.deleteRoute(1L, auth)

        assertEquals(HttpStatus.FORBIDDEN, resp.statusCode)
    }
}
