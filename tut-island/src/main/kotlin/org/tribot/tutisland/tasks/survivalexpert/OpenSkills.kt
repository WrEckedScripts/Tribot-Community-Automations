package org.tribot.tutisland.tasks.survivalexpert

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.GameTab
import org.tribot.script.sdk.Waiting
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class OpenSkills: Task {
    override val displayName = "Opening Skills"
    override val priority = 0

    override fun canRun(): Boolean {
        return !GameTab.SKILLS.isOpen &&
                GameState.getSetting(281) == 50
    }

    override fun execute() {
        if (!GameTab.SKILLS.open()) {
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) { GameTab.SKILLS.isOpen }
        }
    }
}