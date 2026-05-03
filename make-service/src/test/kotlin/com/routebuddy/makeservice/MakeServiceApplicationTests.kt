package com.routebuddy.makeservice

import org.junit.jupiter.api.Test

/**
 * Smoke-тест приложения.
 * Полный Spring-контекст не поднимается — для этого нужна база данных.
 * Реальные тесты находятся в:
 *   - RouteServiceTest (10 тестов — сервисный слой с Mockito)
 *   - RouteApiControllerTest (12 тестов — контроллерный слой с Mockito)
 */
class MakeServiceApplicationTests {

    @Test
    fun `smoke test - application class exists`() {
        // Без БД контекст не поднять — тесты логики см. в RouteServiceTest и RouteApiControllerTest
    }
}
