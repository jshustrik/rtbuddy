package com.routebuddy.usrsysservice.client

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.cloud.openfeign.FeignClient
import kotlin.reflect.full.findAnnotation

class AuthServiceClientTest {

    @Test
    fun `should have correct FeignClient annotation settings`() {
        val annotation = AuthServiceClient::class.findAnnotation<FeignClient>()
        assertEquals("auth-service", annotation?.name)
        // Проверяем, что url использует placeholder из properties
        assertEquals("\${auth.service.url}", annotation?.url)
    }
}
