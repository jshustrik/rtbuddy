package com.routebuddy.routesviewservice.client

import com.routebuddy.routesviewservice.dto.ExportDayListItem
import com.routebuddy.routesviewservice.dto.ExportRouteListItem
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ExportServiceClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${routebuddy.export-service-url:http://127.0.0.1:8086}") exportServiceBaseUrl: String,
) {
    private val client: RestClient = restClientBuilder.baseUrl(exportServiceBaseUrl).build()

    fun listMyRoutes(bearerToken: String): List<ExportRouteListItem> =
        client.get()
            .uri("/api/my-routes")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $bearerToken")
            .retrieve()
            .body(object : ParameterizedTypeReference<List<ExportRouteListItem>>() {})!!

    fun exportRoutePdf(routeId: Long, bearerToken: String): ByteArray =
        client.get()
            .uri("/api/export/route/{routeId}", routeId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $bearerToken")
            .retrieve()
            .body(ByteArray::class.java)!!

    fun exportDayPdf(dayId: Long, bearerToken: String): ByteArray =
        client.get()
            .uri("/api/export/day/{dayId}", dayId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $bearerToken")
            .retrieve()
            .body(ByteArray::class.java)!!
}
