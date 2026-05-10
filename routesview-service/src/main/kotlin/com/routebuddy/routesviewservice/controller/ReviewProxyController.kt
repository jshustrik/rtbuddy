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

/**
 * Proxy to review-service (8085).
 * review-service uses X-User-Id / X-Username headers.
 * JWT is HttpOnly → JS can't read it → server extracts and forwards.
 */
@RestController
@RequestMapping("/api/proxy/reviews")
class ReviewProxyController(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${services.review-service}") private val reviewServiceUrl: String,
    @Value("\${services.internal-token}") private val internalToken: String
) {
    private val rest = RestTemplate()
    private val objectMapper = ObjectMapper()

    private fun extractToken(request: HttpServletRequest): String? {
        request.cookies?.forEach { if (it.name == "JWT") return it.value }
        val bearer = request.getHeader("Authorization") ?: return null
        return if (bearer.startsWith("Bearer ")) bearer.substring(7) else null
    }

    private fun userHeaders(request: HttpServletRequest): HttpHeaders {
        val token = extractToken(request)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Войдите в аккаунт")
        if (!jwtTokenProvider.validateToken(token)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Сессия истекла. Войдите заново")
        }
        val userId = jwtTokenProvider.getUserIdFromJWT(token)
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: "unknown"
        return HttpHeaders().apply {
            set("X-User-Id", userId.toString())
            set("X-Username", username)
            set("X-Internal-Token", internalToken)
            contentType = MediaType.APPLICATION_JSON
        }
    }

    private fun upstreamMessage(e: RestClientResponseException, fallback: String): String {
        val body = e.responseBodyAsString.ifBlank { return fallback }
        return runCatching {
            val json = objectMapper.readValue(body, Map::class.java)
            (json["message"] ?: json["error"])?.toString()
                ?.takeIf { it.isNotBlank() && it != "Bad Request" }
        }.getOrNull() ?: fallback
    }

    private fun proxyError(e: Exception): ResponseEntity<Any> =
        when (e) {
            is ResponseStatusException -> ResponseEntity.status(e.statusCode)
                .body(mapOf("error" to (e.reason ?: "Ошибка авторизации")))
            is RestClientResponseException -> {
                val fallback = when (e.statusCode) {
                    HttpStatus.UNAUTHORIZED -> "Войдите в аккаунт"
                    HttpStatus.FORBIDDEN -> "Нет прав на изменение этого отзыва"
                    HttpStatus.CONFLICT -> "Вы уже оставили отзыв на этот маршрут"
                    HttpStatus.BAD_REQUEST -> "Проверьте текст и оценку отзыва"
                    HttpStatus.NOT_FOUND -> "Отзыв не найден"
                    else -> "Ошибка сервиса отзывов"
                }
                ResponseEntity.status(e.statusCode).body(mapOf("error" to upstreamMessage(e, fallback)))
            }
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Ошибка сервиса отзывов"))
        }

    // GET /api/proxy/reviews/routes/{routeId} — public
    @GetMapping("/routes/{routeId}")
    fun getReviews(
        @PathVariable routeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Any> {
        return try {
            val resp = rest.getForEntity(
                "$reviewServiceUrl/api/reviews/routes/$routeId?page=$page&size=$size",
                Any::class.java
            )
            ResponseEntity.ok(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // GET /api/proxy/reviews/routes/{routeId}/rating — public
    @GetMapping("/routes/{routeId}/rating")
    fun getRating(@PathVariable routeId: Long): ResponseEntity<Any> {
        return try {
            val resp = rest.getForEntity(
                "$reviewServiceUrl/api/reviews/routes/$routeId/rating",
                Any::class.java
            )
            ResponseEntity.ok(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // POST /api/proxy/reviews — create review (requires JWT)
    @PostMapping
    fun createReview(@RequestBody body: Map<String, Any>, request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val headers = userHeaders(request)
            val entity = HttpEntity(body, headers)
            val resp = rest.postForEntity("$reviewServiceUrl/api/reviews", entity, Any::class.java)
            ResponseEntity.status(resp.statusCode).body(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // DELETE /api/proxy/reviews/{reviewId}
    @DeleteMapping("/{reviewId}")
    fun deleteReview(@PathVariable reviewId: String, request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val headers = userHeaders(request)
            val entity = HttpEntity<Void>(headers)
            rest.exchange("$reviewServiceUrl/api/reviews/$reviewId", HttpMethod.DELETE, entity, Void::class.java)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // PUT /api/proxy/reviews/{reviewId}
    @PutMapping("/{reviewId}")
    fun updateReview(
        @PathVariable reviewId: String,
        @RequestBody body: Map<String, Any>,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        return try {
            val headers = userHeaders(request)
            val entity = HttpEntity(body, headers)
            val resp = rest.exchange("$reviewServiceUrl/api/reviews/$reviewId", HttpMethod.PUT, entity, Any::class.java)
            ResponseEntity.status(resp.statusCode).body(resp.body)
        } catch (e: Exception) {
            proxyError(e)
        }
    }

    // GET /api/proxy/me — returns current user info (userId, username) for JS
    @GetMapping("/me")
    fun me(request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val token = extractToken(request) ?: return ResponseEntity.status(401).body(mapOf("error" to "not authenticated"))
            val userId = jwtTokenProvider.getUserIdFromJWT(token)
            val username = jwtTokenProvider.getUsernameFromJWT(token) ?: "unknown"
            ResponseEntity.ok(mapOf("userId" to userId, "username" to username))
        } catch (e: Exception) {
            ResponseEntity.status(401).body(mapOf("error" to "Сессия истекла. Войдите заново"))
        }
    }
}
