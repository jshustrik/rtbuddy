package com.routebuddy.routesviewservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.routebuddy.routesviewservice.security.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

/**
 * Proxy controller: browser → routesview (8083) → make-service (8084).
 * JWT cookie is HttpOnly so JS cannot read it — server extracts userId/username
 * and calls make-service API with proper auth header.
 */
@RestController
@RequestMapping("/api/proxy/routes")
class RouteProxyController(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${services.make-service}") private val makeServiceUrl: String
) {
    private val rest = RestTemplate()
    private val objectMapper = ObjectMapper()

    private fun extractToken(request: HttpServletRequest): String? {
        request.cookies?.forEach { if (it.name == "JWT") return it.value }
        val bearer = request.getHeader("Authorization") ?: return null
        return if (bearer.startsWith("Bearer ")) bearer.substring(7) else null
    }

    private fun authHeaders(request: HttpServletRequest): HttpHeaders {
        val token = extractToken(request)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Войдите в аккаунт")
        if (!jwtTokenProvider.validateToken(token)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Сессия истекла. Войдите заново")
        }
        return HttpHeaders().apply {
            set("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
        }
    }

    private fun upstreamMessage(e: RestClientResponseException, fallback: String): String {
        val body = e.responseBodyAsString.ifBlank { return fallback }
        return runCatching {
            val json = objectMapper.readValue(body, Map::class.java)
            (json["error"] ?: json["message"])?.toString()?.takeIf { it.isNotBlank() }
        }.getOrNull() ?: body
    }

    private fun proxyError(e: Exception): ResponseEntity<Any> =
        when (e) {
            is ResponseStatusException -> ResponseEntity.status(e.statusCode)
                .body(mapOf("error" to (e.reason ?: "Ошибка авторизации")))
            is RestClientResponseException -> {
                val message = when (e.statusCode) {
                    HttpStatus.UNAUTHORIZED -> "Войдите в аккаунт"
                    HttpStatus.FORBIDDEN -> upstreamMessage(e, "Нет прав на изменение этого маршрута")
                    HttpStatus.BAD_REQUEST -> upstreamMessage(e, "Проверьте поля формы")
                    else -> upstreamMessage(e, e.message ?: "Ошибка сервиса маршрутов")
                }
                ResponseEntity.status(e.statusCode).body(mapOf("error" to message))
            }
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to (e.message ?: "Ошибка сервиса маршрутов")))
        }

    // POST /api/proxy/routes → create route
    @PostMapping
    fun createRoute(@RequestBody body: Map<String, Any>, request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val headers = authHeaders(request)
            val entity = HttpEntity(body, headers)
            val resp = rest.postForEntity("$makeServiceUrl/api/routes", entity, Any::class.java)
            ResponseEntity.status(resp.statusCode).body(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // PUT /api/proxy/routes/{id} → update route
    @PutMapping("/{id}")
    fun updateRoute(@PathVariable id: Long, @RequestBody body: Map<String, Any>, request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val headers = authHeaders(request)
            val entity = HttpEntity(body, headers)
            val resp = rest.exchange("$makeServiceUrl/api/routes/$id", HttpMethod.PUT, entity, Any::class.java)
            ResponseEntity.status(resp.statusCode).body(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // DELETE /api/proxy/routes/{id}
    @DeleteMapping("/{id}")
    fun deleteRoute(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val headers = authHeaders(request)
            val entity = HttpEntity<Void>(headers)
            rest.exchange("$makeServiceUrl/api/routes/$id", HttpMethod.DELETE, entity, Void::class.java)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // POST /api/proxy/routes/{routeId}/days
    @PostMapping("/{routeId}/days")
    fun addDay(@PathVariable routeId: Long, @RequestBody body: Map<String, Any>, request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val headers = authHeaders(request)
            val entity = HttpEntity(body, headers)
            val resp = rest.postForEntity("$makeServiceUrl/api/routes/$routeId/days", entity, Any::class.java)
            ResponseEntity.status(resp.statusCode).body(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // DELETE /api/proxy/routes/{routeId}/days/{dayId}
    @DeleteMapping("/{routeId}/days/{dayId}")
    fun deleteDay(@PathVariable routeId: Long, @PathVariable dayId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val headers = authHeaders(request)
            val entity = HttpEntity<Void>(headers)
            rest.exchange("$makeServiceUrl/api/routes/$routeId/days/$dayId", HttpMethod.DELETE, entity, Void::class.java)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // POST /api/proxy/routes/{routeId}/days/{dayId}/points
    @PostMapping("/{routeId}/days/{dayId}/points")
    fun addPoint(@PathVariable routeId: Long, @PathVariable dayId: Long, @RequestBody body: Map<String, Any>, request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val headers = authHeaders(request)
            val entity = HttpEntity(body, headers)
            val resp = rest.postForEntity("$makeServiceUrl/api/routes/$routeId/days/$dayId/points", entity, Any::class.java)
            ResponseEntity.status(resp.statusCode).body(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // DELETE /api/proxy/routes/{routeId}/days/{dayId}/points/{pointId}
    @DeleteMapping("/{routeId}/days/{dayId}/points/{pointId}")
    fun deletePoint(
        @PathVariable routeId: Long,
        @PathVariable dayId: Long,
        @PathVariable pointId: Long,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        return try {
            val headers = authHeaders(request)
            val entity = HttpEntity<Void>(headers)
            rest.exchange(
                "$makeServiceUrl/api/routes/$routeId/days/$dayId/points/$pointId",
                HttpMethod.DELETE, entity, Void::class.java
            )
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // PUT /api/proxy/routes/{routeId}/days/{dayId}/points/{pointId}
    @PutMapping("/{routeId}/days/{dayId}/points/{pointId}")
    fun updatePoint(
        @PathVariable routeId: Long,
        @PathVariable dayId: Long,
        @PathVariable pointId: Long,
        @RequestBody body: Map<String, Any>,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        return try {
            val headers = authHeaders(request)
            val entity = HttpEntity(body, headers)
            val resp = rest.exchange(
                "$makeServiceUrl/api/routes/$routeId/days/$dayId/points/$pointId",
                HttpMethod.PUT, entity, Any::class.java
            )
            ResponseEntity.status(resp.statusCode).body(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // PUT /api/proxy/routes/{routeId}/days/{dayId} — update day
    @PutMapping("/{routeId}/days/{dayId}")
    fun updateDay(
        @PathVariable routeId: Long,
        @PathVariable dayId: Long,
        @RequestBody body: Map<String, Any>,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        return try {
            val headers = authHeaders(request)
            val entity = HttpEntity(body, headers)
            val resp = rest.exchange(
                "$makeServiceUrl/api/routes/$routeId/days/$dayId",
                HttpMethod.PUT, entity, Any::class.java
            )
            ResponseEntity.status(resp.statusCode).body(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // GET /api/proxy/routes/{id} → single route pass-through
    @GetMapping("/{id}")
    fun getRoute(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            val resp = rest.getForEntity("$makeServiceUrl/api/routes/$id", Any::class.java)
            ResponseEntity.status(resp.statusCode).body(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // GET /api/proxy/routes → public list (pass-through)
    @GetMapping
    fun listRoutes(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) my: Boolean?,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        return try {
            val url = buildString {
                append("$makeServiceUrl/api/routes")
            }
            val uriBuilder = UriComponentsBuilder.fromUriString(url)
            if (!search.isNullOrBlank()) uriBuilder.queryParam("search", search)
            if (my == true) uriBuilder.queryParam("my", true)
            val uri = uriBuilder.build().encode().toUri()
            // for "my" routes we need JWT
            val headers = if (my == true) {
                try { authHeaders(request) } catch (e: Exception) { HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON } }
            } else {
                HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            }
            val resp = rest.exchange(uri, HttpMethod.GET, HttpEntity<Void>(headers), Any::class.java)
            ResponseEntity.ok(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }
}
