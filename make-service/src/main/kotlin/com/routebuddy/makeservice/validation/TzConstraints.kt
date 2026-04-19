package com.routebuddy.makeservice.validation

import java.math.BigDecimal

object TzConstraints {
    const val ROUTE_TITLE_MIN = 3
    const val ROUTE_TITLE_MAX = 100
    const val ROUTE_DESC_MIN = 10
    const val ROUTE_DESC_MAX = 1000
    const val DURATION_MIN_DAYS = 1
    const val DURATION_MAX_DAYS = 30

    const val DAY_THEME_MIN = 3
    const val DAY_THEME_MAX = 100
    const val DAY_DESC_MIN = 10
    const val DAY_DESC_MAX = 500

    const val POINT_TITLE_MIN = 2
    const val POINT_TITLE_MAX = 100
    const val POINT_DESC_MAX = 300

    private val HTML_TAG = Regex("<[^>]+>")

    fun validateRouteTitle(raw: String) {
        val title = raw.trim()
        require(title.length in ROUTE_TITLE_MIN..ROUTE_TITLE_MAX) {
            "Название маршрута: от $ROUTE_TITLE_MIN до $ROUTE_TITLE_MAX символов"
        }
        require(title.any { it.isLetter() }) { "Название маршрута должно содержать хотя бы одну букву" }
        require(!title.all { it.isWhitespace() || !it.isLetterOrDigit() && !it.isWhitespace() }) {
            "Название не должно состоять только из пробелов или спецсимволов"
        }
    }

    fun validateRouteDescription(raw: String) {
        val d = raw.trim()
        require(d.length in ROUTE_DESC_MIN..ROUTE_DESC_MAX) {
            "Описание маршрута: от $ROUTE_DESC_MIN до $ROUTE_DESC_MAX символов"
        }
        require(!HTML_TAG.containsMatchIn(d)) { "Описание маршрута не должно содержать HTML-тегов" }
    }

    fun validateDurationDays(days: Int) {
        require(days in DURATION_MIN_DAYS..DURATION_MAX_DAYS) {
            "Длительность маршрута: от $DURATION_MIN_DAYS до $DURATION_MAX_DAYS дней"
        }
    }

    fun validateCostNonNegative(value: BigDecimal) {
        require(value >= BigDecimal.ZERO) { "Стоимость не может быть отрицательной" }
    }

    fun validateDayTheme(raw: String) {
        val t = raw.trim()
        require(t.length in DAY_THEME_MIN..DAY_THEME_MAX) {
            "Тема дня: от $DAY_THEME_MIN до $DAY_THEME_MAX символов"
        }
    }

    fun validateDayDescription(raw: String) {
        val d = raw.trim()
        require(d.length in DAY_DESC_MIN..DAY_DESC_MAX) {
            "Описание дня: от $DAY_DESC_MIN до $DAY_DESC_MAX символов"
        }
        require(!HTML_TAG.containsMatchIn(d)) { "Описание дня не должно содержать HTML-тегов" }
    }

    fun validatePointTitle(raw: String) {
        val t = raw.trim()
        require(t.length in POINT_TITLE_MIN..POINT_TITLE_MAX) {
            "Название точки: от $POINT_TITLE_MIN до $POINT_TITLE_MAX символов"
        }
    }

    fun validatePointDescription(raw: String?) {
        if (raw.isNullOrBlank()) return
        require(raw.length <= POINT_DESC_MAX) { "Описание точки: не более $POINT_DESC_MAX символов" }
        require(!HTML_TAG.containsMatchIn(raw)) { "Описание точки не должно содержать HTML-тегов" }
    }

    fun validateCoordinates(lat: Double, lon: Double) {
        require(lat in -90.0..90.0 && lon in -180.0..180.0) {
            "Некорректные координаты точки"
        }
    }

    fun validateTimeHm(raw: String?) {
        if (raw.isNullOrBlank()) return
        require(raw.matches(Regex("""^([01]\d|2[0-3]):[0-5]\d$"""))) {
            "Время должно быть в формате чч:мм"
        }
    }
}
