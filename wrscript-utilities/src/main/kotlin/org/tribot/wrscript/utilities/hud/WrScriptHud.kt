package org.tribot.wrscript.utilities.hud

import net.runelite.api.Skill
import nullablelib.NullableLib
import nullablelib.core.event.Events
import nullablelib.paint.Colors
import nullablelib.paint.Draw
import nullablelib.paint.Paint
import nullablelib.paint.PaintLayer
import org.tribot.automation.script.event.EventOverride
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.net.URI
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.abs

private const val PADDING = 10
private const val HEADER_HEIGHT = 26
private const val ACCENT_HEIGHT = 2
private const val ROW_HEIGHT = 16
private const val BASE_HUD_HEIGHT = 110
private const val AVATAR_SIZE = 24
private const val TITLE_GAP = 6
private const val SUPPORT_BUTTON_WIDTH = 58
private const val SUPPORT_BUTTON_HEIGHT = 18

class WrScriptHud(
    private val scriptName: String,
    skill: Skill,
    private val author: String = "WrEcked",
    private val supportUrl: String = "https://discord.gg/Ju64CcbykJ",
    private val avatarResource: String = "/wrecked-avatar.png",
    private val position: Point = Point(8, 8),
    private val width: Int = 300,
    private val accent: Color = Colors.danger,
    private val panelFill: Color = Colors.panelBg,
    private val border: Color = Colors.alpha(accent, 180),
    private val labelColor: Color = Colors.dim,
    private val valueColor: Color = Colors.white,
) : PaintLayer {
    private val scriptStart = System.currentTimeMillis()
    private val avatar = loadAvatar()
    private val progress = SkillProgressTracker(skill)
    private val rows = mutableListOf<Pair<String, () -> String>>()

    @Volatile
    private var supportButtonHovered = false

    @Volatile
    private var lastMouseWasBot: Boolean? = null

    private var mouseGestureActive = false
    private var mouseGestureStartedInside = false
    private var mouseGestureIsPhysical = false
    private var mouseGestureDragged = false
    private var nextClickOverride: EventOverride? = null

    fun row(label: String, value: () -> String): WrScriptHud {
        rows.add(label to value)
        return this
    }

    fun install() {
        Events.onGameTick {
            progress.update()
        }

        val paintRegistration = Paint.add(this)
        val mouseOverrideRegistration = Events.overrideMouseEvents(::handleMouseEvent)
        val mouseMoveRegistration = Events.onMouseMove { _, isBot ->
            lastMouseWasBot = isBot
        }
        val mouseDragRegistration = Events.onMouseDrag { _, _, isBot ->
            lastMouseWasBot = isBot
        }
        val mouseClickRegistration = Events.onMouseClick { point, button, isBot ->
            lastMouseWasBot = isBot
            if (
                !isBot &&
                button == MouseEvent.BUTTON1 &&
                supportButtonBounds().contains(point)
            ) {
                openSupportLink()
            }
        }

        Events.onScriptEnding {
            paintRegistration.close()
            mouseOverrideRegistration.close()
            mouseMoveRegistration.close()
            mouseDragRegistration.close()
            mouseClickRegistration.close()
        }
    }

    override fun draw(g: Graphics2D) {
        val bounds = hudBounds()
        Draw.panel(g, bounds, fill = panelFill, border = border)

        val previousFont = g.font
        g.font = Draw.boldFont
        drawAvatar(g, bounds)
        Draw.glowText(
            g,
            bounds.x + PADDING + AVATAR_SIZE + TITLE_GAP,
            bounds.y + PADDING + 17,
            "$scriptName By $author",
            accent,
            accent,
        )
        g.font = previousFont

        drawSupportButton(g)

        Draw.filledBox(
            g,
            Rectangle(
                bounds.x + PADDING,
                bounds.y + PADDING + HEADER_HEIGHT,
                bounds.width - PADDING * 2,
                ACCENT_HEIGHT,
            ),
            Colors.alpha(accent, 140),
        )

        var rowY = bounds.y + PADDING + HEADER_HEIGHT + ACCENT_HEIGHT + 4 + 12
        drawRow(g, rowY, "Runtime", formatDuration(System.currentTimeMillis() - scriptStart))
        rowY += ROW_HEIGHT
        drawRow(g, rowY, "Task", TaskLabelTracker.label)
        rowY += ROW_HEIGHT
        drawRow(g, rowY, "Experience", experienceText())
        rowY += ROW_HEIGHT
        drawRow(g, rowY, "Current Lv", currentLevelText())
        for ((label, value) in rows) {
            rowY += ROW_HEIGHT
            drawRow(g, rowY, label, runCatching(value).getOrDefault("?"))
        }
    }

    private fun drawAvatar(g: Graphics2D, bounds: Rectangle) {
        val image = avatar ?: return
        g.drawImage(
            image,
            bounds.x + PADDING,
            bounds.y + PADDING,
            AVATAR_SIZE,
            AVATAR_SIZE,
            null,
        )
    }

    private fun drawRow(g: Graphics2D, y: Int, label: String, value: String) {
        Draw.text(g, position.x + PADDING, y, label, labelColor)
        Draw.rightText(g, position.x + width - PADDING, y, value, valueColor)
    }

    private fun drawSupportButton(g: Graphics2D) {
        val button = supportButtonBounds()
        val fill = if (supportButtonHovered) {
            Colors.alpha(accent, 100)
        } else {
            Colors.alpha(accent, 45)
        }

        Draw.filledBox(g, button, fill)
        Draw.box(g, button, border)
        Draw.text(
            g,
            button.x + 7,
            button.y + 13,
            "Support",
            valueColor,
        )
    }

    private fun handleMouseEvent(event: MouseEvent): EventOverride {
        val point = event.point
        val insideHud = hudBounds().contains(point)
        supportButtonHovered = supportButtonBounds().contains(point)

        if (event.id == MouseEvent.MOUSE_MOVED) {
            return EventOverride.SEND
        }

        if (event.id == MouseEvent.MOUSE_PRESSED) {
            mouseGestureActive = true
            mouseGestureStartedInside = insideHud
            mouseGestureIsPhysical = lastMouseWasBot == false
            mouseGestureDragged = false
            return gestureOverride()
        }

        if (event.id == MouseEvent.MOUSE_DRAGGED && mouseGestureActive) {
            mouseGestureDragged = true
            return gestureOverride()
        }

        if (event.id == MouseEvent.MOUSE_RELEASED && mouseGestureActive) {
            val override = gestureOverride()
            nextClickOverride = if (mouseGestureDragged) null else override
            mouseGestureActive = false
            return override
        }

        if (event.id == MouseEvent.MOUSE_CLICKED) {
            val override = nextClickOverride
            nextClickOverride = null
            return override ?: if (insideHud && lastMouseWasBot == false) {
                EventOverride.DISMISS
            } else {
                EventOverride.SEND
            }
        }

        if (mouseGestureActive) {
            return gestureOverride()
        }

        return EventOverride.SEND
    }

    private fun gestureOverride(): EventOverride =
        if (mouseGestureStartedInside && mouseGestureIsPhysical) {
            EventOverride.DISMISS
        } else {
            EventOverride.SEND
        }

    private fun hudBounds(): Rectangle =
        Rectangle(
            position.x,
            position.y,
            width,
            BASE_HUD_HEIGHT + rows.size * ROW_HEIGHT,
        )

    private fun supportButtonBounds(): Rectangle =
        Rectangle(
            position.x + width - PADDING - SUPPORT_BUTTON_WIDTH,
            position.y + PADDING + 3,
            SUPPORT_BUTTON_WIDTH,
            SUPPORT_BUTTON_HEIGHT,
        )

    private fun openSupportLink() {
        try {
            if (
                !Desktop.isDesktopSupported() ||
                !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
            ) {
                NullableLib.ctx.logger.warn("Could not open Discord. Visit $supportUrl")
                return
            }

            Desktop.getDesktop().browse(URI(supportUrl))
        } catch (e: Exception) {
            NullableLib.ctx.logger.warn("Could not open Discord. Visit $supportUrl", e)
        }
    }

    private fun loadAvatar(): BufferedImage? {
        return try {
            WrScriptHud::class.java.getResourceAsStream(avatarResource)
                ?.use { ImageIO.read(it) }
        } catch (e: Exception) {
            NullableLib.ctx.logger.warn("Could not load HUD avatar", e)
            null
        }
    }

    private fun experienceText(): String {
        if (!progress.isTracking()) return "Waiting for login"
        return "${format(progress.xpGained())} XP (${format(progress.xpPerHour())} XP/Hr)"
    }

    private fun currentLevelText(): String {
        if (!progress.isTracking()) return "Waiting for login"
        val gained = progress.levelsGained()
        val gainedText = if (gained >= 0) "+$gained" else gained.toString()
        return "${progress.level()} ($gainedText)"
    }

    private fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1_000
        val hours = totalSeconds / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    private fun format(value: Int, decimals: Int = 0): String {
        val absValue = abs(value)
        val formatted = when {
            absValue < 1_000 -> absValue.toString()
            absValue in 1_000..999_999 ->
                String.format(Locale.US, "%.${decimals}f%s", absValue / 1_000.0, "k")

            else ->
                String.format(Locale.US, "%.${decimals}f%s", absValue / 1_000_000.0, "M")
        }
        return if (value < 0) "-$formatted" else formatted
    }
}
