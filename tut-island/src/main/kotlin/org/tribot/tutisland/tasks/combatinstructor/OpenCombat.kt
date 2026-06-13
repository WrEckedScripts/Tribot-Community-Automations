package org.tribot.tutisland.tasks.combatinstructor

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.GameTab
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class OpenCombat: Task {
    override val displayName = "Opening Combat"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 430
    }

    override fun execute() {
        if (!GameTab.COMBAT.open()) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameTab.COMBAT.isOpen }
        }
    }
}