package org.tribot.wrtiaracrafter

import net.runelite.api.Skill
import nullablelib.core.Login
import nullablelib.core.WorldViews
import nullablelib.core.event.Events
import nullablelib.core.tabs.Skills
import nullablelib.paint.Hud
import nullablelib.paint.Paint
import java.awt.Point
import java.util.Locale
import kotlin.math.abs

private const val SCRIPT_NAME = "WrTiaraCrafter"
private const val AUTHOR = "WrEcked"

class TiaraHud {
    private class Baseline(
        val trackingStart: Long,
        val runecraftingXp: Int,
        val runecraftingLevel: Int,
    )

    private val scriptStart = System.currentTimeMillis()

    @Volatile
    private var baseline: Baseline? = null

    @Volatile
    private var currentRunecraftingXp = 0

    @Volatile
    private var currentRunecraftingLevel = 0

    fun install() {
        Events.onGameTick {
            trackRunecrafting()
        }

        val title = "$SCRIPT_NAME By $AUTHOR"
        val hud = Hud(title, position = Point(8, 8), width = 260)
            .row("Runtime") { formatDuration(System.currentTimeMillis() - scriptStart) }
            .row("Experience") { experienceText() }
            .row("Current Lv") { currentLevelText() }

        Paint.add(hud)
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
