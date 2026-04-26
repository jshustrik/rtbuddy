package com.routebuddy.makeservice.controller

import com.routebuddy.makeservice.support.TestJwtFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@ActiveProfiles("test")
class RouteControllerIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        val configurer = springSecurity()
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(configurer)
            .build()
    }

    private fun createBody(title: String = "Маршрут по Москве") = """
            {
              "title": "$title",
              "description": "Подробное описание маршрута по столице с достопримечательностями и прогулками.",
              "durationDays": 1,
              "totalCostRub": 5000,
              "published": true,
              "days": [
                {
                  "theme": "Первый день в городе",
                  "description": "Краткое описание дня с прогулками по центру города и посещением парков.",
                  "dayCostRub": 2500,
                  "points": [
                    {
                      "title": "Красная площадь",
                      "latitude": 55.7558,
                      "longitude": 37.6173,
                      "description": "Главная площадь",
                      "timeStart": "10:00",
                      "timeEnd": "12:00",
                      "stayMinutes": 120,
                      "orderIndex": 1,
                      "imageUrls": []
                    }
                  ]
                }
              ]
            }
    """.trimIndent()

    @Test
    fun `create route returns 201`() {
        mockMvc.perform(
            post("/api/v1/routes")
                .header("Authorization", "Bearer ${TestJwtFactory.token(1L, "tester")}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody()),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("Маршрут по Москве"))
    }

    @Test
    fun `list routes is public`() {
        mockMvc.perform(get("/api/v1/routes"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `create without auth returns 401 or 403`() {
        mockMvc.perform(
            post("/api/v1/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody()),
        )
            .andExpect { result ->
                val sc = result.response.status
                assertTrue(sc == 401 || sc == 403) { "expected 401 or 403, got $sc" }
            }
    }

    @Test
    fun `create with bearer jwt sets author from token`() {
        mockMvc.perform(
            post("/api/v1/routes")
                .header("Authorization", "Bearer ${TestJwtFactory.token(42L, "jwtUser")}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody("Тест JWT маршрута")),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.authorUserId").value(42))
            .andExpect(jsonPath("$.authorUsername").value("jwtUser"))
    }

    @Test
    fun `private route is readable only by author or share token`() {
        val createPrivate = """
            {
              "title": "Приватный маршрут",
              "description": "Это приватный маршрут, доступный по ссылке или автору.",
              "durationDays": 1,
              "totalCostRub": 1000,
              "published": false,
              "days": [
                {
                  "theme": "День 1",
                  "description": "Описание дня маршрута для теста приватного доступа.",
                  "dayCostRub": 1000,
                  "points": [
                    {
                      "title": "Точка 1",
                      "latitude": 55.7,
                      "longitude": 37.6,
                      "timeStart": "10:00",
                      "timeEnd": "11:00",
                      "imageUrls": []
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val mvcCreate = mockMvc.perform(
            post("/api/v1/routes")
                .header("Authorization", "Bearer ${TestJwtFactory.token(10L, "author")}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPrivate),
        )
            .andExpect(status().isCreated)
            .andReturn()

        val body = mvcCreate.response.contentAsString
        val idRegex = """"id"\s*:\s*(\d+)""".toRegex()
        val id = idRegex.find(body)!!.groupValues[1].toLong()

        // Public read by id should not reveal private route
        mockMvc.perform(get("/api/v1/routes/$id"))
            .andExpect(status().isNotFound)

        // Author can read
        mockMvc.perform(get("/api/v1/routes/$id").header("Authorization", "Bearer ${TestJwtFactory.token(10L, "author")}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.published").value(false))

        // Enable share and read via token endpoint
        val shareRes = mockMvc.perform(
            post("/api/v1/routes/$id/share")
                .header("Authorization", "Bearer ${TestJwtFactory.token(10L, "author")}"),
        )
            .andExpect(status().isOk)
            .andReturn()

        val shareBody = shareRes.response.contentAsString
        val tokenRegex = """"shareToken"\s*:\s*"([^"]+)"""".toRegex()
        val token = tokenRegex.find(shareBody)!!.groupValues[1]

        mockMvc.perform(get("/api/v1/routes/share/$token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.shareToken").value(token))

        // Query param shareToken also works
        mockMvc.perform(get("/api/v1/routes/$id").param("shareToken", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id.toInt()))
    }

    @Test
    fun `compose route from published day`() {
        val mvcCreate = mockMvc.perform(
            post("/api/v1/routes")
                .header("Authorization", "Bearer ${TestJwtFactory.token(1L, "seed")}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody("Источник для compose")),
        )
            .andExpect(status().isCreated)
            .andReturn()

        val created = mvcCreate.response.contentAsString
        val dayIdRegex = """"days"\s*:\s*\[\s*\{\s*"id"\s*:\s*(\d+)""".toRegex()
        val dayId = dayIdRegex.find(created)!!.groupValues[1].toLong()

        val composeBody = """
            {
              "title": "Собранный маршрут",
              "description": "Маршрут собран из дней другого опубликованного маршрута.",
              "published": true,
              "dayIds": [$dayId]
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/routes/compose")
                .header("Authorization", "Bearer ${TestJwtFactory.token(99L, "composer")}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(composeBody),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.authorUserId").value(99))
            .andExpect(jsonPath("$.durationDays").value(1))
            .andExpect(jsonPath("$.days[0].theme").exists())
    }

    @Test
    fun `create route rejects invalid point time and image format`() {
        val invalidBody = """
            {
              "title": "Невалидный маршрут",
              "description": "Описание маршрута достаточно длинное для прохождения базовой валидации.",
              "durationDays": 1,
              "totalCostRub": 5000,
              "published": true,
              "days": [
                {
                  "theme": "Тестовый день",
                  "description": "Описание дня достаточно подробное для прохождения валидации дня.",
                  "dayCostRub": 5000,
                  "points": [
                    {
                      "title": "Точка с ошибкой",
                      "latitude": 55.75,
                      "longitude": 37.61,
                      "timeStart": "14:00",
                      "timeEnd": "13:00",
                      "imageUrls": ["https://cdn.example.com/picture.webp"]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/routes")
                .header("Authorization", "Bearer ${TestJwtFactory.token(1L, "tester")}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody),
        )
            .andExpect(status().isBadRequest)
    }
}
