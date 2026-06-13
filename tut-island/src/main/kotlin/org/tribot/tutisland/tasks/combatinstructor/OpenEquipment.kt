package org.tribot.tutisland.tasks.combatinstructor

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.GameTab
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class OpenEquipment: Task {
    override val displayName = "Opening Equipment"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 390
    }

    override fun execute() {
        if (!GameTab.EQUIPMENT.open()) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameTab.EQUIPMENT.isOpen }
        }
    }
}