package org.tribot.tutisland.tasks.gielinorguide

import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.Widgets
import org.tribot.script.sdk.types.Widget
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class HandleExperienceWidget: Task {
    override val displayName = "Selecting Experience Type"
    override val priority = 0

    override fun canRun(): Boolean {
        return Widgets.get(929, 3).map { it.isVisible }.orElse(false)
    }

    override fun execute() {
        val options: List<Widget> = listOfNotNull(
            Widgets.get(929, 5).orElse(null), // new player
            Widgets.get(929, 6).orElse(null), // past player
            Widgets.get(929, 7).orElse(null)  // experienced player
        ).filter { it.isVisible }

        if (options.isEmpty()) return

        if (options.random().click()) {
            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                Widgets.get(929, 3).map { !it.isVisible }.orElse(true)
            }
        }
    }
}