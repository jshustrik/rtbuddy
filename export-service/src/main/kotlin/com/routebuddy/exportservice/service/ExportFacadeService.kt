package com.routebuddy.exportservice.service

import com.routebuddy.exportservice.client.MakeServiceClient
import com.routebuddy.exportservice.client.dto.MakeRouteDayResponse
import com.routebuddy.exportservice.client.dto.MakeRouteResponse
import com.routebuddy.exportservice.dto.ExportDayListItem
import com.routebuddy.exportservice.dto.ExportRouteListItem
import org.springframework.stereotype.Service

@Service
class ExportFacadeService(
    private val makeServiceClient: MakeServiceClient,
    private val pdfExportService: PdfExportService,
) {
    fun myRoutesForExport(bearerToken: String): List<ExportRouteListItem> {
        val summaries = makeServiceClient.listMyRoutes(bearerToken).content
        return summaries.map { summary ->
            val route = makeServiceClient.getRoute(summary.id, bearerToken)
            ExportRouteListItem(
                id = route.id,
                title = route.title,
                days = route.days
                    .sortedBy { it.dayOrder }
                    .map { day ->
                        ExportDayListItem(
                            id = day.id,
                            orderIndex = day.dayOrder,
                            theme = day.theme,
                        )
                    },
            )
        }
    }

    fun exportRoutePdf(routeId: Long, bearerToken: String): ByteArray {
        val route = makeServiceClient.getRoute(routeId, bearerToken)
        return pdfExportService.buildRoutePdf(route)
    }

    fun exportDayPdf(dayId: Long, bearerToken: String): Pair<String, ByteArray> {
        val route = findRouteByDayId(dayId, bearerToken)
        val day = route.days.first { it.id == dayId }
        val filename = "day_${route.id}_${day.dayOrder}.pdf"
        return filename to pdfExportService.buildDayPdf(route, day)
    }

    private fun findRouteByDayId(dayId: Long, bearerToken: String): MakeRouteResponse {
        val routes = makeServiceClient.listMyRoutes(bearerToken).content
        for (summary in routes) {
            val route = makeServiceClient.getRoute(summary.id, bearerToken)
            if (route.days.any { it.id == dayId }) return route
        }
        throw NoSuchElementException("Day $dayId not found in your routes")
    }
}
