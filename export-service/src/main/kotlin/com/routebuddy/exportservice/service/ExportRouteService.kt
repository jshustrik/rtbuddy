package com.routebuddy.exportservice.service

import com.routebuddy.exportservice.dto.ExportRouteDto
import com.routebuddy.exportservice.dto.ExportRouteResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import org.springframework.web.util.UriComponentsBuilder

@Service
class ExportRouteService(
    private val restTemplate: RestTemplate,
    private val staticMapUrlFactory: StaticMapUrlFactory,
    @Value("\${services.make-service}") private val makeServiceUrl: String
) {
    fun listRoutes(search: String?): List<ExportRouteDto> {
        val uri = UriComponentsBuilder.fromUriString("$makeServiceUrl/api/routes")
            .apply { if (!search.isNullOrBlank()) queryParam("search", search) }
            .build()
            .encode()
            .toUri()

        return handleUpstream {
            restTemplate.exchange(uri, HttpMethod.GET, null, object : ParameterizedTypeReference<List<ExportRouteDto>>() {})
                .body
                .orEmpty()
        }
    }

    fun routeExport(routeId: Long): ExportRouteResponse {
        val route = handleUpstream {
            restTemplate.getForObject("$makeServiceUrl/api/routes/$routeId", ExportRouteDto::class.java)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Маршрут не найден")
        }
        val allPoints = route.days.flatMap { it.points }
        val dayMapUrls = route.days.associate { day ->
            day.id to staticMapUrlFactory.build(day.points)
        }.filterValues { it != null }.mapValues { requireNotNull(it.value) }

        return ExportRouteResponse(
            route = route,
            routeStaticMapUrl = staticMapUrlFactory.build(allPoints),
            dayStaticMapUrls = dayMapUrls
        )
    }

    private fun <T> handleUpstream(block: () -> T): T =
        try {
            block()
        } catch (e: RestClientResponseException) {
            when (e.statusCode) {
                HttpStatus.NOT_FOUND -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "Маршрут не найден")
                else -> throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Сервис маршрутов временно недоступен")
            }
        }
}
