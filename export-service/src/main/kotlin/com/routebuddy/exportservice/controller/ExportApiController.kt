package com.routebuddy.exportservice.controller

import com.routebuddy.exportservice.dto.CurrentUserResponse
import com.routebuddy.exportservice.dto.ExportRouteListItem
import com.routebuddy.exportservice.security.PrincipalInfo
import com.routebuddy.exportservice.service.ExportFacadeService
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ExportApiController(
    private val exportFacadeService: ExportFacadeService,
) {
    @GetMapping("/users/me")
    fun me(@AuthenticationPrincipal principal: PrincipalInfo): CurrentUserResponse =
        CurrentUserResponse(userId = principal.userId, username = principal.username)

    @GetMapping("/my-routes")
    fun myRoutes(@RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String): List<ExportRouteListItem> =
        exportFacadeService.myRoutesForExport(extractBearer(authorization))

    @GetMapping("/export/route/{routeId}")
    fun exportRoute(
        @PathVariable routeId: Long,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
    ): ResponseEntity<ByteArray> {
        val bytes = exportFacadeService.exportRoutePdf(routeId, extractBearer(authorization))
        return pdfResponse(bytes, "route_$routeId.pdf")
    }

    @GetMapping("/export/day/{dayId}")
    fun exportDay(
        @PathVariable dayId: Long,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
    ): ResponseEntity<ByteArray> {
        val (filename, bytes) = exportFacadeService.exportDayPdf(dayId, extractBearer(authorization))
        return pdfResponse(bytes, filename)
    }

    private fun extractBearer(authorization: String): String {
        require(authorization.startsWith("Bearer ")) { "Authorization header must be Bearer token" }
        return authorization.substring(7)
    }

    private fun pdfResponse(bytes: ByteArray, fileName: String): ResponseEntity<ByteArray> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(fileName).build().toString(),
            )
            .body(bytes)
}
