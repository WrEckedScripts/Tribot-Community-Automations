package org.tribot.tutisland.paint

import org.tribot.api.Timing
import org.tribot.script.sdk.Widgets
import org.tribot.script.sdk.painting.Painting
import java.awt.*

class TutIslandPaint(
    private val statusProvider: () -> String,
    private val startTime: Long = System.currentTimeMillis()
) {
    private fun chatboxYOrDefault(): Int =
        Widgets.get(162, 0)
            .map { it.bounds.y }
            .orElse(338)

    private fun bottomLeftViewportXY(boxHeight: Int): Point {
        val x = 15
        val y = (chatboxYOrDefault() - boxHeight - 5).coerceAtLeast(15)
        return Point(x, y)
    }

    fun install() {
        Painting.addPaint { raw ->
            val g = raw.create() as Graphics2D
            val runtime = Timing.msToString(System.currentTimeMillis() - startTime)
            val rows = listOf(
                "CrazyDavy Tutorial Island",
                "Runtime: $runtime",
                "Status: ${statusProvider()}"
            )

            val margin = 8
            val rowGap = 4
            val titleFont = Font("Dialog", Font.BOLD, 13)
            val rowFont = Font("Dialog", Font.PLAIN, 11)

            g.font = rowFont
            val rowHeight = g.fontMetrics.height + rowGap
            val rowWidth = rows.maxOf { g.fontMetrics.stringWidth(it) }
            val boxWidth = maxOf(240, rowWidth + margin * 2)
            val boxHeight = rowHeight * rows.size + margin
            val point = bottomLeftViewportXY(boxHeight)

            g.color = Color(0, 0, 0, 175)
            g.fillRoundRect(point.x, point.y, boxWidth, boxHeight, 12, 12)

            g.color = Color(99, 214, 176, 210)
            g.stroke = BasicStroke(1f)
            g.drawRoundRect(point.x, point.y, boxWidth, boxHeight, 12, 12)

            rows.forEachIndexed { index, row ->
                g.font = if (index == 0) titleFont else rowFont
                g.color = if (index == 0) Color(99, 214, 176) else Color.WHITE
                g.drawString(row, point.x + margin, point.y + margin / 2 + (index + 1) * rowHeight - 2)
            }

            g.dispose()
        }
    }
}