package com.routebuddy.exportservice.service

import com.routebuddy.exportservice.client.dto.MakeRouteDayResponse
import com.routebuddy.exportservice.client.dto.MakeRoutePointResponse
import com.routebuddy.exportservice.client.dto.MakeRouteResponse
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.math.RoundingMode
import java.nio.file.Files
import java.nio.file.Path

@Service
class PdfExportService {
    fun buildRoutePdf(route: MakeRouteResponse): ByteArray =
        buildPdf {
            line("Route: ${route.title}")
            line("Author: ${route.authorUsername}")
            line("Duration (days): ${route.durationDays}")
            line("Total cost (RUB): ${route.totalCostRub.setScale(2, RoundingMode.HALF_UP)}")
            line("Description: ${route.description}")
            line("Map: ${buildRouteMapLink(route)}")
            line("")
            line("Days:")
            route.days.sortedBy { it.dayOrder }.forEach { day ->
                appendDay(day)
                line("")
            }
        }

    fun buildDayPdf(route: MakeRouteResponse, day: MakeRouteDayResponse): ByteArray =
        buildPdf {
            line("Day ${day.dayOrder}: ${day.theme}")
            line("Route: ${route.title}")
            line("Author: ${route.authorUsername}")
            line("Day cost (RUB): ${day.dayCostRub.setScale(2, RoundingMode.HALF_UP)}")
            line("Travel time (min): ${day.travelTimeMinutes}")
            line("Description: ${day.description}")
            line("Map: ${buildDayMapLink(day)}")
            line("")
            line("Points:")
            day.points.sortedBy { it.orderIndex }.forEach { appendPoint(it) }
        }

    private fun PdfBuilder.appendDay(day: MakeRouteDayResponse) {
        line("Day ${day.dayOrder}: ${day.theme}")
        line("  Description: ${day.description}")
        line("  Cost (RUB): ${day.dayCostRub.setScale(2, RoundingMode.HALF_UP)}")
        line("  Travel time (min): ${day.travelTimeMinutes}")
        line("  Points:")
        day.points.sortedBy { it.orderIndex }.forEach { point -> appendPoint(point, "    ") }
    }

    private fun PdfBuilder.appendPoint(point: MakeRoutePointResponse, prefix: String = "  ") {
        line("${prefix}${point.orderIndex}. ${point.title}")
        line("${prefix}   Coordinates: ${point.latitude}, ${point.longitude}")
        if (!point.description.isNullOrBlank()) line("${prefix}   Description: ${point.description}")
        if (!point.timeStart.isNullOrBlank() || !point.timeEnd.isNullOrBlank()) {
            line("${prefix}   Time: ${point.timeStart ?: "-"} - ${point.timeEnd ?: "-"}")
        }
        if (point.imageUrls.isNotEmpty()) {
            line("${prefix}   Images:")
            point.imageUrls.forEach { url -> line("$prefix   - $url") }
        }
    }

    private fun buildRouteMapLink(route: MakeRouteResponse): String {
        val orderedPoints = route.days
            .sortedBy { it.dayOrder }
            .flatMap { it.points.sortedBy { point -> point.orderIndex } }
        if (orderedPoints.isEmpty()) return "-"
        val pt = orderedPoints.joinToString("~") { "${it.longitude},${it.latitude},pm2blm" }
        return "https://static-maps.yandex.ru/1.x/?l=map&size=650,450&pt=$pt"
    }

    private fun buildDayMapLink(day: MakeRouteDayResponse): String {
        val orderedPoints = day.points.sortedBy { it.orderIndex }
        if (orderedPoints.isEmpty()) return "-"
        val pt = orderedPoints.joinToString("~") { "${it.longitude},${it.latitude},pm2rdm" }
        return "https://static-maps.yandex.ru/1.x/?l=map&size=650,450&pt=$pt"
    }

    private fun buildPdf(block: PdfBuilder.() -> Unit): ByteArray {
        val document = PDDocument()
        try {
            val builder = PdfBuilder(document)
            builder.block()
            return builder.toByteArray()
        } finally {
            document.close()
        }
    }

    private class PdfBuilder(private val document: PDDocument) {
        private val font: PDFont = loadFont(document)
        private val fontSize = 11f
        private val leading = 15f
        private val margin = 50f
        private val width = PDRectangle.A4.width - margin * 2
        private var page: PDPage = PDPage(PDRectangle.A4)
        private var stream: PDPageContentStream
        private var y = PDRectangle.A4.height - margin

        init {
            document.addPage(page)
            stream = PDPageContentStream(document, page)
            stream.beginText()
            stream.setFont(font, fontSize)
            stream.newLineAtOffset(margin, y)
        }

        fun line(text: String) {
            val sanitized = text.replace("\n", " ").trimEnd()
            val lines = wrap(sanitized.ifEmpty { " " })
            for (line in lines) {
                ensureSpace()
                stream.showText(line)
                stream.newLineAtOffset(0f, -leading)
                y -= leading
            }
        }

        fun toByteArray(): ByteArray {
            stream.endText()
            stream.close()
            val out = ByteArrayOutputStream()
            document.save(out)
            return out.toByteArray()
        }

        private fun ensureSpace() {
            if (y <= margin) {
                stream.endText()
                stream.close()
                page = PDPage(PDRectangle.A4)
                document.addPage(page)
                stream = PDPageContentStream(document, page)
                y = PDRectangle.A4.height - margin
                stream.beginText()
                stream.setFont(font, fontSize)
                stream.newLineAtOffset(margin, y)
            }
        }

        private fun wrap(text: String): List<String> {
            if (text.isBlank()) return listOf(" ")
            val result = mutableListOf<String>()
            val words = text.split(" ")
            var current = StringBuilder()
            for (word in words) {
                val candidate = if (current.isEmpty()) word else "${current} $word"
                val candidateWidth = font.getStringWidth(candidate) / 1000 * fontSize
                if (candidateWidth > width && current.isNotEmpty()) {
                    result += current.toString()
                    current = StringBuilder(word)
                } else {
                    if (current.isNotEmpty()) current.append(' ')
                    current.append(word)
                }
            }
            if (current.isNotEmpty()) result += current.toString()
            return result
        }

        private fun loadFont(document: PDDocument): PDFont {
            val candidates = listOf(
                Path.of("C:/Windows/Fonts/arial.ttf"),
                Path.of("C:/Windows/Fonts/segoeui.ttf"),
                Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
            )
            for (path in candidates) {
                if (Files.exists(path)) {
                    Files.newInputStream(path).use { input ->
                        return PDType0Font.load(document, input, true)
                    }
                }
            }
            return PDType1Font(Standard14Fonts.FontName.HELVETICA)
        }
    }
}
