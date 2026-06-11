package org.tribot.wrtiaracrafter

import net.runelite.api.Skill
import nullablelib.NullableLib
import nullablelib.core.Login
import nullablelib.core.WorldViews
import nullablelib.core.event.Events
import nullablelib.core.tabs.Skills
import nullablelib.paint.Colors
import nullablelib.paint.Draw
import nullablelib.paint.Paint
import nullablelib.paint.PaintLayer
import org.tribot.automation.script.event.EventOverride
import org.tribot.wrtiaracrafter.hud.TaskLabelTracker
import java.awt.Desktop
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.net.URI
import java.util.Locale
import javax.imageio.ImageIO
import kotlin.math.abs

private const val SCRIPT_NAME = "WrTiaraCrafter"
private const val AUTHOR = "WrEcked"
private const val SUPPORT_URL = "https://discord.gg/Ju64CcbykJ"
private const val AVATAR_RESOURCE = "/wrecked-avatar.png"
private const val HUD_X = 8
private const val HUD_Y = 8
private const val HUD_WIDTH = 300
private const val HUD_HEIGHT = 110
private const val PADDING = 10
private const val HEADER_HEIGHT = 26
private const val ACCENT_HEIGHT = 2
private const val ROW_HEIGHT = 16
private const val AVATAR_SIZE = 24
private const val TITLE_GAP = 6
private const val SUPPORT_BUTTON_WIDTH = 58
private const val SUPPORT_BUTTON_HEIGHT = 18

private val HUD_ACCENT = Colors.danger
private val HUD_BORDER = Colors.alpha(HUD_ACCENT, 180)

class TiaraHud : PaintLayer {
    private class Baseline(
        val trackingStart: Long,
        val runecraftingXp: Int,
        val runecraftingLevel: Int,
    )

    private val scriptStart = System.currentTimeMillis()
    private val avatar = loadAvatar()

    @Volatile
    private var baseline: Baseline? = null

    @Volatile
    private var currentRunecraftingXp = 0

    @Volatile
    private var currentRunecraftingLevel = 0

    @Volatile
    private var supportButtonHovered = false

    @Volatile
    private var lastMouseWasBot: Boolean? = null

    private var mouseGestureActive = false
    private var mouseGestureStartedInside = false
    private var mouseGestureIsPhysical = false
    private var mouseGestureDragged = false
    private var nextClickOverride: EventOverride? = null

    fun install() {
        Events.onGameTick {
            trackRunecrafting()
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
        Draw.panel(g, bounds, border = HUD_BORDER)

        val previousFont = g.font
        g.font = Draw.boldFont
        drawAvatar(g, bounds)
        Draw.glowText(
            g,
            bounds.x + PADDING + AVATAR_SIZE + TITLE_GAP,
            bounds.y + PADDING + 17,
            "$SCRIPT_NAME By $AUTHOR",
            HUD_ACCENT,
            HUD_ACCENT,
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
            Colors.alpha(HUD_ACCENT, 140),
        )

        var rowY = bounds.y + PADDING + HEADER_HEIGHT + ACCENT_HEIGHT + 4 + 12
        drawRow(g, rowY, "Runtime", formatDuration(System.currentTimeMillis() - scriptStart))
        rowY += ROW_HEIGHT
        drawRow(g, rowY, "Task", TaskLabelTracker.label)
        rowY += ROW_HEIGHT
        drawRow(g, rowY, "Experience", experienceText())
        rowY += ROW_HEIGHT
        drawRow(g, rowY, "Current Lv", currentLevelText())
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
        Draw.text(g, HUD_X + PADDING, y, label, Colors.dim)
        Draw.rightText(g, HUD_X + HUD_WIDTH - PADDING, y, value, Colors.white)
    }

    private fun drawSupportButton(g: Graphics2D) {
        val button = supportButtonBounds()
        val fill = if (supportButtonHovered) {
            Colors.alpha(HUD_ACCENT, 100)
        } else {
            Colors.alpha(HUD_ACCENT, 45)
        }

        Draw.filledBox(g, button, fill)
        Draw.box(g, button, HUD_BORDER)
        Draw.text(
            g,
            button.x + 7,
            button.y + 13,
            "Support",
            Colors.white,
        )
    }

    private fun handleMouseEvent(event: MouseEvent): EventOverride {
        val point = event.point
        val insideHud = hudBounds().contains(point)
        val insideButton = supportButtonBounds().contains(point)
        supportButtonHovered = insideButton

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
            return override ?: if (insideHud) {
                if (lastMouseWasBot == false) {
                    EventOverride.DISMISS
                } else {
                    EventOverride.SEND
                }
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
            HUD_X,
            HUD_Y,
            HUD_WIDTH,
            HUD_HEIGHT,
        )

    private fun supportButtonBounds(): Rectangle =
        Rectangle(
            HUD_X + HUD_WIDTH - PADDING - SUPPORT_BUTTON_WIDTH,
            HUD_Y + PADDING + 3,
            SUPPORT_BUTTON_WIDTH,
            SUPPORT_BUTTON_HEIGHT,
        )

    private fun openSupportLink() {
        try {
            if (
                !Desktop.isDesktopSupported() ||
                !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
            ) {
                NullableLib.ctx.logger.warn("Could not open Discord. Visit $SUPPORT_URL")
                return
            }

            Desktop.getDesktop().browse(URI(SUPPORT_URL))
        } catch (e: Exception) {
            NullableLib.ctx.logger.warn("Could not open Discord. Visit $SUPPORT_URL", e)
        }
    }

    private fun loadAvatar(): BufferedImage? {
        return try {
            TiaraHud::class.java.getResourceAsStream(AVATAR_RESOURCE)
                ?.use { ImageIO.read(it) }
        } catch (e: Exception) {
            NullableLib.ctx.logger.warn("Could not load HUD avatar", e)
            null
        }
    }

    private fun trackRunecrafting() {
        if (!Login.isLoggedIn() || WorldViews.localPlayer == null) return

        val xp = Skills.getXp(Skill.RUNECRAFT)
        val level = Skills.getLevel(Skill.RUNECRAFT)
        currentRunecraftingXp = xp
        currentRunecraftingLevel = level

        if (baseline == null) {
            baseline = Baseline(
                trackingStart = System.currentTimeMillis(),
                runecraftingXp = xp,
                runecraftingLevel = level,
            )
        }
    }

    private fun experienceText(): String {
        val start = baseline ?: return "Waiting for login"
        val gained = (currentRunecraftingXp - start.runecraftingXp).coerceAtLeast(0)
        val elapsed = System.currentTimeMillis() - start.trackingStart
        val perHour = if (elapsed <= 0) {
            0
        } else {
            (gained * 3_600_000.0 / elapsed).toInt()
        }

        return "${format(gained)} XP (${format(perHour)} XP/Hr)"
    }

    private fun currentLevelText(): String {
        val start = baseline ?: return "Waiting for login"
        val gained = currentRunecraftingLevel - start.runecraftingLevel
        val gainedText = if (gained >= 0) "+$gained" else gained.toString()
        return "$currentRunecraftingLevel ($gainedText)"
    }

    private fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1_000
        val hours = totalSeconds / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}

fun format(value: Int, decimals: Int = 0): String {
    val absValue = abs(value)
    val formatted = when {
        absValue < 1_000 -> absValue.toString()
        absValue in 1_000..999_999 -> String.format(Locale.US, "%.${decimals}f%s", absValue / 1000.0, "k")
        else -> String.format(Locale.US, "%.${decimals}f%s", absValue / 1_000_000.0, "M")
    }
    return if (value < 0) "-$formatted" else formatted
}
