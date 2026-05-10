package com.routebuddy.usrsysservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "jwt.secret=test-secret-test-secret-test-secret-test-secret-test-secret-test-secret-test-secret"
    ]
)
class UsrsysServiceApplicationTests {

    @Test
    fun contextLoads() {
    }

}
