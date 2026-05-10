package com.routebuddy.routesviewservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "jwt.secret=test-secret-test-secret-test-secret-test-secret-test-secret-test-secret-test-secret",
        "services.internal-token=test-internal-token"
    ]
)
class RoutesviewServiceApplicationTests {

    @Test
    fun contextLoads() {
    }

}
