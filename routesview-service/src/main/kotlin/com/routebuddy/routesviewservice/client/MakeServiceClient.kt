package com.routebuddy.routesviewservice.client

import com.routebuddy.routesviewservice.client.dto.MakePageResponse
import com.routebuddy.routesviewservice.client.dto.MakeRouteDayWriteRequest
import com.routebuddy.routesviewservice.client.dto.MakeRouteResponse
import com.routebuddy.routesviewservice.client.dto.MakeRouteSummaryResponse
import com.routebuddy.routesviewservice.client.dto.MakeRouteWriteRequest
import com.routebuddy.routesviewservice.client.dto.MakeComposeRouteRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class MakeServiceClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${routebuddy.make-service-url:http://127.0.0.1:8084}") makeServiceBaseUrl: String,
) {
    private val client: RestClient = restClientBuilder
        .baseUrl(makeServiceBaseUrl)
        .build()

    fun listPublishedRoutes(query: String?, page: Int, size: Int): MakePageResponse<MakeRouteSummaryResponse> =
        client.get()
            .uri { b ->
                b.path("/api/v1/routes")
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .apply { if (!query.isNullOrBlank()) queryParam("q", query) }
                    .build()
            }
            .retrieve()
            .body(object : org.springframework.core.ParameterizedTypeReference<MakePageResponse<MakeRouteSummaryResponse>>() {})!!

    fun getRoute(routeId: Long, bearerToken: String? = null, shareToken: String? = null): MakeRouteResponse =
        client.get()
            .uri { b ->
                b.path("/api/v1/routes/{id}")
                    .apply { if (!shareToken.isNullOrBlank()) queryParam("shareToken", shareToken) }
                    .build(routeId)
            }
            .apply { if (!bearerToken.isNullOrBlank()) header("Authorization", "Bearer $bearerToken") }
            .retrieve()
            .body(MakeRouteResponse::class.java)!!

    fun listMyRoutes(bearerToken: String, page: Int = 0, size: Int = 50): MakePageResponse<MakeRouteSummaryResponse> =
        client.get()
            .uri { b ->
                b.path("/api/v1/routes/my")
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .build()
            }
            .header("Authorization", "Bearer $bearerToken")
            .retrieve()
            .body(object : org.springframework.core.ParameterizedTypeReference<MakePageResponse<MakeRouteSummaryResponse>>() {})!!

    fun updateRoute(routeId: Long, bearerToken: String, body: MakeRouteWriteRequest): MakeRouteResponse =
        client.put()
            .uri("/api/v1/routes/{id}", routeId)
            .header("Authorization", "Bearer $bearerToken")
            .body(body)
            .retrieve()
            .body(MakeRouteResponse::class.java)!!

    fun updateDay(routeId: Long, dayId: Long, bearerToken: String, body: MakeRouteDayWriteRequest): MakeRouteResponse =
        client.put()
            .uri("/api/v1/routes/{routeId}/days/{dayId}", routeId, dayId)
            .header("Authorization", "Bearer $bearerToken")
            .body(body)
            .retrieve()
            .body(MakeRouteResponse::class.java)!!

    fun composeRoute(bearerToken: String, body: MakeComposeRouteRequest): MakeRouteResponse =
        client.post()
            .uri("/api/v1/routes/compose")
            .header("Authorization", "Bearer $bearerToken")
            .body(body)
            .retrieve()
            .body(MakeRouteResponse::class.java)!!
}

