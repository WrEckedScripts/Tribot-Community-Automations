package org.tribot.wrdefriender.hud

import nullablelib.NullableLib
import nullablelib.core.event.Events
import nullablelib.paint.Colors
import nullablelib.paint.Draw
import nullablelib.paint.Paint
import nullablelib.paint.PaintLayer
import org.tribot.automation.script.event.EventOverride
import org.tribot.wrdefriender.DefrienderState
import java.awt.Desktop
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.net.URI

private const val SUPPORT_URL = "https://discord.gg/Ju64CcbykJ"
private val HUD_BOUNDS = Rectangle(8, 8, 260, 104)
private val SUPPORT_BOUNDS = Rectangle(200, 19, 58, 18)

class DefrienderHud : PaintLayer {
    private val startedAt = System.currentTimeMillis()

    @Volatile
    private var supportHovered = false

    @Volatile
    private var lastMouseWasBot: Boolean? = null

    fun install() {
        val paint = Paint.add(this)
        val override = Events.overrideMouseEvents(::handleMouseEvent)
        val move = Events.onMouseMove { _, isBot -> lastMouseWasBot = isBot }
        val click = Events.onMouseClick { point, button, isBot ->
            if (!isBot && button == MouseEvent.BUTTON1 && SUPPORT_BOUNDS.contains(point)) {
                openSupportLink()
            }
        }

        Events.onScriptEnding {
            paint.close()
            override.remove()
            move.remove()
            click.remove()
        }
    }

    override fun draw(g: Graphics2D) {
        Draw.panel(g, HUD_BOUNDS, border = Colors.alpha(Colors.danger, 180))
        val oldFont = g.font
        g.font = Draw.boldFont
        Draw.glowText(g, 18, 31, "WrDefriender by WrEcked", Colors.danger, Colors.danger)
        g.font = oldFont

        val buttonColor = if (supportHovered) Colors.alpha(Colors.danger, 100) else Colors.alpha(Colors.danger, 45)
        Draw.filledBox(g, SUPPORT_BOUNDS, buttonColor)
        Draw.box(g, SUPPORT_BOUNDS, Colors.alpha(Colors.danger, 180))
        Draw.text(g, SUPPORT_BOUNDS.x + 7, SUPPORT_BOUNDS.y + 13, "Support")

        Draw.filledBox(g, Rectangle(18, 44, 240, 2), Colors.alpha(Colors.danger, 140))
        drawRow(g, 62, "Runtime", formatRuntime())
        drawRow(g, 78, "Task", DefrienderState.task)
        drawRow(g, 94, "Removed", DefrienderState.removed.toString())
    }

    private fun drawRow(g: Graphics2D, y: Int, label: String, value: String) {
        Draw.text(g, 18, y, label, Colors.dim)
        Draw.rightText(g, 258, y, value)
    }

    private fun handleMouseEvent(event: MouseEvent): EventOverride {
        supportHovered = SUPPORT_BOUNDS.contains(event.point)
        if (!supportHovered || lastMouseWasBot != false) return EventOverride.SEND

        return if (
            event.id == MouseEvent.MOUSE_PRESSED ||
            event.id == MouseEvent.MOUSE_RELEASED ||
            event.id == MouseEvent.MOUSE_CLICKED
        ) {
            EventOverride.DISMISS
        } else {
            EventOverride.SEND
        }
    }

    private fun openSupportLink() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(SUPPORT_URL))
            } else {
                NullableLib.ctx.logger.warn("Could not open Discord. Visit $SUPPORT_URL")
            }
        } catch (e: Exception) {
            NullableLib.ctx.logger.warn("Could not open Discord. Visit $SUPPORT_URL", e)
        }
    }

    private fun formatRuntime(): String {
        val seconds = (System.currentTimeMillis() - startedAt) / 1_000
        return "%02d:%02d:%02d".format(seconds / 3_600, seconds / 60 % 60, seconds % 60)
    }
}
