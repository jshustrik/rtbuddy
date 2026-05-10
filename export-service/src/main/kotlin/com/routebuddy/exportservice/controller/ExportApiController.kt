package com.routebuddy.exportservice.controller

import com.routebuddy.exportservice.dto.ExportRouteDto
import com.routebuddy.exportservice.dto.ExportRouteResponse
import com.routebuddy.exportservice.service.ExportRouteService
import com.routebuddy.exportservice.service.PdfExportService
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/export")
class ExportApiController(
    private val exportRouteService: ExportRouteService,
    private val pdfExportService: PdfExportService
) {
    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf("status" to "ok")

    @GetMapping("/routes")
    fun listRoutes(@RequestParam(required = false) search: String?): List<ExportRouteDto> =
        exportRouteService.listRoutes(search)

    @GetMapping("/routes/{routeId}")
    fun routeExport(@PathVariable routeId: Long): ExportRouteResponse =
        exportRouteService.routeExport(routeId)

    @GetMapping("/routes/{routeId}/pdf", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun routePdf(
        @PathVariable routeId: Long,
        @RequestParam(required = false) dayId: Long?
    ): ResponseEntity<ByteArray> {
        val pdf = pdfExportService.routePdf(routeId, dayId)
        val fileName = if (dayId == null) {
            "routebuddy-route-$routeId.pdf"
        } else {
            "routebuddy-route-$routeId-day-$dayId.pdf"
        }
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename(fileName, StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf)
    }
}
