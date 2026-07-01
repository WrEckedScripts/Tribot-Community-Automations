package org.tribot.wrscript.utilities.hud

import net.runelite.api.Skill
import nullablelib.core.Login
import nullablelib.core.WorldViews
import nullablelib.core.tabs.Skills

class SkillProgressTracker(private val skill: Skill) {
    @Volatile
    private var trackingStart = 0L

    @Volatile
    private var startingXp = -1

    @Volatile
    private var startingLevel = -1

    @Volatile
    private var currentXp = 0

    @Volatile
    private var currentLevel = 0

    fun update() {
        if (!Login.isLoggedIn() || WorldViews.localPlayer == null) return

        currentXp = Skills.getXp(skill)
        currentLevel = Skills.getLevel(skill)

        if (startingXp < 0) {
            trackingStart = System.currentTimeMillis()
            startingXp = currentXp
            startingLevel = currentLevel
        }
    }

    fun xpGained(): Int =
        if (startingXp < 0) 0 else (currentXp - startingXp).coerceAtLeast(0)

    fun xpPerHour(): Int {
        val elapsed = elapsedTime()
        return if (elapsed <= 0) 0 else (xpGained() * 3_600_000.0 / elapsed).toInt()
    }

    fun level(): Int = currentLevel

    fun levelsGained(): Int =
        if (startingLevel < 0) 0 else currentLevel - startingLevel

    fun isTracking(): Boolean = startingXp >= 0

    private fun elapsedTime(): Long =
        if (trackingStart == 0L) 0L else System.currentTimeMillis() - trackingStart
}
