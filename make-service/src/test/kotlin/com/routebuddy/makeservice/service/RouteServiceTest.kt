package com.routebuddy.makeservice.service

import com.routebuddy.makeservice.dto.CreateRouteRequest
import com.routebuddy.makeservice.dto.UpdateRouteRequest
import com.routebuddy.makeservice.model.Route
import com.routebuddy.makeservice.repository.DayRepository
import com.routebuddy.makeservice.repository.RoutePointRepository
import com.routebuddy.makeservice.repository.RouteRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.util.Optional

class RouteServiceTest {

    private lateinit var routeRepository: RouteRepository
    private lateinit var dayRepository: DayRepository
    private lateinit var pointRepository: RoutePointRepository
    private lateinit var routeService: RouteService

    private fun makeRoute(id: Long = 1L, authorId: Long = 42L) = Route(
        id = id,
        title = "Test Route",
        description = "desc",
        authorId = authorId,
        authorUsername = "user42",
        tags = "природа,горы",
        isPublic = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        routeRepository = mock()
        dayRepository = mock()
        pointRepository = mock()
        routeService = RouteService(routeRepository, dayRepository, pointRepository)
    }

    // ── getPublicRoutes ───────────────────────────────────────────────────────

    @Test
    fun `getPublicRoutes returns list without days`() {
        val routes = listOf(makeRoute(1L), makeRoute(2L))
        whenever(routeRepository.searchPublic(null)).thenReturn(routes)

        val result = routeService.getPublicRoutes()

        assertEquals(2, result.size)
        assertEquals("Test Route", result[0].title)
        // includeDays=false → days empty in DTO
        assertTrue(result[0].days.isEmpty())
        verify(routeRepository).searchPublic(null)
    }

    @Test
    fun `getPublicRoutes with search passes keyword`() {
        whenever(routeRepository.searchPublic("байкал")).thenReturn(emptyList())
        routeService.getPublicRoutes("байкал")
        verify(routeRepository).searchPublic("байкал")
    }

    // ── getMyRoutes ───────────────────────────────────────────────────────────

    @Test
    fun `getMyRoutes filters by authorId`() {
        val routes = listOf(makeRoute(authorId = 7L))
        whenever(routeRepository.searchByAuthor(7L, null)).thenReturn(routes)

        val result = routeService.getMyRoutes(7L)

        assertEquals(1, result.size)
        assertEquals(7L, result[0].authorId)
    }

    // ── getRoute ──────────────────────────────────────────────────────────────

    @Test
    fun `getRoute returns DTO with days`() {
        val route = makeRoute(5L)
        whenever(routeRepository.findById(5L)).thenReturn(Optional.of(route))

        val result = routeService.getRoute(5L)

        assertEquals(5L, result.id)
        assertEquals("Test Route", result.title)
    }

    @Test
    fun `getRoute throws when not found`() {
        whenever(routeRepository.findById(99L)).thenReturn(Optional.empty())
        assertThrows<NoSuchElementException> { routeService.getRoute(99L) }
    }

    // ── createRoute ───────────────────────────────────────────────────────────

    @Test
    fun `createRoute saves and returns DTO`() {
        val req = CreateRouteRequest(title = "Новый маршрут", description = "описание", tags = "горы", isPublic = true)
        val saved = makeRoute(10L, authorId = 1L).copy(title = "Новый маршрут")
        whenever(routeRepository.save(any<Route>())).thenReturn(saved)

        val result = routeService.createRoute(req, 1L, "admin")

        assertEquals("Новый маршрут", result.title)
        verify(routeRepository).save(any())
    }

    // ── updateRoute ───────────────────────────────────────────────────────────

    @Test
    fun `updateRoute changes fields`() {
        val original = makeRoute(3L, authorId = 5L)
        whenever(routeRepository.findById(3L)).thenReturn(Optional.of(original))
        whenever(routeRepository.save(any<Route>())).thenAnswer { it.arguments[0] as Route }

        val req = UpdateRouteRequest(title = "Updated", description = "new desc", tags = "", isPublic = false)
        val result = routeService.updateRoute(3L, req, 5L)

        assertEquals("Updated", result.title)
        assertFalse(result.isPublic)
    }

    @Test
    fun `updateRoute throws AccessDeniedException if not author`() {
        val route = makeRoute(3L, authorId = 5L)
        whenever(routeRepository.findById(3L)).thenReturn(Optional.of(route))

        assertThrows<org.springframework.security.access.AccessDeniedException> {
            routeService.updateRoute(3L, UpdateRouteRequest("X"), userId = 99L)
        }
    }

    // ── deleteRoute ───────────────────────────────────────────────────────────

    @Test
    fun `deleteRoute removes route`() {
        val route = makeRoute(2L, authorId = 3L)
        whenever(routeRepository.findById(2L)).thenReturn(Optional.of(route))

        routeService.deleteRoute(2L, 3L)

        verify(routeRepository).delete(route)
    }

    @Test
    fun `deleteRoute throws AccessDeniedException if not author`() {
        val route = makeRoute(2L, authorId = 3L)
        whenever(routeRepository.findById(2L)).thenReturn(Optional.of(route))

        assertThrows<org.springframework.security.access.AccessDeniedException> {
            routeService.deleteRoute(2L, 999L)
        }
    }
}
