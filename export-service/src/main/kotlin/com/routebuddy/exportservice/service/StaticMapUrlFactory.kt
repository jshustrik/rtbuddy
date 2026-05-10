package com.routebuddy.exportservice.service

import com.routebuddy.exportservice.dto.ExportPointDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class StaticMapUrlFactory(
    @Value("\${yandex.maps.api-key:}") private val apiKey: String
) {
    fun build(points: List<ExportPointDto>): String? {
        val validPoints = points
            .filter { it.lat in -90.0..90.0 && it.lon in -180.0..180.0 }
            .sortedBy { it.orderIndex }
        if (apiKey.isBlank() || validPoints.isEmpty()) return null

        val avgLat = validPoints.sumOf { it.lat } / validPoints.size
        val avgLon = validPoints.sumOf { it.lon } / validPoints.size
        val markers = validPoints.joinToString("~") { point ->
            "${point.lon},${point.lat},pmwtm${(point.orderIndex + 1).coerceIn(1, 99)}"
        }

        val builder = UriComponentsBuilder.fromUriString("https://static-maps.yandex.ru/v1")
            .queryParam("lang", "ru_RU")
            .queryParam("apikey", apiKey)
            .queryParam("ll", "$avgLon,$avgLat")
            .queryParam("size", "650,300")
            .queryParam("z", if (validPoints.size > 1) "12" else "14")
            .queryParam("pt", markers)

        if (validPoints.size > 1) {
            builder.queryParam("pl", "c:1f6f68,w:4,${validPoints.joinToString(",") { "${it.lon},${it.lat}" }}")
        }

        return builder.build().toUriString()
    }
}
