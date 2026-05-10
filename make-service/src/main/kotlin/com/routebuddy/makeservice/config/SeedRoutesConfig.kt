package com.routebuddy.makeservice.config

import com.routebuddy.makeservice.model.Day
import com.routebuddy.makeservice.model.Route
import com.routebuddy.makeservice.model.RoutePoint
import com.routebuddy.makeservice.repository.RouteRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class SeedRoutesConfig {
    private companion object {
        const val SPB_ROUTE_TITLE = "Петербург без спешки: музеи, вода и дворы"
        const val KAZAN_ROUTE_TITLE = "Казань за выходные"

        const val PHOTO_KAZAN_CATHEDRAL = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/24/Kazan_Cathedral_Saint_Petersburg.jpg/1280px-Kazan_Cathedral_Saint_Petersburg.jpg"
        const val PHOTO_SINGER_HOUSE = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/Singer_House_SPB_01.jpg/1280px-Singer_House_SPB_01.jpg"
        const val PHOTO_PALACE_SQUARE = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ed/RUS-2016-Aerial-SPB-Winter_Palace.jpg/1280px-RUS-2016-Aerial-SPB-Winter_Palace.jpg"
        const val PHOTO_HERMITAGE = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/75/Winter_Palace_Panorama_2.jpg/1280px-Winter_Palace_Panorama_2.jpg"
        const val PHOTO_PALACE_BRIDGE = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dc/Palace_Bridge_SPB_%28img2%29.jpg/1280px-Palace_Bridge_SPB_%28img2%29.jpg"
        const val PHOTO_VASILIEVSKY_SPIT = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/78/Spb_06-2017_img01_Spit_of_Vasilievsky_Island.jpg/1280px-Spb_06-2017_img01_Spit_of_Vasilievsky_Island.jpg"
        const val PHOTO_PETER_FORTRESS = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/17/RUS-2016-Aerial-SPB-Peter_and_Paul_Fortress_02.jpg/1280px-RUS-2016-Aerial-SPB-Peter_and_Paul_Fortress_02.jpg"
        const val PHOTO_KAZAN_KREMLIN = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Kazan_Kremlin_-_panoramio_%286%29.jpg/1280px-Kazan_Kremlin_-_panoramio_%286%29.jpg"
        const val PHOTO_BAUMAN_STREET = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Baumana_Street_Kazan_Russia_2009_sept_06.jpg/1280px-Baumana_Street_Kazan_Russia_2009_sept_06.jpg"
        const val PHOTO_KAZAN_FAMILY = "https://upload.wikimedia.org/wikipedia/ru/thumb/8/8f/%D0%A6%D0%B5%D0%BD%D1%82%D1%80_%D1%81%D0%B5%D0%BC%D1%8C%D0%B8_%D0%9A%D0%B0%D0%B7%D0%B0%D0%BD_1.jpg/1280px-%D0%A6%D0%B5%D0%BD%D1%82%D1%80_%D1%81%D0%B5%D0%BC%D1%8C%D0%B8_%D0%9A%D0%B0%D0%B7%D0%B0%D0%BD_1.jpg"
        const val PHOTO_KREMLIN_EMBANKMENT = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ec/%D0%9A%D1%80%D0%B5%D0%BC%D0%BB%D1%91%D0%B2%D1%81%D0%BA%D0%B0%D1%8F_%D0%BD%D0%B0%D0%B1%D0%B5%D1%80%D0%B5%D0%B6%D0%BD%D0%B0%D1%8F_%D1%81_%D1%85%D0%BE%D0%BB%D0%BC%D0%B0.jpg/1280px-%D0%9A%D1%80%D0%B5%D0%BC%D0%BB%D1%91%D0%B2%D1%81%D0%BA%D0%B0%D1%8F_%D0%BD%D0%B0%D0%B1%D0%B5%D1%80%D0%B5%D0%B6%D0%BD%D0%B0%D1%8F_%D1%81_%D1%85%D0%BE%D0%BB%D0%BC%D0%B0.jpg"
    }

    @Bean
    fun seedRoutes(routeRepository: RouteRepository, jdbcTemplate: JdbcTemplate): CommandLineRunner = CommandLineRunner {
        jdbcTemplate.execute("ALTER TABLE IF EXISTS ms_days ALTER COLUMN photo_url TYPE TEXT")
        jdbcTemplate.execute("ALTER TABLE IF EXISTS ms_route_points ALTER COLUMN photo_url TYPE TEXT")
        repairExistingPlacePhotos(jdbcTemplate)
        repairExistingTags(jdbcTemplate)

        val seededTitles = setOf(SPB_ROUTE_TITLE, KAZAN_ROUTE_TITLE)
        val existingSeedRoutes = routeRepository.findAll()
            .filter { it.title in seededTitles && it.authorUsername in setOf("traveler", "guide_kazan") }

        if (existingSeedRoutes.none { it.title == SPB_ROUTE_TITLE }) routeRepository.save(
            Route(
                title = SPB_ROUTE_TITLE,
                description = "Трехдневный маршрут по главным местам Санкт-Петербурга с короткими переходами, картой, временем посещения и спокойным темпом.",
                authorId = 1,
                authorUsername = "traveler",
                tags = "культура,музеи,маломобильные,город",
                isPublic = true,
                durationDays = 3,
                totalCost = 6200.0
            ).apply {
                days.add(
                    Day(
                        route = this,
                        dayNumber = 1,
                        title = "Невский и Дворцовая площадь",
                        description = "Знакомство с центром: Казанский собор, Дом книги, Дворцовая площадь и набережная.",
                        photoUrl = PHOTO_PALACE_SQUARE,
                        cost = 1800.0,
                        travelTime = "1 ч 20 мин"
                    ).apply {
                        points.addAll(
                            listOf(
                                point(this, 0, "Казанский собор", "Начало прогулки у Невского проспекта.", 59.9342802, 30.3245327, PHOTO_KAZAN_CATHEDRAL, "10:00", "10:45"),
                                point(this, 1, "Дом книги", "Короткая остановка на кофе и фото фасада.", 59.935808, 30.325454, PHOTO_SINGER_HOUSE, "11:00", "11:40"),
                                point(this, 2, "Дворцовая площадь", "Главная площадь города, удобная точка для карты маршрута.", 59.939832, 30.314559, PHOTO_PALACE_SQUARE, "12:10", "13:20")
                            )
                        )
                    }
                )
                days.add(
                    Day(
                        route = this,
                        dayNumber = 2,
                        title = "Эрмитаж и стрелка Васильевского острова",
                        description = "День с музеем, набережными и обзорными точками.",
                        photoUrl = PHOTO_HERMITAGE,
                        cost = 2600.0,
                        travelTime = "1 ч 40 мин"
                    ).apply {
                        points.addAll(
                            listOf(
                                point(this, 0, "Эрмитаж", "Основная музейная точка дня, лучше брать билет заранее.", 59.939864, 30.314566, PHOTO_HERMITAGE, "10:30", "13:30"),
                                point(this, 1, "Дворцовый мост", "Переход к стрелке и вид на Неву.", 59.941972, 30.308701, PHOTO_PALACE_BRIDGE, "13:45", "14:10"),
                                point(this, 2, "Стрелка В.О.", "Ростральные колонны и панорама центра.", 59.944316, 30.306086, PHOTO_VASILIEVSKY_SPIT, "14:20", "15:30")
                            )
                        )
                    }
                )
                days.add(
                    Day(
                        route = this,
                        dayNumber = 3,
                        title = "Петропавловская крепость",
                        description = "Финальный день с историческим ядром города и прогулкой вдоль Невы.",
                        photoUrl = PHOTO_PETER_FORTRESS,
                        cost = 1800.0,
                        travelTime = "1 ч"
                    ).apply {
                        points.addAll(
                            listOf(
                                point(this, 0, "Петропавловская крепость", "Историческое ядро Санкт-Петербурга.", 59.950015, 30.316308, PHOTO_PETER_FORTRESS, "11:00", "13:00"),
                                point(this, 1, "Соборная площадь крепости", "Точка для осмотра собора и внутренней территории.", 59.950489, 30.315863, PHOTO_PETER_FORTRESS, "13:10", "14:00")
                            )
                        )
                    }
                )
            }
        )

        if (existingSeedRoutes.none { it.title == KAZAN_ROUTE_TITLE }) routeRepository.save(
            Route(
                title = KAZAN_ROUTE_TITLE,
                description = "Маршрут на два дня: Кремль, Баумана, набережная Казанки и современные общественные пространства.",
                authorId = 2,
                authorUsername = "guide_kazan",
                tags = "выходные,архитектура,семейный",
                isPublic = true,
                durationDays = 2,
                totalCost = 4800.0
            ).apply {
                days.add(
                    Day(route = this, dayNumber = 1, title = "Кремль и центр", description = "Пешеходный день вокруг главных символов города.", photoUrl = PHOTO_KAZAN_KREMLIN, cost = 2500.0, travelTime = "1 ч 10 мин").apply {
                        points.addAll(
                            listOf(
                                point(this, 0, "Казанский Кремль", "Главная историческая точка маршрута.", 55.798931, 49.106405, PHOTO_KAZAN_KREMLIN, "10:00", "12:30"),
                                point(this, 1, "Улица Баумана", "Кафе, сувениры и прогулка по пешеходной улице.", 55.790847, 49.115312, PHOTO_BAUMAN_STREET, "13:00", "15:00")
                            )
                        )
                    }
                )
                days.add(
                    Day(route = this, dayNumber = 2, title = "Набережная и парк", description = "Более спокойный день с видами и отдыхом.", photoUrl = PHOTO_KREMLIN_EMBANKMENT, cost = 2300.0, travelTime = "50 мин").apply {
                        points.addAll(
                            listOf(
                                point(this, 0, "Центр семьи Казан", "Панорама и старт прогулки по набережной.", 55.812084, 49.108621, PHOTO_KAZAN_FAMILY, "11:00", "12:00"),
                                point(this, 1, "Парк Урам", "Современное пространство у воды.", 55.806884, 49.128801, PHOTO_KREMLIN_EMBANKMENT, "12:30", "14:30")
                            )
                        )
                    }
                )
            }
        )
    }

    private fun repairExistingPlacePhotos(jdbcTemplate: JdbcTemplate) {
        mapOf(
            "Невский и Дворцовая площадь" to PHOTO_PALACE_SQUARE,
            "Эрмитаж и стрелка Васильевского острова" to PHOTO_HERMITAGE,
            "Петропавловская крепость" to PHOTO_PETER_FORTRESS,
            "Кремль и центр" to PHOTO_KAZAN_KREMLIN,
            "Набережная и парк" to PHOTO_KREMLIN_EMBANKMENT
        ).forEach { (title, photoUrl) ->
            jdbcTemplate.update("UPDATE ms_days SET photo_url = ? WHERE title = ?", photoUrl, title)
        }

        mapOf(
            "Казанский собор" to PHOTO_KAZAN_CATHEDRAL,
            "Дом книги" to PHOTO_SINGER_HOUSE,
            "Дворцовая площадь" to PHOTO_PALACE_SQUARE,
            "Эрмитаж" to PHOTO_HERMITAGE,
            "Дворцовый мост" to PHOTO_PALACE_BRIDGE,
            "Стрелка В.О." to PHOTO_VASILIEVSKY_SPIT,
            "Петропавловская крепость" to PHOTO_PETER_FORTRESS,
            "Соборная площадь крепости" to PHOTO_PETER_FORTRESS,
            "Казанский Кремль" to PHOTO_KAZAN_KREMLIN,
            "Улица Баумана" to PHOTO_BAUMAN_STREET,
            "Центр семьи Казан" to PHOTO_KAZAN_FAMILY,
            "Парк Урам" to PHOTO_KREMLIN_EMBANKMENT
        ).forEach { (name, photoUrl) ->
            jdbcTemplate.update("UPDATE ms_route_points SET photo_url = ? WHERE name = ?", photoUrl, name)
        }
    }

    private fun repairExistingTags(jdbcTemplate: JdbcTemplate) {
        jdbcTemplate.update(
            "UPDATE ms_routes SET tags = replace(tags, 'маломобильных', 'маломобильные') WHERE tags LIKE '%маломобильных%'"
        )
    }

    private fun point(
        day: Day,
        orderIndex: Int,
        name: String,
        description: String,
        lat: Double,
        lon: Double,
        photoUrl: String?,
        timeStart: String,
        timeEnd: String
    ): RoutePoint =
        RoutePoint(
            day = day,
            orderIndex = orderIndex,
            name = name,
            description = description,
            lat = lat,
            lon = lon,
            photoUrl = photoUrl,
            timeStart = timeStart,
            timeEnd = timeEnd
        )
}
