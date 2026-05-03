package com.routebuddy.routesviewservice.controller

import com.routebuddy.routesviewservice.security.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

/**
 * Proxy to review-service (8085).
 * review-service uses X-User-Id / X-Username headers.
 * JWT is HttpOnly → JS can't read it → server extracts and forwards.
 */
@RestController
@RequestMapping("/api/proxy/reviews")
class ReviewProxyController(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${services.review-service}") private val reviewServiceUrl: String
) {
    private val rest = RestTemplate()

    private fun extractToken(request: HttpServletRequest): String? {
        request.cookies?.forEach { if (it.name == "JWT") return it.value }
        val bearer = request.getHeader("Authorization") ?: return null
        return if (bearer.startsWith("Bearer ")) bearer.substring(7) else null
    }

    private fun userHeaders(request: HttpServletRequest): HttpHeaders {
        val token = extractToken(request) ?: error("Not authenticated")
        val userId = jwtTokenProvider.getUserIdFromJWT(token)
        val username = jwtTokenProvider.getUsernameFromJWT(token) ?: "unknown"
        return HttpHeaders().apply {
            set("X-User-Id", userId.toString())
            set("X-Username", username)
            contentType = MediaType.APPLICATION_JSON
        }
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
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to (e.message ?: "error")))
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
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to (e.message ?: "error")))
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
            val msg = e.message ?: "error"
            val status = if (msg.contains("409") || msg.contains("already")) HttpStatus.CONFLICT else HttpStatus.INTERNAL_SERVER_ERROR
            ResponseEntity.status(status).body(mapOf("error" to msg))
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
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to (e.message ?: "error")))
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
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to (e.message ?: "error")))
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
            ResponseEntity.status(401).body(mapOf("error" to "invalid token"))
        }
    }
}
