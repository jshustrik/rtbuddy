package com.routebuddy.makeservice.service

import com.routebuddy.makeservice.dto.CreateDayRequest
import com.routebuddy.makeservice.dto.CreatePointRequest
import com.routebuddy.makeservice.dto.UpdateDayRequest
import com.routebuddy.makeservice.dto.UpdatePointRequest
import com.routebuddy.makeservice.model.Day
import com.routebuddy.makeservice.model.Route
import com.routebuddy.makeservice.model.RoutePoint
import com.routebuddy.makeservice.repository.DayRepository
import com.routebuddy.makeservice.repository.RoutePointRepository
import com.routebuddy.makeservice.repository.RouteRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.access.AccessDeniedException
import java.util.Optional

class RouteServiceDayPointTest {

    private lateinit var routeRepository: RouteRepository
    private lateinit var dayRepository: DayRepository
    private lateinit var pointRepository: RoutePointRepository
    private lateinit var routeService: RouteService

    @BeforeEach
    fun setUp() {
        routeRepository = mock()
        dayRepository = mock()
        pointRepository = mock()
        routeService = RouteService(routeRepository, dayRepository, pointRepository)
    }

    private fun route(authorId: Long = 7L) = Route(
        id = 10L,
        title = "Route",
        description = "Description",
        authorId = authorId,
        authorUsername = "alice"
    )

    private fun day(route: Route) = Day(
        id = 20L,
        route = route,
        dayNumber = 1,
        title = "Day",
        description = "Old day"
    )

    @Test
    fun `addDay saves day for author`() {
        val route = route()
        val req = CreateDayRequest(dayNumber = 2, title = "Second", description = "Plan", cost = 1200.0, travelTime = "40 мин")
        whenever(routeRepository.findById(10L)).thenReturn(Optional.of(route))
        whenever(dayRepository.save(any<Day>())).thenAnswer { it.arguments[0] as Day }
        whenever(routeRepository.save(any<Route>())).thenAnswer { it.arguments[0] as Route }

        val result = routeService.addDay(10L, req, 7L)

        assertEquals("Second", result.title)
        assertEquals(1200.0, result.cost)
        verify(dayRepository).save(any())
    }

    @Test
    fun `updateDay rejects non author`() {
        whenever(routeRepository.findById(10L)).thenReturn(Optional.of(route(authorId = 1L)))

        assertThrows<AccessDeniedException> {
            routeService.updateDay(10L, 20L, UpdateDayRequest(title = "Nope"), 2L)
        }
    }

    @Test
    fun `addPoint stores coordinates and photo`() {
        val route = route()
        val day = day(route)
        val req = CreatePointRequest(
            orderIndex = 1,
            name = "Кремль",
            description = "Главная точка",
            lat = 55.798931,
            lon = 49.106405,
            photoUrl = "https://example.com/photo.jpg",
            timeStart = "10:00",
            timeEnd = "12:00"
        )
        whenever(routeRepository.findById(10L)).thenReturn(Optional.of(route))
        whenever(dayRepository.findById(20L)).thenReturn(Optional.of(day))
        whenever(pointRepository.save(any<RoutePoint>())).thenAnswer { it.arguments[0] as RoutePoint }
        whenever(routeRepository.save(any<Route>())).thenAnswer { it.arguments[0] as Route }

        val result = routeService.addPoint(10L, 20L, req, 7L)

        assertEquals("Кремль", result.name)
        assertEquals(55.798931, result.lat)
        assertEquals("https://example.com/photo.jpg", result.photoUrl)
    }

    @Test
    fun `addPoint accepts http image cdn url without file extension`() {
        val route = route()
        val day = day(route)
        val photo = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80"
        val req = CreatePointRequest(
            name = "Парк",
            description = "Фото с CDN без расширения",
            lat = 55.75,
            lon = 37.61,
            photoUrl = photo
        )
        whenever(routeRepository.findById(10L)).thenReturn(Optional.of(route))
        whenever(dayRepository.findById(20L)).thenReturn(Optional.of(day))
        whenever(pointRepository.save(any<RoutePoint>())).thenAnswer { it.arguments[0] as RoutePoint }
        whenever(routeRepository.save(any<Route>())).thenAnswer { it.arguments[0] as Route }

        val result = routeService.addPoint(10L, 20L, req, 7L)

        assertEquals(photo, result.photoUrl)
    }

    @Test
    fun `addPoint rejects non http photo url`() {
        val route = route()
        val day = day(route)
        whenever(routeRepository.findById(10L)).thenReturn(Optional.of(route))
        whenever(dayRepository.findById(20L)).thenReturn(Optional.of(day))

        assertThrows<IllegalArgumentException> {
            routeService.addPoint(10L, 20L, CreatePointRequest(name = "X", photoUrl = "javascript:alert(1)"), 7L)
        }
    }

    @Test
    fun `addPoint rejects day from another route`() {
        val route = route()
        val otherRoute = route().copy(id = 99L)
        val day = day(otherRoute)
        whenever(routeRepository.findById(10L)).thenReturn(Optional.of(route))
        whenever(dayRepository.findById(20L)).thenReturn(Optional.of(day))

        assertThrows<AccessDeniedException> {
            routeService.addPoint(10L, 20L, CreatePointRequest(name = "Wrong"), 7L)
        }
    }

    @Test
    fun `updatePoint changes point fields`() {
        val route = route()
        val day = day(route)
        val point = RoutePoint(id = 30L, day = day, name = "Old", description = "Old")
        val req = UpdatePointRequest(orderIndex = 3, name = "New", description = "New desc", lat = 1.0, lon = 2.0)
        whenever(routeRepository.findById(10L)).thenReturn(Optional.of(route))
        whenever(pointRepository.findById(30L)).thenReturn(Optional.of(point))
        whenever(pointRepository.save(any<RoutePoint>())).thenAnswer { it.arguments[0] as RoutePoint }
        whenever(routeRepository.save(any<Route>())).thenAnswer { it.arguments[0] as Route }

        val result = routeService.updatePoint(10L, 20L, 30L, req, 7L)

        assertEquals("New", result.name)
        assertEquals(3, result.orderIndex)
    }

    @Test
    fun `deletePoint removes point for author`() {
        val route = route()
        val day = day(route)
        val point = RoutePoint(id = 30L, day = day, name = "Point")
        whenever(routeRepository.findById(10L)).thenReturn(Optional.of(route))
        whenever(pointRepository.findById(30L)).thenReturn(Optional.of(point))
        whenever(routeRepository.save(any<Route>())).thenAnswer { it.arguments[0] as Route }

        routeService.deletePoint(10L, 20L, 30L, 7L)

        verify(pointRepository).delete(point)
    }

    @Test
    fun `deletePoint rejects point from another day`() {
        val route = route()
        val otherDay = day(route).copy(id = 99L)
        val point = RoutePoint(id = 30L, day = otherDay, name = "Point")
        whenever(routeRepository.findById(10L)).thenReturn(Optional.of(route))
        whenever(pointRepository.findById(30L)).thenReturn(Optional.of(point))

        assertThrows<AccessDeniedException> {
            routeService.deletePoint(10L, 20L, 30L, 7L)
        }
    }
}
