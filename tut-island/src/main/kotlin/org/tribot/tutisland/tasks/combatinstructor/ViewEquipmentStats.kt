package org.tribot.tutisland.tasks.combatinstructor

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.Widgets
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class ViewEquipmentStats: Task {
    override val displayName = "Opening Equipment Stats"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 400
    }

    override fun execute() {
        val combatStats = Widgets.get(387, 1).orElse(null)

        if (combatStats == null || !combatStats.isVisible) return

        if (combatStats.click()) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                Widgets.get(84, 0).map { it.isVisible }.orElse(false)
            }
        }
    }
}