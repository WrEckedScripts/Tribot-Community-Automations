package org.tribot.wrtiaracrafter.contracts

import org.tribot.wrtiaracrafter.hud.TaskLabelTracker

interface TaskContract {
    val name: String

    fun execute(): Boolean {
        TaskLabelTracker.label = name
        return perform()
    }

    fun perform(): Boolean
}
