package com.routebuddy.exportservice.service

import com.routebuddy.exportservice.dto.ExportPointDto
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StaticMapUrlFactoryTest {
    @Test
    fun `build returns null without api key`() {
        val factory = StaticMapUrlFactory("")

        val url = factory.build(listOf(ExportPointDto(lat = 55.75, lon = 37.61)))

        assertNull(url)
    }

    @Test
    fun `build creates yandex static map url for valid points`() {
        val factory = StaticMapUrlFactory("key")

        val url = factory.build(
            listOf(
                ExportPointDto(orderIndex = 0, lat = 55.75, lon = 37.61),
                ExportPointDto(orderIndex = 1, lat = 55.76, lon = 37.62)
            )
        )

        assertNotNull(url)
        assertTrue(url!!.startsWith("https://static-maps.yandex.ru/v1"))
        assertTrue(url.contains("apikey=key"))
        assertTrue(url.contains("pt="))
        assertTrue(url.contains("pl="))
    }

    @Test
    fun `build ignores invalid coordinates`() {
        val factory = StaticMapUrlFactory("key")

        val url = factory.build(
            listOf(
                ExportPointDto(orderIndex = 0, lat = -100000.0, lon = -10000.0),
                ExportPointDto(orderIndex = 1, lat = 55.76, lon = 37.62)
            )
        )

        assertNotNull(url)
        assertFalse(url!!.contains("-100000"))
    }
}
