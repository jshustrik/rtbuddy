package com.routebuddy.exportservice.service

import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.Image
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import com.routebuddy.exportservice.dto.ExportDayDto
import com.routebuddy.exportservice.dto.ExportPointDto
import com.routebuddy.exportservice.dto.ExportRouteDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.util.Base64

@Service
class PdfExportService(
    private val exportRouteService: ExportRouteService
) {
    fun routePdf(routeId: Long, dayId: Long?): ByteArray {
        val payload = exportRouteService.routeExport(routeId)
        val route = payload.route
        val days = selectedDays(route, dayId)
        val fonts = pdfFonts()
        val out = ByteArrayOutputStream()
        val document = Document(PageSize.A4, 36f, 36f, 38f, 42f)

        PdfWriter.getInstance(document, out)
        document.open()
        document.add(title(if (dayId == null) route.title else days.first().title, fonts))
        document.add(meta(route, dayId, fonts))
        document.add(spacer(8f))

        if (dayId == null) {
            document.add(textBlock("Описание маршрута", route.description, fonts))
            document.add(textBlock("Общая длительность", "${route.durationDays} дн.", fonts))
            document.add(textBlock("Общая стоимость", "${route.totalCost.toLong()} ₽", fonts))
            addImage(document, payload.routeStaticMapUrl, 520f, 250f)
        } else {
            val day = days.first()
            document.add(textBlock("Описание дня", day.description, fonts))
            document.add(textBlock("Расходы за день", "${day.cost.toLong()} ₽", fonts))
            document.add(textBlock("Время в пути", day.travelTime.ifBlank { "не рассчитано" }, fonts))
        }

        days.forEachIndexed { index, day ->
            if (index > 0 || dayId == null) document.add(spacer(12f))
            addDay(document, day, payload.dayStaticMapUrls[day.id], fonts)
        }

        document.close()
        return out.toByteArray()
    }

    private fun selectedDays(route: ExportRouteDto, dayId: Long?): List<ExportDayDto> {
        if (dayId == null) return route.days
        val day = route.days.firstOrNull { it.id == dayId }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "День маршрута не найден")
        return listOf(day)
    }

    private fun addDay(document: Document, day: ExportDayDto, mapUrl: String?, fonts: PdfFonts) {
        document.add(Paragraph("День ${day.dayNumber}. ${day.title}", fonts.h2))
        document.add(line("Описание", day.description, fonts))
        document.add(line("Расходы", "${day.cost.toLong()} ₽", fonts))
        document.add(line("Время в пути", day.travelTime.ifBlank { "не рассчитано" }, fonts))
        addImage(document, day.photoUrl, 500f, 220f)
        addImage(document, mapUrl, 500f, 220f)

        if (day.points.isEmpty()) {
            document.add(Paragraph("Точки не добавлены.", fonts.small))
            return
        }

        document.add(Paragraph("Точки посещения", fonts.h3))
        val table = PdfPTable(floatArrayOf(0.7f, 2.4f, 2.8f, 1.6f))
        table.widthPercentage = 100f
        listOf("№", "Точка", "Описание", "Время").forEach {
            table.addCell(headerCell(it, fonts))
        }
        day.points.sortedBy { it.orderIndex }.forEachIndexed { idx, point ->
            table.addCell(bodyCell("${idx + 1}", fonts))
            table.addCell(bodyCell(point.name, fonts))
            table.addCell(bodyCell(point.description.ifBlank { coords(point) }, fonts))
            table.addCell(bodyCell(time(point), fonts))
        }
        document.add(table)

        day.points.sortedBy { it.orderIndex }.forEach { point ->
            if (!point.photoUrl.isNullOrBlank()) {
                document.add(Paragraph(point.name, fonts.bold))
                addImage(document, point.photoUrl, 260f, 160f)
            }
        }
    }

    private fun title(value: String, fonts: PdfFonts): Paragraph =
        Paragraph(value.ifBlank { "RouteBuddy" }, fonts.title).apply {
            alignment = Element.ALIGN_LEFT
            spacingAfter = 8f
        }

    private fun meta(route: ExportRouteDto, dayId: Long?, fonts: PdfFonts): Paragraph {
        val date = route.createdAt?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "не указана"
        val scope = if (dayId == null) "Маршрут целиком" else "Отдельный день"
        return Paragraph("Автор: ${route.authorUsername} • Создан: $date • $scope", fonts.small)
    }

    private fun textBlock(label: String, value: String, fonts: PdfFonts): Paragraph =
        Paragraph().apply {
            add(Phrase("$label: ", fonts.bold))
            add(Phrase(value.ifBlank { "не указано" }, fonts.regular))
            spacingAfter = 5f
        }

    private fun line(label: String, value: String, fonts: PdfFonts): Paragraph =
        textBlock(label, value, fonts)

    private fun spacer(height: Float): Paragraph =
        Paragraph(" ").apply { spacingAfter = height }

    private fun headerCell(value: String, fonts: PdfFonts): PdfPCell =
        PdfPCell(Phrase(value, fonts.bold)).apply {
            setPadding(6f)
            horizontalAlignment = Element.ALIGN_LEFT
        }

    private fun bodyCell(value: String, fonts: PdfFonts): PdfPCell =
        PdfPCell(Phrase(value, fonts.small)).apply {
            setPadding(6f)
            verticalAlignment = Element.ALIGN_TOP
        }

    private fun coords(point: ExportPointDto): String =
        if (point.lat != 0.0 || point.lon != 0.0) "${point.lat}, ${point.lon}" else ""

    private fun time(point: ExportPointDto): String =
        listOfNotNull(point.timeStart, point.timeEnd).joinToString(" - ").ifBlank { "—" }

    private fun addImage(document: Document, source: String?, maxWidth: Float, maxHeight: Float) {
        if (source.isNullOrBlank()) return
        runCatching {
            val bytes = loadImageBytes(source)
            val image = Image.getInstance(bytes)
            image.scaleToFit(maxWidth, maxHeight)
            image.spacingBefore = 4f
            image.spacingAfter = 8f
            document.add(image)
        }
    }

    private fun loadImageBytes(source: String): ByteArray {
        if (source.startsWith("data:image/", ignoreCase = true)) {
            val base64 = source.substringAfter(",", "")
            if (base64.isBlank()) throw IllegalArgumentException("empty data url")
            return Base64.getMimeDecoder().decode(base64)
        }

        val conn = URI(source).toURL().openConnection()
        conn.connectTimeout = 2500
        conn.readTimeout = 3000
        conn.getInputStream().use { input ->
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                total += read
                if (total > 5 * 1024 * 1024) throw IllegalArgumentException("image too large")
                out.write(buffer, 0, read)
            }
            return out.toByteArray()
        }
    }

    private fun pdfFonts(): PdfFonts {
        val regularPath = findFont(
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
            "/Library/Fonts/Arial Unicode.ttf",
            "/System/Library/Fonts/Supplemental/Arial.ttf"
        )
        val boldPath = findFont(
            "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
            "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
            regularPath
        )
        val regularBase = createBaseFont(regularPath)
        val boldBase = createBaseFont(boldPath)
        return PdfFonts(
            regular = Font(regularBase, 10.5f),
            small = Font(regularBase, 9f),
            bold = Font(boldBase, 10.5f, Font.BOLD),
            h3 = Font(boldBase, 12f, Font.BOLD),
            h2 = Font(boldBase, 14f, Font.BOLD),
            title = Font(boldBase, 20f, Font.BOLD)
        )
    }

    private fun findFont(vararg paths: String?): String? =
        paths.filterNotNull().firstOrNull { Files.exists(Paths.get(it)) }

    private fun createBaseFont(path: String?): BaseFont =
        if (path != null) {
            BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
        } else {
            BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED)
        }

    private data class PdfFonts(
        val regular: Font,
        val small: Font,
        val bold: Font,
        val h3: Font,
        val h2: Font,
        val title: Font
    )
}
