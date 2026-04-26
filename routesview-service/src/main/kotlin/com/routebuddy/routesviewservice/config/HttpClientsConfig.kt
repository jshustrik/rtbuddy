package com.routebuddy.routesviewservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class HttpClientsConfig {
    @Bean
    fun restClientBuilder(): RestClient.Builder = RestClient.builder()
}

