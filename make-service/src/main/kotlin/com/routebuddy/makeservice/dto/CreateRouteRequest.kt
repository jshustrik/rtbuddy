package com.routebuddy.makeservice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateRouteRequest(
    @field:NotBlank
    @field:Pattern(regexp = "^(?=.{3,100}$)(?=.*[A-Za-zА-Яа-яЁё]).*$")
    val title: String,
    @field:Size(min = 10, max = 1000)
    @field:Pattern(regexp = "^(?!.*<[^>]+>)[\\s\\S]*$")
    val description: String = "",
    @field:Size(max = 200)
    val tags: String = "",
    val isPublic: Boolean = true,
    @field:Min(1)
    @field:Max(30)
    val durationDays: Int = 1,
    @field:DecimalMin("0.0")
    val totalCost: Double = 0.0
)

data class UpdateRouteRequest(
    @field:NotBlank
    @field:Pattern(regexp = "^(?=.{3,100}$)(?=.*[A-Za-zА-Яа-яЁё]).*$")
    val title: String,
    @field:Size(min = 10, max = 1000)
    @field:Pattern(regexp = "^(?!.*<[^>]+>)[\\s\\S]*$")
    val description: String = "",
    @field:Size(max = 200)
    val tags: String = "",
    val isPublic: Boolean = true,
    @field:Min(1)
    @field:Max(30)
    val durationDays: Int = 1,
    @field:DecimalMin("0.0")
    val totalCost: Double = 0.0
)

data class CreateDayRequest(
    @field:Min(1)
    @field:Max(30)
    val dayNumber: Int = 1,
    @field:NotBlank
    @field:Size(min = 2, max = 100)
    val title: String,
    @field:Size(max = 1000)
    @field:Pattern(regexp = "^(?!.*<[^>]+>)[\\s\\S]*$")
    val description: String = "",
    @field:Size(max = 7_000_000)
    val photoUrl: String? = null,
    @field:DecimalMin("0.0")
    val cost: Double = 0.0,
    @field:Size(max = 50)
    val travelTime: String = ""
)

data class UpdateDayRequest(
    @field:Min(1)
    @field:Max(30)
    val dayNumber: Int = 1,
    @field:NotBlank
    @field:Size(min = 2, max = 100)
    val title: String,
    @field:Size(max = 1000)
    @field:Pattern(regexp = "^(?!.*<[^>]+>)[\\s\\S]*$")
    val description: String = "",
    @field:Size(max = 7_000_000)
    val photoUrl: String? = null,
    @field:DecimalMin("0.0")
    val cost: Double = 0.0,
    @field:Size(max = 50)
    val travelTime: String = ""
)

data class CreatePointRequest(
    @field:Min(0)
    val orderIndex: Int = 0,
    @field:NotBlank
    @field:Size(min = 2, max = 100)
    val name: String,
    @field:Size(max = 300)
    @field:Pattern(regexp = "^(?!.*<[^>]+>)[\\s\\S]*$")
    val description: String = "",
    @field:DecimalMin("-90.0")
    @field:DecimalMax("90.0")
    val lat: Double = 0.0,
    @field:DecimalMin("-180.0")
    @field:DecimalMax("180.0")
    val lon: Double = 0.0,
    @field:Size(max = 7_000_000)
    val photoUrl: String? = null,
    @field:Size(max = 10)
    val timeStart: String? = null,
    @field:Size(max = 10)
    val timeEnd: String? = null
)

data class UpdatePointRequest(
    @field:Min(0)
    val orderIndex: Int = 0,
    @field:NotBlank
    @field:Size(min = 2, max = 100)
    val name: String,
    @field:Size(max = 300)
    @field:Pattern(regexp = "^(?!.*<[^>]+>)[\\s\\S]*$")
    val description: String = "",
    @field:DecimalMin("-90.0")
    @field:DecimalMax("90.0")
    val lat: Double = 0.0,
    @field:DecimalMin("-180.0")
    @field:DecimalMax("180.0")
    val lon: Double = 0.0,
    @field:Size(max = 7_000_000)
    val photoUrl: String? = null,
    @field:Size(max = 10)
    val timeStart: String? = null,
    @field:Size(max = 10)
    val timeEnd: String? = null
)
