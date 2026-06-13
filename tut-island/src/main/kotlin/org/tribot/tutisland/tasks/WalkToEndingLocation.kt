package org.tribot.tutisland.tasks

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.MyPlayer
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.gui.Settings
import org.tribot.tutisland.util.data.Location
import org.tribot.tutisland.util.taskmanagement.Task

class WalkToEndingLocation: Task {
    override val displayName: String
        get() = "Walking to ${Settings.walkLocation.displayName}"

    override val priority = 100

    override fun canRun(): Boolean =
        Settings.walkLocation != Location.NONE &&
                Settings.walkLocation.walkingArea?.contains(MyPlayer.getTile()) == false &&
                GameState.getSetting(281) >= 1000

    override fun execute() {
        Settings.walkLocation.walkingArea?.let { area ->
            Walker.walkTo(area.randomTile)
        }
    }
}