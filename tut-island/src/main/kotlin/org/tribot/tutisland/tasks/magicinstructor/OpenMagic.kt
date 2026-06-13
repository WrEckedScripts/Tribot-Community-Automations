package org.tribot.tutisland.tasks.magicinstructor

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.GameTab
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class OpenMagic: Task {
    override val displayName = "Opening Magic"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 630
    }

    override fun execute() {
        if (!GameTab.MAGIC.open()) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameTab.MAGIC.isOpen }
        }
    }
}