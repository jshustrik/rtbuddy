package com.routebuddy.exportservice.client

import com.routebuddy.exportservice.client.dto.MakePageResponse
import com.routebuddy.exportservice.client.dto.MakeRouteResponse
import com.routebuddy.exportservice.client.dto.MakeRouteSummaryResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class MakeServiceClient(
    @Value("\${routebuddy.make-service-url:http://127.0.0.1:8084}") makeServiceBaseUrl: String,
) {
    private val client: RestClient = RestClient.builder()
        .baseUrl(makeServiceBaseUrl)
        .build()

    fun listMyRoutes(bearerToken: String): MakePageResponse<MakeRouteSummaryResponse> =
        client.get()
            .uri("/api/v1/routes/my")
            .header("Authorization", "Bearer $bearerToken")
            .retrieve()
            .body(object : ParameterizedTypeReference<MakePageResponse<MakeRouteSummaryResponse>>() {})!!

    fun getRoute(routeId: Long, bearerToken: String): MakeRouteResponse =
        client.get()
            .uri("/api/v1/routes/{id}", routeId)
            .header("Authorization", "Bearer $bearerToken")
            .retrieve()
            .body(MakeRouteResponse::class.java)!!
}
