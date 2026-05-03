package com.routebuddy.routesviewservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class YandexMapsConfig(
    @Value("\${yandex.maps.api-key}") private val apiKey: String
) {
    @ModelAttribute("yandexMapsApiKey")
    fun yandexMapsApiKey(): String = apiKey
}
