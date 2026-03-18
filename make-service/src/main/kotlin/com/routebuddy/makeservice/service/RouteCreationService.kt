package com.routebuddy.makeservice.service

@Service
class RouteCreationService(
    private val routeRepository: RouteRepository,
    private val routeDayRepository: RouteDayRepository,
    private val routePointRepository: RoutePointRepository,
    private val authServiceClient: AuthServiceClient? = null // для получения данных пользователя, опционально
) {

    private val logger = LoggerFactory.getLogger(RouteCreationService::class.java)

    /**
     * Полное создание маршрута с днями и точками
     */
    @Transactional(rollbackFor = [Exception::class]) // транзакция MongoDB (через MongoTemplate)
    fun createFullRoute(request: CreateRouteRequest, authorId: String, authorUsername: String): Route {
        logger.info("Создание полного маршрута пользователем $authorUsername")

        // 1. Создаём маршрут
        val route = Route(
            title = request.title,
            description = request.description,
            shortDescription = request.shortDescription,
            authorId = authorId,
            authorUsername = authorUsername,
            difficulty = try {
                RouteDifficulty.valueOf(request.difficulty.uppercase())
            } catch (e: Exception) {
                RouteDifficulty.MEDIUM
            },
            distance = request.distance,
            durationDays = request.durationDays,
            totalCost = request.totalCost,
            city = request.city,
            isPublic = request.isPublic,
            tags = request.tags,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedRoute = routeRepository.save(route)

        // 2. Обрабатываем дни
        request.days.forEach { dayReq ->
            val day = RouteDay(
                routeId = savedRoute.id!!,
                dayNumber = dayReq.dayNumber,
                title = dayReq.title,
                description = dayReq.description,
                cost = dayReq.cost,
                createdAt = LocalDateTime.now()
            )
            val savedDay = routeDayRepository.save(day)

            // 3. Обрабатываем точки дня
            val points = dayReq.points.map { pointReq ->
                RoutePoint(
                    routeId = savedRoute.id!!,
                    dayId = savedDay.id,
                    latitude = pointReq.latitude,
                    longitude = pointReq.longitude,
                    title = pointReq.title,
                    description = pointReq.description,
                    type = try { PointType.valueOf(pointReq.type.uppercase()) } catch (e: Exception) { PointType.SIGHTSEEING },
                    cost = pointReq.cost,
                    timeSpent = pointReq.timeSpent,
                    orderIndex = pointReq.orderIndex,
                    createdAt = LocalDateTime.now()
                )
            }
            routePointRepository.saveAll(points)
        }

        logger.info("Маршрут ${savedRoute.id} успешно создан")
        return savedRoute
    }

    /**
     * Создание только маршрута (без дней)
     */
    fun createRouteOnly(request: CreateRouteRequest, authorId: String, authorUsername: String): Route {
        val route = Route(
            title = request.title,
            description = request.description,
            shortDescription = request.shortDescription,
            authorId = authorId,
            authorUsername = authorUsername,
            difficulty = try { RouteDifficulty.valueOf(request.difficulty.uppercase()) } catch (e: Exception) { RouteDifficulty.MEDIUM },
            distance = request.distance,
            durationDays = request.durationDays,
            totalCost = request.totalCost,
            city = request.city,
            isPublic = request.isPublic,
            tags = request.tags,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return routeRepository.save(route)
    }

    /**
     * Добавление дня к существующему маршруту
     */
    fun addDayToRoute(routeId: String, request: CreateDayRequest, authorId: String): RouteDay {
        // Проверяем, что маршрут существует и автор совпадает
        val route = routeRepository.findById(routeId)
            .orElseThrow { IllegalArgumentException("Маршрут не найден") }
        if (route.authorId != authorId) {
            throw SecurityException("Только автор может добавлять дни")
        }

        val day = RouteDay(
            routeId = routeId,
            dayNumber = request.dayNumber,
            title = request.title,
            description = request.description,
            cost = request.cost,
            createdAt = LocalDateTime.now()
        )
        val savedDay = routeDayRepository.save(day)

        // Добавляем точки, если есть
        if (request.points.isNotEmpty()) {
            val points = request.points.map { pointReq ->
                RoutePoint(
                    routeId = routeId,
                    dayId = savedDay.id,
                    latitude = pointReq.latitude,
                    longitude = pointReq.longitude,
                    title = pointReq.title,
                    description = pointReq.description,
                    type = try { PointType.valueOf(pointReq.type.uppercase()) } catch (e: Exception) { PointType.SIGHTSEEING },
                    cost = pointReq.cost,
                    timeSpent = pointReq.timeSpent,
                    orderIndex = pointReq.orderIndex,
                    createdAt = LocalDateTime.now()
                )
            }
            routePointRepository.saveAll(points)
        }

        // Обновляем длительность и стоимость маршрута? – опционально, можно пересчитать
        // Например, увеличиваем durationDays, если день новый
        // route.durationDays = max(route.durationDays, request.dayNumber)
        // route.totalCost += request.cost + points.sumOf { it.cost ?: 0.0 }
        // routeRepository.save(route)

        return savedDay
    }

    /**
     * Добавление точки к существующему дню
     */
    fun addPointToDay(dayId: String, request: CreatePointRequest, authorId: String): RoutePoint {
        val day = routeDayRepository.findById(dayId)
            .orElseThrow { IllegalArgumentException("День не найден") }
        val route = routeRepository.findById(day.routeId)
            .orElseThrow { IllegalArgumentException("Маршрут не найден") }
        if (route.authorId != authorId) {
            throw SecurityException("Только автор может добавлять точки")
        }

        val point = RoutePoint(
            routeId = day.routeId,
            dayId = dayId,
            latitude = request.latitude,
            longitude = request.longitude,
            title = request.title,
            description = request.description,
            type = try { PointType.valueOf(request.type.uppercase()) } catch (e: Exception) { PointType.SIGHTSEEING },
            cost = request.cost,
            timeSpent = request.timeSpent,
            orderIndex = request.orderIndex,
            createdAt = LocalDateTime.now()
        )
        return routePointRepository.save(point)
    }
}