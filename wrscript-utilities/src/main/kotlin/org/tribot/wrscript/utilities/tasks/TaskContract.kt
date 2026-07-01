package org.tribot.wrscript.utilities.tasks

import org.tribot.wrscript.utilities.hud.TaskLabelTracker

interface TaskContract {
    val name: String

    fun execute(): Boolean {
        TaskLabelTracker.label = name
        return perform()
    }

    fun perform(): Boolean
}
